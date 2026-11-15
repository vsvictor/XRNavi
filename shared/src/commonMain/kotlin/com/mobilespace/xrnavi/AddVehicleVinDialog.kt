package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleVinDialog(state: AddVehicleState, dispatch: (AddVehicleEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { dispatch(AddVehicleEvent.DismissVinDialog) },
        title = { Text("Пошук за VIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AddVehicleCatalogSearch(
                    value = state.vinQuery,
                    placeholder = "Введіть VIN авто",
                    onValueChange = { dispatch(AddVehicleEvent.VinChanged(it)) },
                    enabled = !state.vinLoading,
                )
                if (state.vinError.isNotEmpty()) Text(
                    state.vinError,
                    modifier = Modifier.testTag("add-vehicle-vin-error"),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { dispatch(AddVehicleEvent.SearchVin) },
                enabled = state.canSearchVin,
                modifier = Modifier.testTag("add-vehicle-vin-search"),
            ) { Text(if (state.vinLoading) "Пошук…" else "Пошук") }
        },
        dismissButton = {
            TextButton(onClick = { dispatch(AddVehicleEvent.DismissVinDialog) }) { Text("Скасувати") }
        },
    )
}
