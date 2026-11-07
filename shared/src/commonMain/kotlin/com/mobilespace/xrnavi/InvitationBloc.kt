package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class InvitationState(
    val code: String = "",
    val consent: Boolean = false,
    val notice: String? = null
)

internal sealed interface InvitationEvent {
    data class Code(val value: String) : InvitationEvent
    data class Consent(val value: Boolean) : InvitationEvent
    data object Accept : InvitationEvent
    data object DismissNotice : InvitationEvent
    data object Left : InvitationEvent
}

internal class InvitationBloc {
    private val mutableState = MutableStateFlow(InvitationState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: InvitationEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: InvitationState, event: InvitationEvent): InvitationState = when (event) {
            is InvitationEvent.Code -> state.copy(code = event.value, consent = false)
            is InvitationEvent.Consent -> state.copy(consent = event.value)
            InvitationEvent.Accept -> state.copy(
                notice = if (!state.consent || state.code.isBlank())
                    "Введіть код запрошення та підтвердьте ознайомлення з правилами."
                else "Перевірка й прийняття запрошення не підключені. Членство в організації не змінено."
            )

            InvitationEvent.DismissNotice -> state.copy(notice = null)
            InvitationEvent.Left -> state.copy(consent = false, notice = null)
        }
    }
}
