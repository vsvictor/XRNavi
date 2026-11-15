package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun AddVehicleSaveButton(state: AddVehicleState, onSave: () -> Unit) {
    if (state.error.isNotEmpty()) Text(state.error, color = MaterialTheme.colorScheme.error)
    Button(
        onClick = onSave,
        enabled = state.canSubmit,
        modifier = Modifier.fillMaxWidth().testTag("add-vehicle-submit"),
    ) { Text(if (state.editingVehicle != null) "Зберегти зміни" else "Зберегти авто") }
}
