package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class DeleteAccountState(
    val consent: Boolean = false,
    val notice: String? = null
)

internal sealed interface DeleteAccountEvent {
    data class Consent(val value: Boolean) : DeleteAccountEvent
    data object Confirm : DeleteAccountEvent
    data object DismissNotice : DeleteAccountEvent
    data object Left : DeleteAccountEvent
}

internal class DeleteAccountBloc {
    private val mutableState = MutableStateFlow(DeleteAccountState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: DeleteAccountEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: DeleteAccountState, event: DeleteAccountEvent): DeleteAccountState =
            when (event) {
                is DeleteAccountEvent.Consent -> state.copy(consent = event.value)
                DeleteAccountEvent.Confirm -> state.copy(
                    notice = if (!state.consent) "Підтвердьте ознайомлення з наслідками видалення."
                    else "Видалення аккаунта не підключено. Аккаунт, сесію та локальні дані не видалено."
                )

                DeleteAccountEvent.DismissNotice -> state.copy(notice = null)
                DeleteAccountEvent.Left -> state.copy(consent = false, notice = null)
            }
    }
}
