package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class VehicleCatalogState(
    val query: String = "",
    val vehicleClass: GarageFilter = GarageFilter.Passenger
)

internal sealed interface VehicleCatalogEvent {
    data class Query(val value: String) : VehicleCatalogEvent
    data class VehicleClass(val value: GarageFilter) : VehicleCatalogEvent
}

internal class VehicleCatalogBloc {
    private val mutableState = MutableStateFlow(VehicleCatalogState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: VehicleCatalogEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: VehicleCatalogState, event: VehicleCatalogEvent): VehicleCatalogState =
            when (event) {
                is VehicleCatalogEvent.Query -> state.copy(query = event.value)
                is VehicleCatalogEvent.VehicleClass -> state.copy(vehicleClass = event.value)
            }
    }
}
