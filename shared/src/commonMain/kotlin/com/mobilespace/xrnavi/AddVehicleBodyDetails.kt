package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun AddVehicleBodyDetails(equipment: VehicleEquipmentState, onRetry: () -> Unit) {
    if (equipment.selectedTrim == null) return
    when {
        equipment.bodiesLoading -> Unit
        equipment.bodiesError.isNotEmpty() -> {
            Text(equipment.bodiesError, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text("Повторити") }
        }
        else -> AddVehicleEquipmentSection("Кузов", equipment.bodies.map(::equipmentFields))
    }
}
