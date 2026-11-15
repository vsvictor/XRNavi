package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleColorSwatch(
    option: VehicleConfigurationOption,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    val color = option.swatchColor()
    if (color == null) {
        Text("Колір недоступний", style = MaterialTheme.typography.bodySmall)
        return
    }
    Box(
        Modifier.size(50.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                if (selected) 3.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                CircleShape,
            )
            .selectable(selected, role = Role.RadioButton, onClick = onSelected)
            .semantics { contentDescription = option.name },
    )
}
