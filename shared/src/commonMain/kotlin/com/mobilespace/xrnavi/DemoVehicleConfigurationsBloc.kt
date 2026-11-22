package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class DemoVehicleConfigurationsState(
    val draft: VehicleConfigurationDraft = VehicleConfigurationDraft("", null, null)
)

internal sealed interface DemoVehicleConfigurationsEvent {
    /** Starts a fresh draft whenever the viewed vehicle or its applied configuration changes. */
    data class Sync(val vehicleId: String, val appliedConfigurationId: String) :
        DemoVehicleConfigurationsEvent

    data class Select(val configuration: DemoVehicleConfiguration) : DemoVehicleConfigurationsEvent
    data object Cancel : DemoVehicleConfigurationsEvent
}

internal class DemoVehicleConfigurationsBloc {
    private val mutableState = MutableStateFlow(DemoVehicleConfigurationsState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: DemoVehicleConfigurationsEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(
            state: DemoVehicleConfigurationsState,
            event: DemoVehicleConfigurationsEvent
        ): DemoVehicleConfigurationsState = when (event) {
            is DemoVehicleConfigurationsEvent.Sync ->
                if (state.draft.vehicleId == event.vehicleId &&
                    state.draft.appliedConfigurationId == event.appliedConfigurationId
                ) state
                else DemoVehicleConfigurationsState(
                    VehicleConfigurationDraft(
                        event.vehicleId, event.appliedConfigurationId, event.appliedConfigurationId
                    )
                )

            is DemoVehicleConfigurationsEvent.Select ->
                state.copy(draft = state.draft.select(event.configuration))

            DemoVehicleConfigurationsEvent.Cancel -> state.copy(draft = state.draft.cancel())
        }
    }
}
