package com.mobilespace.xrnavi

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun AddVehicleTabContent(state: AddVehicleState, dispatch: (AddVehicleEvent) -> Unit) {
    if (state.vinLoading) {
        CircularProgressIndicator(Modifier.testTag("add-vehicle-vin-loading"))
        return
    }
    when (state.selectedTab) {
        AddVehicleTab.General -> AddVehicleGeneralTab(state, dispatch)
        AddVehicleTab.Equipment -> AddVehicleEquipmentTab(state, dispatch)
    }
}
