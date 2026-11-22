package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleScreen(
    state: AddVehicleState,
    dispatch: (AddVehicleEvent) -> Unit,
    landscape: Boolean = false,
    compactLandscape: Boolean = false,
) {
    if (state.step == AddVehicleStep.Review) {
        state.added?.let {
            AddVehicleReviewScreen(
                it,
                updated = state.editingVehicle != null,
            ) {
                dispatch(AddVehicleEvent.Close)
            }
        }
        return
    }
    if (landscape) {
        Row(
            Modifier.fillMaxWidth().testTag("add-vehicle"),
            horizontalArrangement = Arrangement.spacedBy(if (compactLandscape) 12.dp else 24.dp),
        ) {
            AddVehicleSidebar(
                state = state,
                modifier = Modifier.weight(0.46f),
            )
            Column(
                Modifier.weight(0.54f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.editCatalogError.isNotEmpty()) {
                    Text(state.editCatalogError, color = MaterialTheme.colorScheme.error)
                    if (state.selectedMake != null) OutlinedButton(onClick = {
                        dispatch(
                            AddVehicleEvent.RetryEditCatalog
                        )
                    }) { Text("Повторити") }
                }
                AddVehicleSaveButton(state) { dispatch(AddVehicleEvent.Submit) }
                AddVehicleTabs(state.selectedTab) { dispatch(AddVehicleEvent.SelectTab(it)) }
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AddVehicleTabContent(state, dispatch)
                }
            }
        }
    } else {
        Column(
            Modifier.fillMaxWidth().testTag("add-vehicle"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AddVehicleSidebar(
                state = state,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.editCatalogError.isNotEmpty()) {
                Text(state.editCatalogError, color = MaterialTheme.colorScheme.error)
                if (state.selectedMake != null) OutlinedButton(onClick = { dispatch(AddVehicleEvent.RetryEditCatalog) }) {
                    Text(
                        "Повторити"
                    )
                }
            }
            AddVehicleSaveButton(state) { dispatch(AddVehicleEvent.Submit) }
            AddVehicleTabs(state.selectedTab) { dispatch(AddVehicleEvent.SelectTab(it)) }
            AddVehicleTabContent(state, dispatch)
        }
    }
}
