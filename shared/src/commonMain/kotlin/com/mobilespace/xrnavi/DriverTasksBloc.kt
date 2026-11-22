package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class DriverTasksState(val completedTasks: Boolean = false)

internal sealed interface DriverTasksEvent {
    data class CompletedTasks(val value: Boolean) : DriverTasksEvent
}

internal class DriverTasksBloc {
    private val mutableState = MutableStateFlow(DriverTasksState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: DriverTasksEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: DriverTasksState, event: DriverTasksEvent): DriverTasksState =
            when (event) {
                is DriverTasksEvent.CompletedTasks -> state.copy(completedTasks = event.value)
            }
    }
}
