package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun AddVehicleTabs(selected: AddVehicleTab, onSelect: (AddVehicleTab) -> Unit) {
    PrimaryTabRow(
        selectedTabIndex = selected.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag("add-vehicle-tabs"),
    ) {
        AddVehicleTab.entries.forEach { tab ->
            Tab(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                modifier = Modifier.testTag("add-vehicle-tab-${tab.name.lowercase()}"),
                text = { Text(tab.label, style = MaterialTheme.typography.labelMedium, maxLines = 1) },
            )
        }
    }
}
