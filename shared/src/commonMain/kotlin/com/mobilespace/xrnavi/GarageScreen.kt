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
internal fun GarageScreen(
    state: GarageState,
    dispatch: (GarageEvent) -> Unit,
    onAddVehicle: () -> Unit,
    onOpenDetails: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth().testTag("garage-screen"), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Гараж", style = MaterialTheme.typography.titleLarge)
        if (state.loading) CircularProgressIndicator(Modifier.testTag("garage-loading"))
        if (state.error.isNotEmpty()) {
            Text(state.error, Modifier.testTag("garage-error"), color = MaterialTheme.colorScheme.error)
            OutlinedButton(
                onClick = { dispatch(GarageEvent.Load) },
                modifier = Modifier.testTag("garage-retry"),
            ) { Text("Повторити") }
        }
        if (state.loaded && state.vehicles.isEmpty()) {
            Text("У гаражі ще немає авто.", Modifier.testTag("garage-empty"))
        }
        state.vehicles.forEach { vehicle ->
            val active = vehicle.id == state.activeVehicleId
            Card(Modifier.fillMaxWidth().testTag("garage-vehicle-${vehicle.id}")) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(vehicle.name, style = MaterialTheme.typography.titleMedium)
                    Text("${vehicle.category} · ${vehicle.verification.label()}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (active) Text("Активне авто", Modifier.testTag("garage-active-${vehicle.id}"))
                        else Button(
                            onClick = { dispatch(GarageEvent.Select(vehicle.id)) },
                            enabled = !state.loading,
                            modifier = Modifier.testTag("garage-select-${vehicle.id}"),
                        ) { Text("Вибрати") }
                        OutlinedButton(
                            onClick = { onOpenDetails(vehicle.id) },
                            modifier = Modifier.testTag("garage-details-${vehicle.id}"),
                        ) { Text("Деталі") }
                    }
                }
            }
        }
        Button(
            onClick = onAddVehicle,
            modifier = Modifier.testTag("garage-add"),
        ) { Text("Додати авто") }
    }
}
