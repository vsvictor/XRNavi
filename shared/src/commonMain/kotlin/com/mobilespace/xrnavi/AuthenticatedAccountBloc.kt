package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

internal enum class AccountMode { SignedOut, Demo, Authenticated }

internal data class AuthenticatedAccountState(
    val mode: AccountMode = AccountMode.SignedOut,
    val section: DemoSection = DemoSection.Map,
    val profile: AuthenticatedProfile? = null,
    val loading: Boolean = false,
    val error: String = "",
    val expired: Boolean = false,
    val restoring: Boolean = false,
) {
    override fun toString(): String = "AuthenticatedAccountState(mode=$mode, loading=$loading)"
}

internal sealed interface AuthenticatedAccountEvent {
    class LoginCompleted(val session: AuthSession) : AuthenticatedAccountEvent
    class OpenSection(val section: DemoSection) : AuthenticatedAccountEvent
    data object RetryProfile : AuthenticatedAccountEvent
    data object ContinueWithoutLogin : AuthenticatedAccountEvent
    data object Logout : AuthenticatedAccountEvent
    data object Restore : AuthenticatedAccountEvent

    /** Drops in-memory state only; the stored session stays for the next launch. */
    data object Release : AuthenticatedAccountEvent
}

internal class AuthenticatedAccountBloc(
    private val profiles: AuthenticatedProfileService,
    private val scope: CoroutineScope,
    private val sessions: SessionManager = SessionManager(
        MemorySecureSessionStorage(),
        { throw SessionExpiredException() },
    ),
) {
    private val mutableState = MutableStateFlow(AuthenticatedAccountState())
    val state = mutableState.asStateFlow()
    private var session: AuthSession? = null
    fun currentSession(): AuthSession? = sessions.current() ?: session
    private var request: Job? = null
    private var expiry: Job? = null

    init {
        scope.launch { sessions.expirations.collect { expireSession() } }
    }

    fun dispatch(event: AuthenticatedAccountEvent) {
        when (event) {
            is AuthenticatedAccountEvent.LoginCompleted -> {
                clearSession()
                session = event.session
                mutableState.value = AuthenticatedAccountState(mode = AccountMode.Authenticated)
                scope.launch { sessions.start(event.session) }
                scheduleRefresh(event.session.expiresInSeconds.coerceAtMost(
                    (Long.MAX_VALUE / 1000).toULong()
                ).toLong() * 1000)
                loadProfile()
            }
            is AuthenticatedAccountEvent.OpenSection -> if (
                mutableState.value.mode == AccountMode.Authenticated &&
                event.section in listOf(
                    DemoSection.Map, DemoSection.Garage, DemoSection.Settings, DemoSection.Profile,
                )
            ) {
                mutableState.value = mutableState.value.copy(section = event.section)
            }
            AuthenticatedAccountEvent.RetryProfile -> loadProfile()
            AuthenticatedAccountEvent.ContinueWithoutLogin -> {
                clearSession()
                mutableState.value = AuthenticatedAccountState(mode = AccountMode.Demo)
            }
            AuthenticatedAccountEvent.Logout -> {
                clearSession()
                scope.launch { sessions.clear() }
                mutableState.value = AuthenticatedAccountState()
            }
            AuthenticatedAccountEvent.Restore -> restore()
            AuthenticatedAccountEvent.Release -> {
                clearSession()
                mutableState.value = AuthenticatedAccountState()
            }
        }
    }

    private fun restore() {
        if (mutableState.value.restoring || mutableState.value.mode != AccountMode.SignedOut) return
        mutableState.value = AuthenticatedAccountState(restoring = true)
        request = scope.launch {
            when (val result = sessions.restore()) {
                is SessionRestoreResult.Ready -> {
                    session = result.session
                    mutableState.value = AuthenticatedAccountState(mode = AccountMode.Authenticated)
                    scheduleRefresh((result.session.expiresAtMillis - currentTimeMillis()).coerceAtLeast(0))
                    loadProfile()
                }
                SessionRestoreResult.None -> mutableState.value = AuthenticatedAccountState()
                SessionRestoreResult.Rejected -> mutableState.value = AuthenticatedAccountState(
                    error = "Сесія завершилась. Увійдіть знову.",
                )
                SessionRestoreResult.Unavailable -> mutableState.value = AuthenticatedAccountState(
                    error = "Не вдалося перевірити сесію. Перевірте з'єднання та увійдіть знову.",
                )
            }
        }
    }

    /** Renews the token shortly before it expires; a rejected refresh ends the session. */
    private fun scheduleRefresh(lifetimeMillis: Long) {
        expiry?.cancel()
        expiry = scope.launch {
            var wait = maxOf(lifetimeMillis - sessionRefreshSkewMillis, lifetimeMillis / 2)
            while (true) {
                delay(wait)
                try {
                    session = sessions.refreshNow()
                    wait = maxOf(
                        session!!.expiresInSeconds.coerceAtMost((Long.MAX_VALUE / 2000).toULong()).toLong() * 1000 -
                            sessionRefreshSkewMillis,
                        1_000,
                    )
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: SessionExpiredException) {
                    expireSession()
                    return@launch
                } catch (_: Exception) {
                    wait = signInTimeoutMillis
                }
            }
        }
    }

    private fun expireSession() {
        if (mutableState.value.mode != AccountMode.Authenticated) return
        request?.cancel()
        request = null
        expiry?.cancel()
        expiry = null
        session = null
        mutableState.value = mutableState.value.copy(
            profile = null, loading = false, expired = true,
            error = "Термін сесії завершився. Вийдіть та увійдіть знову.",
        )
    }

    private fun clearSession() {
        request?.cancel()
        expiry?.cancel()
        request = null
        expiry = null
        session = null
    }

    private fun loadProfile() {
        val authenticated = session ?: return
        if (mutableState.value.loading) return
        mutableState.value = mutableState.value.copy(loading = true, error = "", profile = null)
        request = scope.launch {
            try {
                val profile = withTimeout(signInTimeoutMillis) { profiles.getProfile(authenticated) }
                currentCoroutineContext().ensureActive()
                mutableState.value = mutableState.value.copy(profile = profile, loading = false)
            } catch (_: TimeoutCancellationException) {
                currentCoroutineContext().ensureActive()
                fail("Сервер не відповів вчасно. Повторіть завантаження профілю.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: InvalidProfileResponseException) {
                currentCoroutineContext().ensureActive()
                fail("Сервер повернув некоректний профіль. Повторіть завантаження.")
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                fail("Не вдалося завантажити профіль. Перевірте з'єднання та повторіть. Якщо сесія недійсна, увійдіть знову.")
            }
        }
    }

    private fun fail(message: String) {
        mutableState.value = mutableState.value.copy(profile = null, loading = false, error = message)
    }
}
