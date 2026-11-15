package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun AddVehicleEngineDetailsButton(
    option: VehicleConfigurationOption,
    expanded: Boolean,
    onShow: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box {
        IconButton(
            onClick = onShow,
            modifier = Modifier.semantics { contentDescription = "Характеристики двигуна" },
        ) { Text("D") }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = false),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                engineDetailFields(option.rawData).forEach { (label, value) ->
                    Text("$label: $value")
                }
            }
        }
    }
}
