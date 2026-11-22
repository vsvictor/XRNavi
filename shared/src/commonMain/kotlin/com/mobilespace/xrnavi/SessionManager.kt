package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

internal const val sessionRefreshSkewMillis = 30_000L

/**
 * Single owner of the OAuth 2 session: persists it, checks its validity and refreshes it.
 * Refreshes are serialized so concurrent requests never spend one refresh token twice.
 */
internal class SessionManager(
    private val storage: SecureSessionStorage,
    private val refresher: SessionRefreshService,
    private val clock: () -> Long = ::currentTimeMillis,
) {
    private val lock = Mutex()
    private var session: AuthSession? = null
    private val mutableExpirations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emits when the session was rejected by the server and removed. */
    val expirations: SharedFlow<Unit> = mutableExpirations.asSharedFlow()

    fun current(): AuthSession? = session

    suspend fun start(newSession: AuthSession) = lock.withLock {
        session = newSession
        persist(newSession)
    }

    suspend fun clear() = lock.withLock { drop() }

    suspend fun restore(): SessionRestoreResult = lock.withLock {
        val stored = decode(storage.read())
        if (stored == null) {
            storage.clear()
            return SessionRestoreResult.None
        }
        session = stored
        if (isValid(stored)) return SessionRestoreResult.Ready(stored)
        try {
            SessionRestoreResult.Ready(refreshLocked(stored))
        } catch (_: SessionExpiredException) {
            SessionRestoreResult.Rejected
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            SessionRestoreResult.Unavailable
        }
    }

    /** Returns a session whose access token has not expired, refreshing it if needed. */
    suspend fun validSession(): AuthSession = lock.withLock {
        val active = session ?: throw SessionExpiredException()
        if (isValid(active)) active else refreshLocked(active)
    }

    /** Refreshes after the server rejected [rejectedAccessToken], unless another caller already did. */
    suspend fun refreshAfterRejection(rejectedAccessToken: String): AuthSession = lock.withLock {
        val active = session ?: throw SessionExpiredException()
        if (active.accessToken != rejectedAccessToken) active else refreshLocked(active)
    }

    /** Refreshes regardless of the clock, e.g. when the scheduled expiry time has been reached. */
    suspend fun refreshNow(): AuthSession = lock.withLock {
        refreshLocked(session ?: throw SessionExpiredException())
    }

    private fun isValid(value: AuthSession) = value.expiresAtMillis - sessionRefreshSkewMillis > clock()

    private suspend fun refreshLocked(active: AuthSession): AuthSession {
        val renewed = try {
            refresher.refresh(active.refreshToken)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            if (failure.isAuthRejection() || failure is InvalidAuthResponseException) {
                drop()
                mutableExpirations.tryEmit(Unit)
                throw SessionExpiredException()
            }
            throw failure
        }
        session = renewed
        persist(renewed)
        return renewed
    }

    private fun drop() {
        session = null
        storage.clear()
    }

    private fun persist(value: AuthSession) {
        storage.write(buildJsonObject {
            put("access_token", value.accessToken)
            put("refresh_token", value.refreshToken)
            put("token_type", value.tokenType)
            put("expires_in", value.expiresInSeconds.toLong())
            put("expires_at", value.expiresAtMillis)
        }.toString())
    }

    private fun decode(raw: String?): AuthSession? {
        val fields = try {
            Json.parseToJsonElement(raw ?: return null) as? JsonObject
        } catch (_: Exception) {
            null
        } ?: return null
        fun text(name: String) = (fields[name] as? JsonPrimitive)?.takeIf { it.isString }
            ?.content?.takeIf { it.isNotBlank() }
        fun number(name: String) = (fields[name] as? JsonPrimitive)?.takeUnless { it.isString }?.longOrNull
        return AuthSession(
            accessToken = text("access_token") ?: return null,
            refreshToken = text("refresh_token") ?: return null,
            tokenType = text("token_type") ?: return null,
            expiresInSeconds = number("expires_in")?.takeIf { it > 0 }?.toULong() ?: return null,
            expiresAtMillis = number("expires_at") ?: return null,
        )
    }
}
