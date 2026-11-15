package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DemoGarageLandscape(
    layout: AccountLayout,
    currentVehicleId: String,
    filter: GarageFilter,
    vehicles: List<DemoVehicleProfile>,
    onFilterChange: (GarageFilter) -> Unit,
    onSelectVehicle: (DemoVehicleProfile) -> Unit,
    onOpenVehicleProfile: (DemoVehicleProfile) -> Unit,
    unavailable: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().testTag("demo-garage-landscape"),
        horizontalArrangement = Arrangement.spacedBy(if (layout.tablet) 20.dp else 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val featuredVehicle = demoVehicleById(currentVehicleId) ?: demoVehicle
        Column(
            Modifier.weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.fillMaxWidth().height(if (layout.tablet) 156.dp else 112.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("demo-garage-landscape-hero"),
                contentAlignment = Alignment.Center,
            ) {
                DemoVehicleImage(
                    vehicle = featuredVehicle,
                    modifier = Modifier.fillMaxWidth()
                        .height(if (layout.tablet) 156.dp else 112.dp),
                    contentDescription = "Демонстраційне зображення ${featuredVehicle.name}",
                    grayscale = true,
                )
            }
            Text(
                "Ваш транспорт",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
        Column(
            Modifier.weight(1.6f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GarageFilterBar(filter, onFilterChange)
            Row(
                Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("ТРАНСПОРТ", style = MaterialTheme.typography.titleSmall)
                Text(
                    "${vehicles.size} авто",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("demo-garage-vehicle-count"),
                )
            }
            if (vehicles.isEmpty()) {
                GarageEmptyState(filter)
            } else {
                vehicles.forEach { vehicle ->
                    GarageVehicleCard(
                        vehicle = vehicle,
                        layout = layout,
                        selected = currentVehicleId == vehicle.id,
                        onSelect = { onSelectVehicle(vehicle) },
                        onDetails = { onOpenVehicleProfile(vehicle) },
                    )
                }
            }
            Button(
                onClick = { unavailable(garageAddUnavailableExplanation) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(
                    "Додати транспорт · не підключено",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
