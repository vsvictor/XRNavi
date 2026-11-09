package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

@Composable
internal fun SearchVehicleCard(
    vehicleClass: String,
    vehicle: DemoVehicleProfile?,
    mode: String,
    onModeChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (vehicleClassUnavailableMessage(vehicleClass) == null && vehicle != null) Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DemoVehicleImage(
                vehicle = vehicle,
                contentDescription = "Демонстраційне зображення ${vehicle.name}",
                thumbnail = true,
                modifier = Modifier
                    .size(width = 94.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    vehicle.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    vehicle.specification,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                vehicle.configurationName?.let {
                    Text(
                        "Конфігурація: $it",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FigmaIcon(
                light = Res.drawable.search_car_light,
                dark = Res.drawable.search_car_dark,
                modifier = Modifier.size(18.dp),
                contentDescription = "Транспорт"
            )
        } else {
            StatusCard(
                title = "Профіль транспорту недоступний",
                message = vehicleClassUnavailableMessage(vehicleClass).orEmpty()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Особистий", "Корпоративний").forEach { item ->
                val selected = item == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable(role = Role.Button) { onModeChange(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
