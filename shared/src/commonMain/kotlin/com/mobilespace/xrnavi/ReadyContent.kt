package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ReadyContent(
    state: RouteState,
    route: PlannedRoute,
    reliableOrigin: Boolean,
    dispatch: (RouteEvent) -> Unit,
) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            formatDuration(route.durationSeconds), fontSize = 24.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("route-duration"),
        )
        Text(
            formatDistance(route.distanceMeters), fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp).testTag("route-distance"),
        )
    }
    if (state.sheet != SheetLevel.Collapsed) {
        if (route.roads.isNotBlank()) {
            Text("Через: ${route.roads}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        VehicleClassSelector(
            selected = state.vehicleClass,
            onSelected = { dispatch(RouteEvent.SelectClass(it)) },
            title = "Категорія транспорту",
        )
    }
    if (state.sheet == SheetLevel.Expanded) {
        Text(
            "Маршрут побудовано за загальним профілем авто. Гараж і параметри конкретного транспорту ще не підключені.",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    XrnaviActionButton(
        text = "Почати маршрут",
        onClick = { dispatch(RouteEvent.StartRequested(reliableOrigin)) },
        height = 48.dp,
        cornerRadius = 14.dp,
        modifier = Modifier.fillMaxWidth().testTag("route-start"),
    )
}
