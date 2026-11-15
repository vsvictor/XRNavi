package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun GarageDeleteDialog(state: GarageState, dispatch: (GarageEvent) -> Unit) {
    val vehicle = state.vehicleToDelete ?: return
    AlertDialog(
        onDismissRequest = { dispatch(GarageEvent.CancelDelete) },
        title = { Text("Видалити авто?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${vehicle.name} буде видалено з вашого гаража.")
                Text(
                    "Цю дію не можна скасувати.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (state.error.isNotEmpty()) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { dispatch(GarageEvent.ConfirmDelete) },
                enabled = !state.loading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(if (state.loading) "Видалення…" else "Видалити")
            }
        },
        dismissButton = {
            TextButton(onClick = { dispatch(GarageEvent.CancelDelete) }, enabled = !state.loading) {
                Text("Скасувати")
            }
        },
    )
}
