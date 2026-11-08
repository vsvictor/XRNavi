package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class AdditionalNoticeState(val notice: String? = null)

internal sealed interface AdditionalNoticeEvent {
    data class Unavailable(val action: String) : AdditionalNoticeEvent
    data object Dismiss : AdditionalNoticeEvent
}

/** Explains that a demo action has no connected service behind it. */
internal class AdditionalNoticeBloc {
    private val mutableState = MutableStateFlow(AdditionalNoticeState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: AdditionalNoticeEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(
            state: AdditionalNoticeState,
            event: AdditionalNoticeEvent
        ): AdditionalNoticeState = when (event) {
            is AdditionalNoticeEvent.Unavailable -> state.copy(
                notice = "${event.action}: сервіс не підключено. Дію не виконано; серверні дані не змінено."
            )

            AdditionalNoticeEvent.Dismiss -> state.copy(notice = null)
        }
    }
}
