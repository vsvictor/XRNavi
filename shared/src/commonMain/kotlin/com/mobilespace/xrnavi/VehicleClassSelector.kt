package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

@Composable
internal fun VehicleClassSelector(
    selected: String,
    onSelected: (String) -> Unit,
    title: String = "Категорія демо-профілю"
) {
    val types = listOf("Легкове", "Вантажне", "Спецтехніка")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        types.forEach { type ->
            val active = selected == type
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        1.dp,
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .semantics {
                        contentDescription = type
                        this.selected = active
                    }
                    .clickable(role = Role.Button) { onSelected(type) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FigmaIcon(
                    light = Res.drawable.vehicle_car_light,
                    dark = when (type) {
                        "Вантажне" -> Res.drawable.vehicle_truck_dark
                        "Спецтехніка" -> Res.drawable.vehicle_special_dark
                        else -> Res.drawable.vehicle_car_dark
                    },
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = type,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .clearAndSetSemantics { },
                    fontSize = 9.sp,
                    color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
        }
    }
}
