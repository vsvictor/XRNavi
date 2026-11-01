package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModelStore
import com.mobilespace.xrnavi.presentation.auth.RecoveryBloc
import com.mobilespace.xrnavi.presentation.auth.RecoveryEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecoveryBlocTest {
    @Test
    fun initialRequestHasNoPrematureErrors() {
        val bloc = RecoveryBloc()
        val state = bloc.state.value
        assertEquals("", state.email)
        assertNull(state.emailError)
        assertFalse(state.canRequest)
        assertFalse(state.requestUnavailable)
    }

    @Test
    fun emailValidationAppearsAfterBlurOrEditing() {
        val bloc = RecoveryBloc()
        bloc.dispatch(RecoveryEvent.EmailTouched)
        assertNotNull(bloc.state.value.emailError)
        bloc.dispatch(RecoveryEvent.EmailChanged("invalid"))
        assertNotNull(bloc.state.value.emailError)
        bloc.dispatch(RecoveryEvent.EmailChanged("test+reset@example.invalid"))
        assertNull(bloc.state.value.emailError)
        assertTrue(bloc.state.value.canRequest)
    }

    @Test
    fun validRequestReportsUnavailableWithoutClaimingDelivery() {
        val bloc = RecoveryBloc()
        bloc.dispatch(RecoveryEvent.EmailChanged("test@example.invalid"))
        repeat(2) { bloc.dispatch(RecoveryEvent.RequestReset) }

        assertTrue(bloc.state.value.requestUnavailable)
        assertEquals("test@example.invalid", bloc.state.value.email)
    }

    @Test
    fun invalidRequestDoesNotClaimDelivery() {
        val bloc = RecoveryBloc()
        bloc.dispatch(RecoveryEvent.RequestReset)

        assertNotNull(bloc.state.value.emailError)
        assertFalse(bloc.state.value.requestUnavailable)
    }

    @Test
    fun cancelAndLifetimeEndClearAddressAndNotice() {
        val bloc = RecoveryBloc()
        bloc.dispatch(RecoveryEvent.EmailChanged("test@example.invalid"))
        bloc.dispatch(RecoveryEvent.RequestReset)
        bloc.dispatch(RecoveryEvent.Cancel)

        assertEquals("", bloc.state.value.email)
        assertFalse(bloc.state.value.requestUnavailable)
        assertNull(bloc.state.value.emailError)

        val store = ViewModelStore()
        store.put("recovery", bloc)
        bloc.dispatch(RecoveryEvent.EmailChanged("another@example.com"))
        store.clear()
        assertEquals("", bloc.state.value.email)
    }
}
