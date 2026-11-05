package com.mobilespace.xrnavi

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SignInBlocTest {
    @Test
    fun visibilityTogglePreservesInputsAndAccountSelectionOnlyFillsEmail() = runTest {
        var calls = 0
        val bloc = SignInBloc({ _, _ -> calls++; session() }, this)
        bloc.dispatch(SignInEvent.PasswordChanged("secret"))
        bloc.dispatch(SignInEvent.TogglePasswordVisibility)
        assertTrue(bloc.state.value.passwordVisible)
        assertEquals("secret", bloc.state.value.password)
        bloc.dispatch(SignInEvent.TogglePasswordVisibility)
        assertFalse(bloc.state.value.passwordVisible)
        bloc.dispatch(SignInEvent.SelectAccount)
        assertTrue(bloc.state.value.accountPickerRequested)
        bloc.dispatch(SignInEvent.AccountPickerLaunched)
        assertFalse(bloc.state.value.accountPickerRequested)
        bloc.dispatch(SignInEvent.AccountSelected("chosen@example.org"))
        assertEquals("chosen@example.org", bloc.state.value.email)
        assertEquals(0, calls)
        bloc.dispatch(SignInEvent.AccountSelected("not an email"))
        assertEquals("chosen@example.org", bloc.state.value.email)
        assertTrue(bloc.state.value.notice.isNotBlank())
        bloc.dispatch(SignInEvent.AccountSelectionFailed("System selection unavailable"))
        assertEquals("System selection unavailable", bloc.state.value.notice)
    }

    @Test
    fun validatesBeforeCallingAndDoesNotImposeRegistrationPasswordRules() = runTest {
        var calls = 0
        val bloc = SignInBloc({ _, password ->
            assertEquals("x", password)
            calls++
            session()
        }, this)
        bloc.dispatch(SignInEvent.Submit)
        assertTrue(bloc.state.value.notice.isNotBlank())
        bloc.dispatch(SignInEvent.EmailChanged("valid@example.org"))
        bloc.dispatch(SignInEvent.Submit)
        assertEquals("Введіть пароль", bloc.state.value.notice)
        assertEquals(0, calls)
        bloc.dispatch(SignInEvent.PasswordChanged("x"))
        bloc.dispatch(SignInEvent.Submit)
        runCurrent()
        assertEquals(1, calls)
        assertTrue(bloc.state.value.signedIn)
    }

    @Test
    fun submitDeduplicatesAndSuccessfulLoginClearsPasswordAndOpensMapOnce() = runTest {
        var calls = 0
        val result = CompletableDeferred<AuthSession>()
        val bloc = SignInBloc({ email, password ->
            assertEquals("test@example.org", email)
            assertEquals(" secret ", password)
            calls++
            result.await()
        }, this)
        credentials(bloc)
        bloc.dispatch(SignInEvent.TogglePasswordVisibility)
        repeat(3) { bloc.dispatch(SignInEvent.Submit) }
        assertTrue(bloc.state.value.submitting)
        bloc.dispatch(SignInEvent.EmailChanged("other@example.org"))
        bloc.dispatch(SignInEvent.PasswordChanged("other"))
        runCurrent()
        assertEquals(1, calls)
        result.complete(session())
        runCurrent()
        val state = bloc.state.value
        assertTrue(state.signedIn)
        assertTrue(state.openMap)
        assertFalse(state.submitting)
        assertFalse(state.passwordVisible)
        assertEquals("", state.password)
        bloc.dispatch(SignInEvent.MapOpened)
        assertFalse(bloc.state.value.openMap)
        bloc.dispatch(SignInEvent.Submit)
        runCurrent()
        assertEquals(1, calls)
        bloc.dispatch(SignInEvent.Cancel)
        assertFalse(bloc.state.value.signedIn)
    }

    @Test
    fun rpcAndInvalidResponseFailuresAreVisibleAndDoNotLeakServerDetails() = runTest {
        for (error in listOf(
            IllegalStateException("Sensitive raw server response"),
            InvalidAuthResponseException(),
        )) {
            val bloc = SignInBloc({ _, _ -> throw error }, this)
            credentials(bloc)
            bloc.dispatch(SignInEvent.Submit)
            runCurrent()
            assertFalse(bloc.state.value.submitting)
            assertFalse(bloc.state.value.signedIn)
            assertFalse(bloc.state.value.openMap)
            assertTrue(bloc.state.value.notice.isNotBlank())
            assertFalse(bloc.state.value.notice.contains("Sensitive"))
        }
    }

    @Test
    fun connectionOrLoginTimeoutAllowsAnotherSubmission() = runTest {
        var calls = 0
        val bloc = SignInBloc({ _, _ -> calls++; awaitCancellation() }, this)
        credentials(bloc)
        bloc.dispatch(SignInEvent.Submit)
        runCurrent()
        advanceTimeBy(signInTimeoutMillis)
        runCurrent()
        assertFalse(bloc.state.value.submitting)
        assertTrue(bloc.state.value.notice.contains("вчасно"))
        bloc.dispatch(SignInEvent.Submit)
        runCurrent()
        assertEquals(2, calls)
        bloc.dispatch(SignInEvent.Cancel)
    }

    @Test
    fun leavingSignInCancelsRequestAndClearsSecretsWithoutLateNavigation() = runTest {
        val result = CompletableDeferred<AuthSession>()
        val bloc = SignInBloc({ _, _ -> result.await() }, this)
        credentials(bloc)
        bloc.dispatch(SignInEvent.Submit)
        runCurrent()
        bloc.dispatch(SignInEvent.Cancel)
        result.complete(session())
        runCurrent()
        assertFalse(bloc.state.value.signedIn)
        assertFalse(bloc.state.value.openMap)
        assertFalse(bloc.state.value.submitting)
        assertEquals("", bloc.state.value.password)
        assertEquals("", bloc.state.value.notice)
    }

    private fun credentials(bloc: SignInBloc) {
        bloc.dispatch(SignInEvent.EmailChanged(" test@example.org "))
        bloc.dispatch(SignInEvent.PasswordChanged(" secret "))
    }

    private fun session() = AuthSession("access", "refresh", "Bearer", 3600uL)
}
