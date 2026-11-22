package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

expect class PlatformMapFactory() {
    internal var locations: StateFlow<DeviceFix?>
}

@Composable
internal fun NativeMap(
    factory: PlatformMapFactory,
    state: MapState,
    overlay: MapOverlay,
    dark: Boolean,
    dispatch: (MapEvent) -> Unit,
    modifier: Modifier,
) {
    val location by factory.locations.collectAsStateWithLifecycle()
    PlatformNativeMap(factory, state, overlay.withTrackerLocation(location), dark, dispatch, modifier)
}

@Composable
internal expect fun PlatformNativeMap(
    factory: PlatformMapFactory,
    state: MapState,
    overlay: MapOverlay,
    dark: Boolean,
    dispatch: (MapEvent) -> Unit,
    modifier: Modifier,
)
