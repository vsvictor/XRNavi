package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.map_layers_dark
import xrnavi.shared.generated.resources.map_layers_light
import xrnavi.shared.generated.resources.map_locate_dark
import xrnavi.shared.generated.resources.map_locate_light

@Composable
internal fun AuthenticatedMapScreen(
    factory: PlatformMapFactory,
    state: MapState,
    dispatch: (MapEvent) -> Unit,
    routeState: RouteState,
    routeDispatch: (RouteEvent) -> Unit,
    geoState: GeoState,
    geoDispatch: (GeoEvent) -> Unit,
    expired: Boolean,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isDarkXrnaviTheme()
    LaunchedEffect(dark) { dispatch(MapEvent.StyleChanged) }
    val ready = routeState.status as? RouteStatus.Ready
    val userPoint = geoState.reliableFix?.point
    val overlay = remember(routeState.origin, routeState.destination, ready, userPoint) {
        MapOverlay(
            user = userPoint,
            route = (ready?.route ?: routeState.previousRoute)?.points.orEmpty(),
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
                MapAddressSearchCard(routeState, near, geoState, routeDispatch, geoDispatch)
            }
            GeoStatusCard(
                geoState, geoDispatch,
                onManualOrigin = { routeDispatch(RouteEvent.OpenSearch(RouteField.Origin)) },
            )
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
            ) {
                geoDispatch(GeoEvent.Locate)
                dispatch(MapEvent.Control(MapCommand.Recenter, reliableUser = userPoint != null))
            }
        }
        RouteSheet(
            routeState, routeDispatch, geoState.reliableFix != null,
            Modifier.align(Alignment.BottomCenter),
        )
    }
}

internal fun nextSheetLevelUp(level: SheetLevel) = when (level) {
    SheetLevel.Collapsed -> SheetLevel.Partial
    else -> SheetLevel.Expanded
}
