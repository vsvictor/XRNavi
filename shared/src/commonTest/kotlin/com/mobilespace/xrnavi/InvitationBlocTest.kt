package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InvitationBlocTest {
    @Test
    fun invitationRequiresCodeAndConsentAndNeverClaimsMembership() {
        val bloc = InvitationBloc()
        bloc.dispatch(InvitationEvent.Accept)
        assertTrue(bloc.state.value.notice.orEmpty().contains("Введіть код"))
        bloc.dispatch(InvitationEvent.Code("EXAMPLE"))
        bloc.dispatch(InvitationEvent.Consent(true))
        bloc.dispatch(InvitationEvent.Accept)
        assertTrue(bloc.state.value.notice.orEmpty().contains("Членство в організації не змінено"))
        bloc.dispatch(InvitationEvent.Code("OTHER"))
        assertFalse(bloc.state.value.consent)
    }
}
