package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleEquipmentTab(state: AddVehicleState, dispatch: (AddVehicleEvent) -> Unit) {
    Column(
        Modifier.fillMaxWidth().testTag("add-vehicle-equipment"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.selectedMake == null || state.selectedModel == null || state.year == null) {
            Text("Оберіть марку, модель та рік на вкладці «Загальне».")
            return@Column
        }
        Text("${state.selectedMake.name} ${state.selectedModel.name} · ${state.year}")
        val equipment = state.equipment
        if (equipment.vinNotice.isNotEmpty()) Text(equipment.vinNotice)
        if (equipment.error.isNotEmpty()) {
            Text(equipment.error, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = { dispatch(AddVehicleEvent.RetryEquipment) }) { Text("Повторити") }
        } else if (!equipment.loading && equipment.trims.isEmpty()) {
            Text("Для цього авто комплектацій не знайдено.")
        }
        if (!equipment.loading && equipment.error.isEmpty()) {
            Text("Комплектація", style = MaterialTheme.typography.titleSmall)
            AddVehicleDropdown(
                value = equipment.trimQuery,
                selection = equipment.trimCursor,
                placeholder = "Оберіть комплектацію",
                options = equipment.filteredTrims.map { it.id to it.name },
                readOnly = true,
                optionDividers = true,
                popupMaxHeight = null,
                expanded = equipment.trimExpanded,
                enabled = equipment.trims.isNotEmpty(),
                tag = "add-vehicle-equipment-trim",
                onValueChange = { dispatch(AddVehicleEvent.EquipmentTrimQueryChanged(it)) },
                onSelectionChanged = { dispatch(AddVehicleEvent.EquipmentTrimCursorChanged(it)) },
                onExpandedChange = { dispatch(AddVehicleEvent.EquipmentTrimExpanded(it)) },
                onFocusChanged = { dispatch(AddVehicleEvent.EquipmentTrimExpanded(it)) },
                onSelected = { dispatch(AddVehicleEvent.SelectEquipmentTrim(it)) },
            )
            if (equipment.selectedTrim == null) return@Column
            AddVehicleBodyDetails(equipment) { dispatch(AddVehicleEvent.RetryEquipmentBodies) }
            AddVehicleEngineDetails(equipment) { dispatch(AddVehicleEvent.RetryEquipmentEngines) }
            AddVehicleTransmissionDetails(equipment) { dispatch(AddVehicleEvent.RetryEquipmentEngines) }
            AddVehicleFuelConsumptionDetails(equipment) { dispatch(AddVehicleEvent.RetryEquipmentMileages) }
            VehicleConfigurationField.entries.filter {
                it != VehicleConfigurationField.Body && it != VehicleConfigurationField.Engine
                        && it != VehicleConfigurationField.Transmission
            }.forEach { field ->
                AddVehicleColorTable(field, equipment) {
                    dispatch(AddVehicleEvent.SelectConfigurationOption(field, it))
                }
            }
        }
    }
}
