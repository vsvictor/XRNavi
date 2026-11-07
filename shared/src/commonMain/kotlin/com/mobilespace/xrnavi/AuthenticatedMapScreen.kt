package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.map_destination_marker_dark
import xrnavi.shared.generated.resources.map_destination_marker_light
import xrnavi.shared.generated.resources.map_layers_dark
import xrnavi.shared.generated.resources.map_layers_light
import xrnavi.shared.generated.resources.map_locate_dark
import xrnavi.shared.generated.resources.map_locate_light
import xrnavi.shared.generated.resources.map_swap_dark
import xrnavi.shared.generated.resources.map_swap_light

@Composable
internal fun AuthenticatedMapScreen(
    factory: PlatformMapFactory,
    state: MapState,
    dispatch: (MapEvent) -> Unit,
    routeState: RouteState,
    routeDispatch: (RouteEvent) -> Unit,
    expired: Boolean,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isDarkXrnaviTheme()
    LaunchedEffect(dark) { dispatch(MapEvent.StyleChanged) }
    val ready = routeState.status as? RouteStatus.Ready
    val overlay = remember(routeState.origin, routeState.destination, ready) {
        MapOverlay(
            route = ready?.route?.points.orEmpty(),
            origin = routeState.origin?.point,
            destination = routeState.destination?.point,
        )
    }
    val near = GeoPoint(state.camera.latitude, state.camera.longitude)
    Box(modifier.testTag("authenticated-map")) {
        NativeMap(factory, state, overlay, dark, dispatch, Modifier.fillMaxSize())
        Column(
            Modifier.align(Alignment.TopCenter).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (routeState.editing == null) {
                RouteCard(routeState, routeDispatch)
            } else {
                AddressSearchCard(routeState, near, routeDispatch)
            }
            MapMessages(state, routeState, expired, dispatch, routeDispatch, onProfile)
        }
        Column(
            Modifier.align(Alignment.CenterEnd).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapControlButton(
                Res.drawable.map_layers_light, Res.drawable.map_layers_dark,
                if (state.satellite) "Схема мапи" else "Супутник",
            ) { dispatch(MapEvent.ToggleLayer) }
            MapControlButton(
                Res.drawable.map_locate_light, Res.drawable.map_locate_dark, "Центр мапи",
            ) { dispatch(MapEvent.Control(MapCommand.Recenter)) }
        }
        RouteSheet(
            routeState, routeDispatch,
            Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun MapMessages(
    state: MapState,
    routeState: RouteState,
    expired: Boolean,
    dispatch: (MapEvent) -> Unit,
    routeDispatch: (RouteEvent) -> Unit,
    onProfile: () -> Unit,
) {
    val notice = routeState.notice.ifEmpty { state.notice }
    if (state.status == MapLoadStatus.Ready && notice.isEmpty() && !expired) return
    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 3.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (state.status) {
                MapLoadStatus.Loading -> {
                    CircularProgressIndicator(Modifier.testTag("map-loading"))
                    Text("Завантаження мапи…")
                }
                MapLoadStatus.Failed -> {
                    Text("Не вдалося завантажити мапу. Перевірте мережу та налаштування Mapbox.")
                    TextButton(onClick = { dispatch(MapEvent.Retry) }) { Text("Повторити") }
                }
                MapLoadStatus.Ready -> Unit
            }
            if (expired) {
                Text("Сесія завершилась. Онлайн-дані акаунта недоступні; мапа залишається доступною.")
                TextButton(onClick = onProfile) { Text("Перейти до профілю") }
            }
            if (notice.isNotEmpty()) {
                Text(notice)
                TextButton(onClick = {
                    dispatch(MapEvent.DismissNotice)
                    routeDispatch(RouteEvent.DismissNotice)
                }) { Text("Зрозуміло") }
            }
        }
    }
}

@Composable
private fun RouteCard(state: RouteState, dispatch: (RouteEvent) -> Unit) {
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

@Composable
private fun PlaceText(name: String?, placeholder: String, modifier: Modifier) {
    Text(
        name ?: placeholder,
        modifier.padding(start = 10.dp, end = 8.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 13.sp,
        fontWeight = if (name == null) FontWeight.Normal else FontWeight.SemiBold,
        color = if (name == null) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun AddressSearchCard(state: RouteState, near: GeoPoint, dispatch: (RouteEvent) -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val title = if (state.editing == RouteField.Origin) "Звідки" else "Куди"
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { dispatch(RouteEvent.QueryChanged(it, near)) },
                    modifier = Modifier.weight(1f).focusRequester(focus).testTag("route-search-input"),
                    label = { Text(title) },
                    singleLine = true,
                )
                TextButton(
                    onClick = { dispatch(RouteEvent.CloseSearch) },
                    modifier = Modifier.testTag("route-search-close"),
                ) { Text("Закрити") }
            }
            if (state.searching) CircularProgressIndicator(Modifier.size(20.dp))
            if (state.searchError.isNotEmpty()) {
                Text(state.searchError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Column(Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState())) {
                state.suggestions.forEach { place ->
                    Column(
                        Modifier.fillMaxWidth()
                            .clickable { dispatch(RouteEvent.SelectPlace(place)) }
                            .padding(vertical = 8.dp)
                            .semantics { contentDescription = "${place.name}. ${place.address}" }
                            .testTag("route-suggestion"),
                    ) {
                        Text(place.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (place.address != place.name) {
                            Text(
                                place.address, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RouteSheet(state: RouteState, dispatch: (RouteEvent) -> Unit, modifier: Modifier) {
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
            when (val status = state.status) {
                RouteStatus.Idle -> Text(
                    "Оберіть початок і призначення, щоб побудувати маршрут.",
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
                is RouteStatus.Ready -> ReadyContent(state, status.route, dispatch)
            }
        }
    }
}

private fun nextSheetLevelUp(level: SheetLevel) = when (level) {
    SheetLevel.Collapsed -> SheetLevel.Partial
    else -> SheetLevel.Expanded
}

@Composable
private fun ReadyContent(state: RouteState, route: PlannedRoute, dispatch: (RouteEvent) -> Unit) {
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
        onClick = { dispatch(RouteEvent.StartRequested) },
        height = 48.dp,
        cornerRadius = 14.dp,
        modifier = Modifier.fillMaxWidth().testTag("route-start"),
    )
}
