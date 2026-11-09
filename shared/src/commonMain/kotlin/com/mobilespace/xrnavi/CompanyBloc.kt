package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class CompanyState(
    val dispatcherNotifications: Boolean = true,
    val voicePrompts: Boolean = true,
    val notice: String? = null
)

internal sealed interface CompanyEvent {
    data class DispatcherNotifications(val value: Boolean) : CompanyEvent
    data class VoicePrompts(val value: Boolean) : CompanyEvent
    data object DismissNotice : CompanyEvent
}

internal class CompanyBloc {
    private val mutableState = MutableStateFlow(CompanyState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: CompanyEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: CompanyState, event: CompanyEvent): CompanyState = when (event) {
            is CompanyEvent.DispatcherNotifications -> state.copy(
                dispatcherNotifications = event.value,
                notice = "Змінено лише локальний приклад. Сервіс сповіщень диспетчера не підключено."
            )

            is CompanyEvent.VoicePrompts -> state.copy(
                voicePrompts = event.value,
                notice = "Змінено лише локальний приклад. Голосовий сервіс не підключено."
            )

            CompanyEvent.DismissNotice -> state.copy(notice = null)
        }
    }
}
