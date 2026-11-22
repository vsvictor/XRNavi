package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class VehicleDetailsState(val vehicleId: String? = null)

internal sealed interface VehicleDetailsEvent {
    data class Open(val vehicleId: String) : VehicleDetailsEvent
    data object Close : VehicleDetailsEvent
}

internal class VehicleDetailsBloc {
    private val mutableState = MutableStateFlow(VehicleDetailsState())
    val state = mutableState.asStateFlow()

    /** Returns true when the details screen was open and got closed. */
    fun handleBack(): Boolean {
        if (mutableState.value.vehicleId == null) return false
        dispatch(VehicleDetailsEvent.Close)
        return true
    }

    fun dispatch(event: VehicleDetailsEvent) {
        mutableState.value = when (event) {
            is VehicleDetailsEvent.Open -> VehicleDetailsState(event.vehicleId)
            VehicleDetailsEvent.Close -> VehicleDetailsState()
        }
    }
}
