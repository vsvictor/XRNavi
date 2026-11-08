package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class VehicleReviewState(
    val height: String = "",
    val length: String = "",
    val mass: String = ""
)

internal sealed interface VehicleReviewEvent {
    data class Height(val value: String) : VehicleReviewEvent
    data class Length(val value: String) : VehicleReviewEvent
    data class Mass(val value: String) : VehicleReviewEvent
}

internal class VehicleReviewBloc {
    private val mutableState = MutableStateFlow(VehicleReviewState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: VehicleReviewEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: VehicleReviewState, event: VehicleReviewEvent): VehicleReviewState =
            when (event) {
                is VehicleReviewEvent.Height -> state.copy(height = event.value)
                is VehicleReviewEvent.Length -> state.copy(length = event.value)
                is VehicleReviewEvent.Mass -> state.copy(mass = event.value)
            }
    }
}
