package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class HistoryFilter(val label: String) {
    All("Усі"), Personal("Особисті"), Work("Робочі")
}

internal data class TripHistoryState(val filter: HistoryFilter = HistoryFilter.All)

internal sealed interface TripHistoryEvent {
    data class Filter(val value: HistoryFilter) : TripHistoryEvent
}

internal class TripHistoryBloc {
    private val mutableState = MutableStateFlow(TripHistoryState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: TripHistoryEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: TripHistoryState, event: TripHistoryEvent): TripHistoryState =
            when (event) {
                is TripHistoryEvent.Filter -> state.copy(filter = event.value)
            }
    }
}
