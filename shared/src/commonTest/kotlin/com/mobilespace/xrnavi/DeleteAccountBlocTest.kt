package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteAccountBlocTest {
    @Test
    fun deletionNeverReportsSuccessOrDeletesAnythingEvenAfterConsent() {
        val bloc = DeleteAccountBloc()
        bloc.dispatch(DeleteAccountEvent.Confirm)
        assertTrue(bloc.state.value.notice.orEmpty().contains("Підтвердьте"))
        bloc.dispatch(DeleteAccountEvent.Consent(true))
        bloc.dispatch(DeleteAccountEvent.Confirm)
        assertTrue(bloc.state.value.notice.orEmpty().contains("не видалено"))
        bloc.dispatch(DeleteAccountEvent.Left)
        assertFalse(bloc.state.value.consent)
        assertEquals(null, bloc.state.value.notice)
    }
}
