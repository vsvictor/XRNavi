package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class VehicleModelState(
    val model: String = "Mustang",
    val year: String = "2024",
    val trim: String = "GT · 5.0 V8 · автоматична"
)

internal sealed interface VehicleModelEvent {
    data class Model(val value: String) : VehicleModelEvent
    data class Year(val value: String) : VehicleModelEvent
    data class Trim(val value: String) : VehicleModelEvent
}

internal class VehicleModelBloc {
    private val mutableState = MutableStateFlow(VehicleModelState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: VehicleModelEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: VehicleModelState, event: VehicleModelEvent): VehicleModelState =
            when (event) {
                is VehicleModelEvent.Model -> state.copy(model = event.value)
                is VehicleModelEvent.Year -> state.copy(year = event.value)
                is VehicleModelEvent.Trim -> state.copy(trim = event.value)
            }
    }
}
