package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticatedAccountRestoreTest {
    private val profile = AuthenticatedProfile("id", "a@b.c", "2026-01-01", emptyList(), kotlinx.serialization.json.JsonObject(emptyMap()))

    private fun session(expiresAt: Long) = AuthSession("access", "refresh", "Bearer", 3600uL, expiresAt)

    @Test
    fun noStoredTokenStaysSignedOut() = runTest {
        val account = AuthenticatedAccountBloc({ profile }, backgroundScope)
        account.dispatch(AuthenticatedAccountEvent.Restore)
        runCurrent()
        assertEquals(AccountMode.SignedOut, account.state.value.mode)
        assertEquals(false, account.state.value.restoring)
    }

    @Test
    fun validStoredTokenSignsInAutomatically() = runTest {
        val storage = MemorySecureSessionStorage()
        val sessions = SessionManager(storage, { throw SessionExpiredException() })
        sessions.start(session(currentTimeMillis() + 3_600_000))
        val account = AuthenticatedAccountBloc({ profile }, backgroundScope, SessionManager(storage, { throw SessionExpiredException() }))
        account.dispatch(AuthenticatedAccountEvent.Restore)
        runCurrent()
        assertEquals(AccountMode.Authenticated, account.state.value.mode)
        assertEquals(profile, account.state.value.profile)
    }

    @Test
    fun rejectedStoredTokenReturnsToSignIn() = runTest {
        val storage = MemorySecureSessionStorage()
        SessionManager(storage, { throw SessionExpiredException() }).start(session(0))
        val account = AuthenticatedAccountBloc({ profile }, backgroundScope, SessionManager(storage, { throw SessionExpiredException() }))
        account.dispatch(AuthenticatedAccountEvent.Restore)
        runCurrent()
        assertEquals(AccountMode.SignedOut, account.state.value.mode)
        assertEquals(null, storage.read())
    }

    @Test
    fun logoutClearsStoredSessionButReleaseKeepsIt() = runTest {
        val storage = MemorySecureSessionStorage()
        val account = AuthenticatedAccountBloc({ profile }, backgroundScope, SessionManager(storage, { throw SessionExpiredException() }))
        account.dispatch(AuthenticatedAccountEvent.LoginCompleted(session(currentTimeMillis() + 3_600_000)))
        runCurrent()
        account.dispatch(AuthenticatedAccountEvent.Release)
        runCurrent()
        assertTrue(storage.read() != null)
        account.dispatch(AuthenticatedAccountEvent.Logout)
        runCurrent()
        assertEquals(null, storage.read())
    }
}
