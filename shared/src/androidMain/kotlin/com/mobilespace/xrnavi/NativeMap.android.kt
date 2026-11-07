package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

actual class PlatformMapFactory actual constructor()

@Composable
internal actual fun NativeMap(
    factory: PlatformMapFactory,
    state: MapState,
    dark: Boolean,
    dispatch: (MapEvent) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val latestDispatch = rememberUpdatedState(dispatch)
    val mapView = remember(factory, context) { MapView(context) }
    val bridge = remember(mapView) { AndroidMapBridge(mapView) }
    DisposableEffect(mapView, owner) {
        val loaded = mapView.mapboxMap.subscribeMapLoaded {
            latestDispatch.value(MapEvent.NativeStatus(bridge.revision, MapLoadStatus.Ready))
        }
        val error = mapView.mapboxMap.subscribeMapLoadingError {
            latestDispatch.value(MapEvent.NativeStatus(bridge.revision, MapLoadStatus.Failed))
        }
        val camera = mapView.mapboxMap.subscribeCameraChanged {
            val position = mapView.mapboxMap.cameraState
            latestDispatch.value(MapEvent.CameraChanged(MapCamera(
                position.center.latitude(), position.center.longitude(),
                position.zoom, position.bearing, position.pitch,
            )))
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        onDispose {
            owner.lifecycle.removeObserver(observer)
            loaded.cancel()
            error.cancel()
            camera.cancel()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    AndroidView(factory = { mapView }, modifier = modifier, update = {
        bridge.update(state, dark)
    })
}

private class AndroidMapBridge(private val view: MapView) {
    var revision = -1
        private set
    private var commandId = -1
    private var initialized = false

    fun update(state: MapState, dark: Boolean) {
        if (!initialized) {
            view.mapboxMap.setCamera(state.camera.options())
            commandId = state.commandId
            initialized = true
        }
        if (revision != state.revision) {
            revision = state.revision
            view.mapboxMap.loadStyle(if (dark) Style.DARK else Style.LIGHT)
        }
        if (commandId != state.commandId) {
            commandId = state.commandId
            val camera = view.mapboxMap.cameraState
            when (state.command) {
                MapCommand.ZoomIn -> view.mapboxMap.setCamera(
                    CameraOptions.Builder().zoom((camera.zoom + 1).coerceAtMost(22.0)).build(),
                )
                MapCommand.ZoomOut -> view.mapboxMap.setCamera(
                    CameraOptions.Builder().zoom((camera.zoom - 1).coerceAtLeast(0.0)).build(),
                )
                MapCommand.Recenter -> view.mapboxMap.setCamera(MapCamera().options())
                MapCommand.None -> Unit
            }
        }
    }
}

private fun MapCamera.options() = CameraOptions.Builder()
    .center(Point.fromLngLat(longitude, latitude))
    .zoom(zoom).bearing(bearing).pitch(pitch).build()
