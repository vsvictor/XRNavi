package com.mobilespace.xrnavi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun VehicleDetailsScreen(vehicle: GarageVehicle, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("vehicle-details"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(vehicle.name, style = MaterialTheme.typography.titleLarge)
        Text("Статус: ${vehicle.verification.label()}")
        if (vehicle.needsVerification) {
            Text(
                "Профіль не підтверджено: маршрут для великогабаритного авто недоступний.",
                Modifier.testTag("vehicle-unverified"), color = MaterialTheme.colorScheme.error,
            )
        }
        vehicle.heightMeters?.let { Text("Висота: $it м") }
        vehicle.widthMeters?.let { Text("Ширина: $it м") }
        vehicle.lengthMeters?.let { Text("Довжина: $it м") }
        vehicle.weightKg?.let { Text("Маса: $it кг") }
        OutlinedButton(onClick = onBack, modifier = Modifier.testTag("vehicle-details-back")) { Text("Назад") }
    }
}
