package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RecoveryBlocServiceTest {
    private class FakeService(var failure: Exception? = null) : RecoveryService {
        val requested = mutableListOf<String>()
        val confirmed = mutableListOf<Pair<String, String>>()
        override suspend fun requestReset(email: String) {
            failure?.let { throw it }
            requested += email
        }

        override suspend fun confirmReset(resetToken: String, newPassword: String) {
            failure?.let { throw it }
            confirmed += resetToken to newPassword
        }
    }

    private fun bloc(service: FakeService) =
        RecoveryBloc(false, service, TestScope(UnconfinedTestDispatcher()))

    @Test
    fun requestMovesToCheckEmail() {
        val service = FakeService()
        val bloc = bloc(service)
        bloc.dispatch(RecoveryEvent.EmailChanged(" a@b.co "))
        bloc.dispatch(RecoveryEvent.ResetRequested)
        assertEquals(listOf("a@b.co"), service.requested)
        assertEquals(RecoveryStage.CheckEmail, bloc.state.value.stage)
        assertFalse(bloc.state.value.busy)
    }

    @Test
    fun requestFailureKeepsRequestStage() {
        val bloc = bloc(FakeService(RuntimeException("net")))
        bloc.dispatch(RecoveryEvent.EmailChanged("a@b.co"))
        bloc.dispatch(RecoveryEvent.ResetRequested)
        assertEquals(RecoveryStage.Request, bloc.state.value.stage)
        assertEquals(recoveryNetworkMessage, bloc.state.value.notice)
    }

    @Test
    fun confirmChangesPasswordAndDropsToken() {
        val service = FakeService()
        val bloc = bloc(service)
        bloc.dispatch(RecoveryEvent.TokenReceived("tok"))
        assertEquals(RecoveryStage.NewPassword, bloc.state.value.stage)
        bloc.dispatch(RecoveryEvent.PasswordChanged("Str0ng!Pass1"))
        bloc.dispatch(RecoveryEvent.ConfirmationChanged("Str0ng!Pass1"))
        bloc.dispatch(RecoveryEvent.NewPasswordSubmitted)
        assertEquals(listOf("tok" to "Str0ng!Pass1"), service.confirmed)
        assertEquals(RecoveryStage.Changed, bloc.state.value.stage)
        assertEquals("", bloc.state.value.resetToken)
        assertEquals("", bloc.state.value.password)
    }

    @Test
    fun rejectedTokenShowsInvalidLink() {
        val bloc = bloc(FakeService(RuntimeException("RPC error -32001")))
        bloc.dispatch(RecoveryEvent.TokenReceived("tok"))
        bloc.dispatch(RecoveryEvent.PasswordChanged("Str0ng!Pass1"))
        bloc.dispatch(RecoveryEvent.ConfirmationChanged("Str0ng!Pass1"))
        bloc.dispatch(RecoveryEvent.NewPasswordSubmitted)
        assertEquals(RecoveryStage.InvalidLink, bloc.state.value.stage)
    }

    @Test
    fun rpcServiceCallsPublicMethods() = kotlinx.coroutines.test.runTest {
        val calls = mutableListOf<Pair<String, String>>()
        val rpc = RpcRecoveryService { m, p -> calls += m to p; "{}" }
        rpc.requestReset("a@b.co")
        rpc.confirmReset("t", "p")
        assertEquals("auth.requestPasswordReset", calls[0].first)
        assertEquals("auth.confirmPasswordReset", calls[1].first)
        assertTrue(calls[1].second.contains("reset_token"))
    }
}
