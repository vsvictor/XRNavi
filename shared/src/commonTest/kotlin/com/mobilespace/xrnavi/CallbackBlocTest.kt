package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CallbackBlocTest {
    private class FakeService : CallbackService {
        var running = false
        var onToken: (String) -> Unit = {}
        var onFail: (Throwable) -> Unit = {}
        override suspend fun run(onResetToken: (String) -> Unit, onFailure: (Throwable) -> Unit) {
            running = true
            onToken = onResetToken
            onFail = onFailure
            try {
                awaitCancellation()
            } finally {
                running = false
            }
        }
    }

    @Test
    fun deliversTokenAndStopsOnLogout() {
        val service = FakeService()
        val bloc = CallbackBloc(service, TestScope(UnconfinedTestDispatcher()))
        bloc.dispatch(CallbackEvent.Started)
        assertTrue(service.running)
        service.onToken("abc")
        assertEquals("abc", bloc.state.value.resetToken)
        bloc.dispatch(CallbackEvent.ResetTokenDismissed)
        assertNull(bloc.state.value.resetToken)
        bloc.dispatch(CallbackEvent.Stopped)
        assertFalse(service.running)
        assertFalse(bloc.state.value.active)
    }

    @Test
    fun marksFailure() {
        val service = FakeService()
        val bloc = CallbackBloc(service, TestScope(UnconfinedTestDispatcher()))
        bloc.dispatch(CallbackEvent.Started)
        service.onFail(IllegalStateException("x"))
        assertTrue(bloc.state.value.failed)
    }

    @Test
    fun parsesOnlyResetTokenCallbacks() {
        assertEquals("t1", parseResetToken("auth.passwordResetToken", """{"reset_token":"t1"}"""))
        assertNull(parseResetToken("other", """{"reset_token":"t1"}"""))
        assertNull(parseResetToken("auth.passwordResetToken", null))
        assertNull(parseResetToken("auth.passwordResetToken", "not json"))
        assertNull(parseResetToken("auth.passwordResetToken", """{"reset_token":""}"""))
    }
}
