package com.mobilespace.xrnavi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleCatalogClassOptions(
    classes: List<VehicleClassItem>,
    selectedClassId: String?,
    enabled: Boolean,
    onSelected: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .testTag("add-vehicle-class-options"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        classes.forEach { item ->
            val selected = selectedClassId == item.id
            Surface(
                modifier = Modifier.width(116.dp)
                    .testTag("add-vehicle-class-${item.id}")
                    .clickable(
                        enabled = enabled,
                        role = Role.Button,
                        onClick = { onSelected(item.id) },
                    ),
                shape = RoundedCornerShape(8.dp),
                color = if (selected) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface,
            ) {
                Text(
                    item.name,
                    Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
        }
    }
}
