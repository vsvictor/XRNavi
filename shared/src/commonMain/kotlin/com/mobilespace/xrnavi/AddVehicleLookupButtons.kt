package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleLookupButtons(enabled: Boolean, onVin: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
            onClick = onVin,
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("add-vehicle-vin"),
        ) {
            AccountIcon(AccountIcon.Search, Modifier.size(18.dp), tint = LocalContentColor.current)
            Spacer(Modifier.width(4.dp))
            Text("VIN", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
        }
        Button(
            onClick = {},
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("add-vehicle-scan"),
        ) {
            AccountIcon(AccountIcon.Xr, Modifier.size(18.dp), tint = LocalContentColor.current)
            Spacer(Modifier.width(4.dp))
            Text("Scan", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
        }
        Button(
            onClick = {},
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("add-vehicle-smartcar"),
        ) {
            AccountIcon(AccountIcon.Driver, Modifier.size(18.dp), tint = LocalContentColor.current)
            Spacer(Modifier.width(4.dp))
            Text("SmartCar", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
        }
    }
}
