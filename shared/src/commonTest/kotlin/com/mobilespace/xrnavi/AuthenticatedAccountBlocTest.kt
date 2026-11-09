package com.mobilespace.xrnavi

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticatedAccountBlocTest {
    @Test
    fun successfulLoginNeverEntersDemoAndLoadsProfileFromSessionForAllFourSections() = runTest {
        val result = CompletableDeferred<AuthenticatedProfile>()
        var calls = 0
        val account = AuthenticatedAccountBloc({ authenticated ->
            assertEquals("access", authenticated.accessToken)
            calls++
            result.await()
        }, backgroundScope)
        val signIn = SignInBloc({ _, _ -> session() }, backgroundScope) {
            account.dispatch(AuthenticatedAccountEvent.LoginCompleted(it))
        }
        signIn.dispatch(SignInEvent.EmailChanged("login@example.org"))
        signIn.dispatch(SignInEvent.PasswordChanged("test-password"))
        signIn.dispatch(SignInEvent.Submit)
        runCurrent()
        assertTrue(signIn.state.value.signedIn)
        assertEquals(AccountMode.Authenticated, account.state.value.mode)
        assertTrue(account.state.value.loading)
        assertNull(account.state.value.profile)
        assertEquals(1, calls)
        account.dispatch(AuthenticatedAccountEvent.RetryProfile)
        runCurrent()
        assertEquals(1, calls)
        result.complete(profile())
        runCurrent()
        listOf(DemoSection.Map, DemoSection.Garage, DemoSection.Settings, DemoSection.Profile)
            .forEach {
                account.dispatch(AuthenticatedAccountEvent.OpenSection(it))
                assertEquals(it, account.state.value.section)
                assertEquals(AccountMode.Authenticated, account.state.value.mode)
                assertEquals("stored@example.org", account.state.value.profile?.email)
                assertEquals(listOf("backend-role"), account.state.value.profile?.roles)
            }
        account.dispatch(AuthenticatedAccountEvent.OpenSection(DemoSection.VehicleDetails))
        assertEquals(DemoSection.Profile, account.state.value.section)
        account.dispatch(AuthenticatedAccountEvent.Logout)
        signIn.dispatch(SignInEvent.Cancel)
        assertEquals(AccountMode.SignedOut, account.state.value.mode)
        assertNull(account.state.value.profile)
        assertFalse(signIn.state.value.signedIn)
    }

    @Test
    fun failedProfileLoadingStaysAuthenticatedWithoutSyntheticFallbackAndCanRetry() = runTest {
        var calls = 0
        val account = AuthenticatedAccountBloc({
            if (++calls == 1) throw IllegalStateException("secret raw response")
            profile()
        }, backgroundScope)
        account.dispatch(AuthenticatedAccountEvent.LoginCompleted(session()))
        runCurrent()
        assertEquals(AccountMode.Authenticated, account.state.value.mode)
        assertFalse(account.state.value.loading)
        assertNull(account.state.value.profile)
        assertTrue(account.state.value.error.isNotBlank())
        assertFalse(account.state.value.error.contains("secret"))
        account.dispatch(AuthenticatedAccountEvent.RetryProfile)
        runCurrent()
        assertEquals(profile(), account.state.value.profile)
        assertEquals("", account.state.value.error)
    }

    @Test
    fun invalidProfileAndTimeoutHaveSafeRetryableErrors() = runTest {
        for (timeout in listOf(false, true)) {
            val account = AuthenticatedAccountBloc({
                if (timeout) awaitCancellation() else throw InvalidProfileResponseException()
            }, backgroundScope)
            account.dispatch(AuthenticatedAccountEvent.LoginCompleted(session()))
            runCurrent()
            if (timeout) {
                advanceTimeBy(signInTimeoutMillis)
                runCurrent()
            }
            assertEquals(AccountMode.Authenticated, account.state.value.mode)
            assertFalse(account.state.value.loading)
            assertNull(account.state.value.profile)
            assertTrue(account.state.value.error.isNotBlank())
            account.dispatch(AuthenticatedAccountEvent.Logout)
        }
    }

    @Test
    fun logoutAndExplicitDemoCancelLoadingAndPreventLateProfileUpdates() = runTest {
        for (next in listOf(
            AuthenticatedAccountEvent.Logout,
            AuthenticatedAccountEvent.ContinueWithoutLogin,
        )) {
            val result = CompletableDeferred<AuthenticatedProfile>()
            val account = AuthenticatedAccountBloc({ result.await() }, backgroundScope)
            account.dispatch(AuthenticatedAccountEvent.LoginCompleted(session()))
            runCurrent()
            account.dispatch(next)
            result.complete(profile())
            runCurrent()
            assertNull(account.state.value.profile)
            assertFalse(account.state.value.loading)
            assertEquals(
                if (next == AuthenticatedAccountEvent.Logout) AccountMode.SignedOut else AccountMode.Demo,
                account.state.value.mode,
            )
            account.dispatch(AuthenticatedAccountEvent.RetryProfile)
            runCurrent()
            assertNull(account.state.value.profile)
        }
    }

    @Test
    fun expiryClearsProfileAndRequiresNewLoginWithoutRefreshOrDemo() = runTest {
        var calls = 0
        val account = AuthenticatedAccountBloc({ calls++; profile() }, backgroundScope)
        account.dispatch(AuthenticatedAccountEvent.LoginCompleted(
            AuthSession("access", "refresh", "Bearer", 1uL),
        ))
        runCurrent()
        assertEquals(profile(), account.state.value.profile)
        advanceTimeBy(1000)
        runCurrent()
        assertTrue(account.state.value.expired)
        assertNull(account.state.value.profile)
        assertEquals(AccountMode.Authenticated, account.state.value.mode)
        account.dispatch(AuthenticatedAccountEvent.RetryProfile)
        runCurrent()
        assertEquals(1, calls)
    }

    private fun session() = AuthSession("access", "refresh", "Bearer", 3600uL)
    private fun profile() = AuthenticatedProfile(
        "backend-id", "stored@example.org", "2026-01-01T00:00:00+00:00",
        listOf("backend-role"), JsonObject(emptyMap()),
    )
}
