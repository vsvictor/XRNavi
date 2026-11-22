package com.mobilespace.xrnavi

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.map_destination_marker_dark
import xrnavi.shared.generated.resources.map_destination_marker_light
import xrnavi.shared.generated.resources.map_swap_dark
import xrnavi.shared.generated.resources.map_swap_light

@Composable
internal fun RouteCard(state: RouteState, dispatch: (RouteEvent) -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth().height(40.dp)
                    .clickable { dispatch(RouteEvent.OpenSearch(RouteField.Origin)) }
                    .testTag("route-origin"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.onSurface, CircleShape))
                PlaceText(state.origin?.name, "Вибрати адресу початку", Modifier.weight(1f))
                FigmaIcon(
                    Res.drawable.map_swap_light, Res.drawable.map_swap_dark,
                    Modifier.size(18.dp).clickable { dispatch(RouteEvent.Swap) }.testTag("route-swap"),
                    contentDescription = "Поміняти місцями початок і призначення",
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Row(
                Modifier.fillMaxWidth().height(40.dp)
                    .clickable { dispatch(RouteEvent.OpenSearch(RouteField.Destination)) }
                    .testTag("route-destination"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FigmaIcon(
                    Res.drawable.map_destination_marker_light, Res.drawable.map_destination_marker_dark,
                    Modifier.size(14.dp),
                )
                PlaceText(state.destination?.name, "Вибрати адресу призначення", Modifier.weight(1f))
            }
        }
    }
}
