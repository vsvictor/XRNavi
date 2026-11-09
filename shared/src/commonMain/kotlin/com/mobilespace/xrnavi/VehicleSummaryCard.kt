package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun VehicleSummaryCard(vehicle: DemoVehicleProfile) {
    Row(
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
                lineHeight = 25.sp,
                color = MaterialTheme.colorScheme.onSurface
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
        Text(
            "⌄",
            modifier = Modifier.padding(start = 4.dp),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
