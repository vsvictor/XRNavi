@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView

interface IosMapView {
    val view: UIView
    fun update(dark: Boolean, satellite: Boolean, revision: Int, command: MapCommand, commandId: Int, overlay: MapOverlay)
    fun dispose()
}

interface IosMapViewFactory {
    fun create(
        camera: MapCamera,
        status: (Int, MapLoadStatus) -> Unit,
        cameraChanged: (MapCamera) -> Unit,
    ): IosMapView
}

actual class PlatformMapFactory actual constructor() {
    internal var native: IosMapViewFactory? = null
        private set

    constructor(factory: IosMapViewFactory) : this() { native = factory }
}

@Composable
internal actual fun NativeMap(
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
            ).also { views[it.view] = it }.view
        },
        modifier = modifier,
        update = { views[it]?.update(dark, state.satellite, state.revision, state.command, state.commandId, overlay) },
        onRelease = { views.remove(it)?.dispose() },
    )
}
