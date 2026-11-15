package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun AddVehicleFuelConsumptionDetails(equipment: VehicleEquipmentState, onRetry: () -> Unit) {
    if (equipment.selectedTrim == null) return
    when {
        equipment.mileagesLoading -> Unit
        equipment.mileagesError.isNotEmpty() -> {
            Text(equipment.mileagesError, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text("Повторити") }
        }
        else -> AddVehicleEquipmentSection("Витрата палива та запас ходу", equipment.mileages.map(::equipmentFields))
    }
}
