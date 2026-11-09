package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Host state of the demo account area: viewed vehicle, notice dialog and theme picker. */
internal data class DemoAccountState(
    val viewedVehicleId: String? = null,
    val notice: String? = null,
    val themePickerVisible: Boolean = false
)

internal sealed interface DemoAccountEvent {
    data class ViewVehicle(val vehicleId: String) : DemoAccountEvent
    data class Unavailable(val explanation: String) : DemoAccountEvent
    data object ShowThemePicker : DemoAccountEvent
    data object DismissDialog : DemoAccountEvent
    data object Left : DemoAccountEvent
}

internal class DemoAccountBloc {
    private val mutableState = MutableStateFlow(DemoAccountState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: DemoAccountEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: DemoAccountState, event: DemoAccountEvent): DemoAccountState =
            when (event) {
                is DemoAccountEvent.ViewVehicle -> state.copy(viewedVehicleId = event.vehicleId)
                is DemoAccountEvent.Unavailable -> state.copy(notice = event.explanation)
                DemoAccountEvent.ShowThemePicker -> state.copy(themePickerVisible = true)
                DemoAccountEvent.DismissDialog -> state.copy(notice = null, themePickerVisible = false)
                DemoAccountEvent.Left -> DemoAccountState()
            }
    }
}
