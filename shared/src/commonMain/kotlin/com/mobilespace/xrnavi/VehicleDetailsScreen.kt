package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
            Modifier.fillMaxWidth().fillMaxHeight().testTag("vehicle-details"),
            horizontalArrangement = Arrangement.spacedBy(if (compactLandscape) 12.dp else 24.dp),
        ) {
            VehicleDetailsSidebar(
                vehicle = vehicle,
                modifier = Modifier.width(if (compactLandscape) 176.dp else 304.dp),
                imageHeight = if (compactLandscape) 104.dp else 180.dp,
            )
            Column(
                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
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
                imageHeight = 196.dp,
                horizontal = false,
            )
            VehicleDetailsContent(vehicle)
        }
    }
}
