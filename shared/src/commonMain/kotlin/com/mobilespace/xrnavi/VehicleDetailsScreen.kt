package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun VehicleDetailsScreen(
    vehicle: GarageVehicle,
    landscape: Boolean = false,
    compactLandscape: Boolean = false,
) {
    if (landscape) {
        Row(
            Modifier.fillMaxWidth().testTag("vehicle-details"),
            horizontalArrangement = Arrangement.spacedBy(if (compactLandscape) 12.dp else 24.dp),
        ) {
            VehicleDetailsSidebar(
                vehicle = vehicle,
                modifier = Modifier.weight(0.46f),
            )
            Column(
                Modifier.weight(0.54f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                VehicleDetailsContent(vehicle)
            }
        }
    } else {
        Column(
            Modifier.fillMaxWidth().testTag("vehicle-details"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VehicleDetailsSidebar(
                vehicle = vehicle,
                modifier = Modifier.fillMaxWidth(),
            )
            VehicleDetailsContent(vehicle)
        }
    }
}
