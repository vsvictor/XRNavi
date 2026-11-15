package com.mobilespace.xrnavi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleCatalogClassTabs(
    selectedGroup: VehicleCatalogGroup,
    availableGroups: List<VehicleCatalogGroup>,
    enabled: Boolean,
    onSelected: (VehicleCatalogGroup) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("add-vehicle-class-tabs"),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            val groups = listOf(
                VehicleCatalogGroup.Passenger,
                VehicleCatalogGroup.Cargo,
                VehicleCatalogGroup.Special,
            ) + if (VehicleCatalogGroup.Other in availableGroups) listOf(VehicleCatalogGroup.Other) else emptyList()
            groups.forEach { group ->
                val selected = selectedGroup == group
                Surface(
                    modifier = Modifier.weight(1f)
                        .testTag("add-vehicle-group-${group.name.lowercase()}")
                        .clickable(
                            enabled = enabled,
                            role = Role.Button,
                            onClick = { onSelected(group) },
                        ),
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            group.label,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
