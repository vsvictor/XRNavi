package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun AddVehicleTransmissionDetails(equipment: VehicleEquipmentState, onRetry: () -> Unit) {
    if (equipment.selectedTrim == null) return
    when {
        equipment.enginesLoading -> Unit
        equipment.enginesError.isNotEmpty() -> {
            Text(equipment.enginesError, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text("Повторити") }
        }
        else -> AddVehicleEquipmentSection(
            "Трансмісія", equipment.engines.map(::transmissionEquipmentFields),
        )
    }
}
