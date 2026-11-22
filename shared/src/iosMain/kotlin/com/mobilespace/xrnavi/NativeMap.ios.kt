@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface IosMapView {
    val view: UIView
    fun update(dark: Boolean, satellite: Boolean, revision: Int, command: MapCommand, commandId: Int, initialRecenterTarget: GeoPoint?, overlay: MapOverlay)
    fun dispose()
}

interface IosMapViewFactory {
    fun create(
        camera: MapCamera,
        status: (Int, MapLoadStatus) -> Unit,
        cameraChanged: (MapCamera) -> Unit,
        initialRecenterHandled: (Int) -> Unit,
    ): IosMapView
}

actual class PlatformMapFactory actual constructor() {
    internal actual var locations: StateFlow<DeviceFix?> = MutableStateFlow(null)
    internal var native: IosMapViewFactory? = null
        private set

    constructor(factory: IosMapViewFactory) : this() { native = factory }
}

@Composable
internal actual fun PlatformNativeMap(
    factory: PlatformMapFactory,
    state: MapState,
    overlay: MapOverlay,
    dark: Boolean,
    dispatch: (MapEvent) -> Unit,
    modifier: Modifier,
) {
    val nativeFactory = checkNotNull(factory.native) { "Mapbox native factory must be injected by the iOS host." }
    val views = androidx.compose.runtime.remember(factory) { mutableMapOf<UIView, IosMapView>() }
    val latestDispatch = androidx.compose.runtime.rememberUpdatedState(dispatch)
    UIKitView(
        factory = {
            nativeFactory.create(
                state.camera,
                { revision, status -> latestDispatch.value(MapEvent.NativeStatus(revision, status)) },
                { camera -> latestDispatch.value(MapEvent.CameraChanged(camera)) },
                { id -> latestDispatch.value(MapEvent.InitialRecenterHandled(id)) },
            ).also { views[it.view] = it }.view
        },
        modifier = modifier,
        update = { views[it]?.update(dark, state.satellite, state.revision, state.command, state.commandId, state.initialRecenterTarget, overlay) },
        onRelease = { views.remove(it)?.dispose() },
    )
}
