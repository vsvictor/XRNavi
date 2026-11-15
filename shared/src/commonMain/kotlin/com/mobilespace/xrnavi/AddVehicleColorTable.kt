package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddVehicleColorTable(
    field: VehicleConfigurationField,
    equipment: VehicleEquipmentState,
    onSelected: (String) -> Unit,
) {
    Text(field.label, style = MaterialTheme.typography.titleSmall)
    val options = equipment.options(field)
    if (options.isEmpty()) Text("Немає доступних варіантів", style = MaterialTheme.typography.bodySmall)
    FlowRow(
        Modifier.fillMaxWidth().selectableGroup().testTag("configuration-${field.name.lowercase()}"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        options.forEach { option ->
            AddVehicleColorSwatch(
                option,
                selected = equipment.selections[field]?.id == option.id,
                onSelected = { onSelected(option.id) },
            )
        }
    }
}
