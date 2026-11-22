package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun RouteSheet(
    state: RouteState,
    dispatch: (RouteEvent) -> Unit,
    reliableOrigin: Boolean,
    modifier: Modifier,
) {
    var drag by remember { mutableStateOf(0f) }
    val dragState = rememberDraggableState { drag += it }
    Surface(
        modifier.fillMaxWidth().testTag("route-sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.fillMaxWidth().height(28.dp)
                    .draggable(dragState, Orientation.Vertical, onDragStopped = {
                        when {
                            drag < -24f -> dispatch(RouteEvent.SetSheet(nextSheetLevelUp(state.sheet)))
                            drag > 24f -> dispatch(RouteEvent.SetSheet(previousSheetLevel(state.sheet)))
                        }
                        drag = 0f
                    })
                    .clickable { dispatch(RouteEvent.SetSheet(nextSheetLevel(state.sheet))) }
                    .semantics { contentDescription = "Змінити висоту панелі маршруту" }
                    .testTag("route-sheet-handle"),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.width(44.dp).height(5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(3.dp)),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (state.sheet != SheetLevel.Expanded) {
                    TextButton(
                        onClick = { dispatch(RouteEvent.SetSheet(nextSheetLevelUp(state.sheet))) },
                        modifier = Modifier.testTag("route-sheet-expand"),
                    ) { Text("Розгорнути") }
                }
                if (state.sheet != SheetLevel.Collapsed) {
                    TextButton(
                        onClick = { dispatch(RouteEvent.SetSheet(previousSheetLevel(state.sheet))) },
                        modifier = Modifier.testTag("route-sheet-collapse"),
                    ) { Text("Згорнути") }
                }
            }
            if (state.routeStale) {
                Text(
                    "Маршрут неактуальний: точки змінено, новий маршрут ще не побудовано.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("route-stale"),
                )
            }
            when (val status = state.status) {
                RouteStatus.Idle -> Text(
                    if (state.origin == null && state.destination == null) "Оберіть початок і призначення, щоб побудувати маршрут. Це не помилка."
                    else if (state.origin == null) "Оберіть початок маршруту."
                    else "Оберіть призначення.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("route-idle"),
                )
                RouteStatus.Loading -> Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(22.dp).testTag("route-loading"))
                    Text("Будуємо маршрут…")
                }
                is RouteStatus.Failed -> {
                    Text(status.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("route-error"))
                    TextButton(onClick = { dispatch(RouteEvent.Retry) }) { Text("Повторити") }
                }
                is RouteStatus.Ready -> ReadyContent(state, status.route, reliableOrigin, dispatch)
            }
        }
    }
}
