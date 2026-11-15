package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AddVehicleScreen(
    state: AddVehicleState,
    dispatch: (AddVehicleEvent) -> Unit,
    landscape: Boolean = false,
    compactLandscape: Boolean = false,
) {
    if (state.step == AddVehicleStep.Review) {
        state.added?.let {
            AddVehicleReviewScreen(it, updated = state.editingVehicle != null) {
                dispatch(AddVehicleEvent.Close)
            }
        }
        return
    }
    if (state.editCatalogError.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(state.editCatalogError, color = MaterialTheme.colorScheme.error)
            if (state.selectedMake != null) {
                OutlinedButton(onClick = { dispatch(AddVehicleEvent.RetryEditCatalog) }) {
                    Text("Повторити")
                }
            }
        }
        return
    }

    if (landscape) {
        Row(
            Modifier.fillMaxWidth().fillMaxHeight().testTag("add-vehicle"),
            horizontalArrangement = Arrangement.spacedBy(if (compactLandscape) 12.dp else 24.dp),
        ) {
            AddVehicleSidebar(
                state = state,
                modifier = Modifier.width(if (compactLandscape) 176.dp else 304.dp),
                imageHeight = if (compactLandscape) 104.dp else 180.dp,
                showScreenTitle = true,
            )
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AddVehicleTabs(state.selectedTab) { dispatch(AddVehicleEvent.SelectTab(it)) }
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AddVehicleTabContent(state, dispatch)
                }
                if (state.selectedTab == AddVehicleTab.Equipment && state.equipment.selectedTrim != null &&
                    !state.equipment.loading && state.equipment.error.isEmpty()
                ) {
                    AddVehicleSaveButton(state) { dispatch(AddVehicleEvent.Submit) }
                }
            }
        }
    } else {
        Column(
            Modifier.fillMaxWidth().testTag("add-vehicle"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (state.editingVehicle == null) "Додати авто" else "Редагування авто",
                fontFamily = rememberRobotoCondensedFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 29.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AddVehicleSidebar(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                imageHeight = 196.dp,
            )
            AddVehicleTabs(state.selectedTab) { dispatch(AddVehicleEvent.SelectTab(it)) }
            AddVehicleTabContent(state, dispatch)
            if (state.selectedTab == AddVehicleTab.Equipment && state.equipment.selectedTrim != null &&
                !state.equipment.loading && state.equipment.error.isEmpty()
            ) {
                AddVehicleSaveButton(state) { dispatch(AddVehicleEvent.Submit) }
            }
        }
    }
}
