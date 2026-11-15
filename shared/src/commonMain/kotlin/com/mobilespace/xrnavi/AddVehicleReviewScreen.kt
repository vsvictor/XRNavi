package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleReviewScreen(vehicle: GarageVehicle, updated: Boolean = false, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().testTag("add-vehicle-review"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            if (updated) "Зміни авто збережено" else "Транспорт додано до гаража",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            "Перевірте профіль, отриманий від сервера. Поля без даних позначені як «Не вказано».",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        VehicleDetailsScreen(vehicle)
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().testTag("add-vehicle-finish"),
        ) { Text("Готово") }
    }
}
