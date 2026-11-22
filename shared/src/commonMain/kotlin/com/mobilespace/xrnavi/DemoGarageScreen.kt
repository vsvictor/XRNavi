package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DemoGarageScreen(
    layout: AccountLayout,
    currentVehicleId: String,
    filter: GarageFilter,
    onFilterChange: (GarageFilter) -> Unit,
    onSelectVehicle: (DemoVehicleProfile) -> Unit,
    onOpenVehicleProfile: (DemoVehicleProfile) -> Unit,
    unavailable: (String) -> Unit
) {
    val vehicles = garageVehicles(filter)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GarageFilterBar(filter, onFilterChange)
        GarageSectionHeading(
            text = if (filter == GarageFilter.All) "Доступні демопрофілі"
            else "${filter.label} транспорт"
        )
        if (vehicles.isEmpty()) {
            GarageEmptyState(filter)
        } else {
            vehicles.forEach { vehicle ->
                GarageVehicleCard(
                    vehicle = vehicle,
                    layout = layout,
                    selected = currentVehicleId == vehicle.id,
                    onSelect = { onSelectVehicle(vehicle) },
                    onDetails = { onOpenVehicleProfile(vehicle) }
                )
            }
        }
        Button(
            onClick = { unavailable(garageAddUnavailableExplanation) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Додати транспорт · не підключено", fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        AccountBody(garageCorporateUnavailableExplanation)
    }
}
