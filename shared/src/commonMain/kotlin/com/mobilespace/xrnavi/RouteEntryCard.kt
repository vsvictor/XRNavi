package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

@Composable
internal fun RouteEntryCard(
    origin: String,
    destination: String,
    routeStopsCount: Int,
    onOriginClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onLocationClick: () -> Unit,
    onRouteStopsClick: () -> Unit,
    modifier: Modifier
) {
    val dark = isDarkXrnaviTheme()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(role = Role.Button, onClick = onOriginClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = origin.ifBlank { "Моє місцезнаходження" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    maxLines = 1,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FigmaIcon(
                    light = Res.drawable.map_swap_light,
                    dark = Res.drawable.map_swap_dark,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(role = Role.Button, onClick = onLocationClick),
                    contentDescription = "Використати місцезнаходження"
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(role = Role.Button, onClick = onDestinationClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FigmaIcon(
                    light = Res.drawable.map_destination_marker_light,
                    dark = Res.drawable.map_destination_marker_dark,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = destination.ifBlank { "Вибрати адресу призначення" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    maxLines = 1,
                    fontSize = 13.sp,
                    fontWeight = if (destination.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (destination.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                FigmaIcon(
                    light = Res.drawable.map_edit_light,
                    dark = Res.drawable.map_edit_dark,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(role = Role.Button, onClick = onDestinationClick),
                    contentDescription = "Редагувати адресу призначення"
                )
            }
            Text(
                "Зупинки маршруту · $routeStopsCount",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onRouteStopsClick)
                    .padding(start = 20.dp, top = 7.dp, bottom = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
