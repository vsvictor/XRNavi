package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleCatalogMakeList(
    makes: List<VehicleMake>,
    enabled: Boolean,
    onSelected: (VehicleMake) -> Unit,
) {
    Surface(
        modifier = Modifier.testTag("add-vehicle-make-list"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            makes.forEach { make ->
                AddVehicleCatalogMakeCard(
                    make = make,
                    enabled = enabled,
                    onClick = { onSelected(make) },
                )
            }
        }
    }
}
