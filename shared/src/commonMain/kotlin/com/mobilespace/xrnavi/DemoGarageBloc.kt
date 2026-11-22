package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class DemoGarageState(val filter: GarageFilter = GarageFilter.All)

internal sealed interface DemoGarageEvent {
    data class Filter(val value: GarageFilter) : DemoGarageEvent
}

internal class DemoGarageBloc {
    private val mutableState = MutableStateFlow(DemoGarageState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: DemoGarageEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: DemoGarageState, event: DemoGarageEvent): DemoGarageState =
            when (event) {
                is DemoGarageEvent.Filter -> state.copy(filter = event.value)
            }
    }
}
