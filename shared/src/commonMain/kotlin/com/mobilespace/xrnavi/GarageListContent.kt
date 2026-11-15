package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun GarageListContent(
    state: GarageState,
    dispatch: (GarageEvent) -> Unit,
    onAddVehicle: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onEditVehicle: (GarageVehicle) -> Unit,
    showTitle: Boolean,
) {
    GarageDeleteDialog(state, dispatch)
    if (showTitle) {
        Text("Мій гараж", style = MaterialTheme.typography.titleLarge)
    }
    GarageCategoryFilterBar(
        categories = state.categories,
        selectedCategory = state.selectedCategory,
        onSelected = { dispatch(GarageEvent.FilterChanged(it)) },
    )
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("ТРАНСПОРТ", style = MaterialTheme.typography.titleSmall)
        Text(
            if (state.loaded) "${state.vehicles.size} авто" else "—",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    if (state.error.isNotEmpty()) {
        Text(
            state.error,
            Modifier.fillMaxWidth().testTag("garage-error"),
            color = MaterialTheme.colorScheme.error,
        )
        OutlinedButton(
            onClick = { dispatch(GarageEvent.Load) },
            modifier = Modifier.testTag("garage-retry"),
        ) { Text("Повторити") }
    }
    if (state.loaded && state.vehicles.isEmpty() && !state.loadFailed) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 24.dp).testTag("garage-empty"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Гараж порожній", style = MaterialTheme.typography.titleMedium)
            Text(
                "Додайте транспорт із каталогу, щоб він з’явився тут.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onAddVehicle,
                modifier = Modifier.testTag("garage-empty-add")
            ) {
                Text("Додати транспорт")
            }
        }
    }
    if (state.loaded && state.vehicles.isNotEmpty() && state.visibleVehicles.isEmpty()) {
        Text(
            "У цій категорії транспорту немає.",
            Modifier.fillMaxWidth().testTag("garage-filter-empty").padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    state.visibleVehicles.forEach { vehicle ->
        AuthenticatedGarageVehicleCard(
            vehicle = vehicle,
            active = vehicle.id == state.activeVehicleId,
            enabled = !state.loading,
            onSelect = { dispatch(GarageEvent.Select(vehicle.id)) },
            onDetails = { onOpenDetails(vehicle.id) },
            onEdit = { onEditVehicle(vehicle) },
            onDelete = { dispatch(GarageEvent.RequestDelete(vehicle.id)) },
        )
    }
    if (state.loaded && state.vehicles.isNotEmpty()) {
        OutlinedButton(
            onClick = onAddVehicle,
            modifier = Modifier.fillMaxWidth().testTag("garage-add"),
        ) { Text("Додати транспорт") }
    }
}
