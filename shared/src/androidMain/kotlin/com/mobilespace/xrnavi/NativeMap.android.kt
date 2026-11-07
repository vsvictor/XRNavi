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
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

actual class PlatformMapFactory actual constructor()

@Composable
internal actual fun NativeMap(
    factory: PlatformMapFactory,
    state: MapState,
    overlay: MapOverlay,
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
        bridge.update(state, overlay, dark)
    })
}

private class AndroidMapBridge(private val view: MapView) {
    var revision = -1
        private set
    private var commandId = -1
    private var initialized = false

    private var styleReady = false
    private var overlay = MapOverlay()
    private var fittedRoute: List<GeoPoint> = emptyList()

    fun update(state: MapState, overlay: MapOverlay, dark: Boolean) {
        if (!initialized) {
            view.mapboxMap.setCamera(state.camera.options())
            commandId = state.commandId
            initialized = true
        }
        if (revision != state.revision) {
            revision = state.revision
            styleReady = false
            val style = when {
                state.satellite -> Style.SATELLITE_STREETS
                dark -> Style.DARK
                else -> Style.LIGHT
            }
            val expected = revision
            view.mapboxMap.loadStyle(style) {
                if (expected == revision) {
                    styleReady = true
                    drawOverlay()
                }
            }
        }
        if (this.overlay != overlay) {
            this.overlay = overlay
            drawOverlay()
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
    private fun drawOverlay() {
        if (!styleReady) return
        val style = view.mapboxMap.style ?: return
        listOf("xrnavi-route-casing", "xrnavi-route-line", "xrnavi-pins").forEach { style.removeStyleLayer(it) }
        listOf("xrnavi-route-source", "xrnavi-pins-source").forEach { style.removeStyleSource(it) }
        if (overlay.route.size >= 2) {
            val line = LineString.fromLngLats(overlay.route.map { Point.fromLngLat(it.longitude, it.latitude) })
            style.addSource(geoJsonSource("xrnavi-route-source") { geometry(line) })
            style.addLayer(lineLayer("xrnavi-route-casing", "xrnavi-route-source") {
                lineColor("#FFFFFF"); lineWidth(9.0); lineCap(LineCap.ROUND); lineJoin(LineJoin.ROUND)
            })
            style.addLayer(lineLayer("xrnavi-route-line", "xrnavi-route-source") {
                lineColor("#2F7BFF"); lineWidth(6.0); lineCap(LineCap.ROUND); lineJoin(LineJoin.ROUND)
            })
        }
        val pins = listOfNotNull(
            overlay.origin?.let { it to "#FFFFFF" },
            overlay.destination?.let { it to "#E5484D" },
        )
        if (pins.isNotEmpty()) {
            val features = FeatureCollection.fromFeatures(pins.map { (point, color) ->
                Feature.fromGeometry(Point.fromLngLat(point.longitude, point.latitude)).also {
                    it.addStringProperty("pin-color", color)
                }
            })
            style.addSource(geoJsonSource("xrnavi-pins-source") { featureCollection(features) })
            style.addLayer(circleLayer("xrnavi-pins", "xrnavi-pins-source") {
                circleRadius(8.0)
                circleColor(Expression.toColor(Expression.get("pin-color")))
                circleStrokeColor("#172033"); circleStrokeWidth(3.0)
            })
        }
        if (overlay.route.size >= 2 && overlay.route != fittedRoute) {
            fittedRoute = overlay.route
            val density = view.resources.displayMetrics.density
            val camera = view.mapboxMap.cameraForCoordinates(
                overlay.route.map { Point.fromLngLat(it.longitude, it.latitude) },
                CameraOptions.Builder().build(),
                EdgeInsets(180.0 * density, 48.0 * density, 280.0 * density, 48.0 * density),
                null, null,
            )
            view.mapboxMap.setCamera(camera)
        } else if (overlay.route.isEmpty()) fittedRoute = emptyList()
    }
}

private fun MapCamera.options() = CameraOptions.Builder()
    .center(Point.fromLngLat(longitude, latitude))
    .zoom(zoom).bearing(bearing).pitch(pitch).build()
