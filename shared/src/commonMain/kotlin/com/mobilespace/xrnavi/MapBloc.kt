package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MapLoadStatus { Loading, Ready, Failed }
enum class MapCommand { None, ZoomIn, ZoomOut, Recenter }

data class MapCamera(
    val latitude: Double = 50.4501,
    val longitude: Double = 30.5234,
    val zoom: Double = 11.0,
    val bearing: Double = 0.0,
    val pitch: Double = 0.0,
)

data class MapOverlay(
    val route: List<GeoPoint> = emptyList(),
    val origin: GeoPoint? = null,
    val destination: GeoPoint? = null,
    val user: GeoPoint? = null,
) {
    val routeCoordinates: List<Double> get() = route.flatMap { listOf(it.latitude, it.longitude) }
}

internal data class MapState(
    val satellite: Boolean = false,
    val status: MapLoadStatus = MapLoadStatus.Loading,
    val revision: Int = 0,
    val command: MapCommand = MapCommand.None,
    val commandId: Int = 0,
    val camera: MapCamera = MapCamera(),
    val notice: String = "",
)

internal sealed interface MapEvent {
    data object Retry : MapEvent
    data object StyleChanged : MapEvent
    data object ToggleLayer : MapEvent
    data class NativeStatus(val revision: Int, val status: MapLoadStatus) : MapEvent
    data class CameraChanged(val camera: MapCamera) : MapEvent
    data class Control(val command: MapCommand, val reliableUser: Boolean = false) : MapEvent
    data object SearchRequested : MapEvent
    data object PlacesRequested : MapEvent
    data object DismissNotice : MapEvent
}

internal class MapBloc {
    private val mutableState = MutableStateFlow(MapState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: MapEvent) {
        val current = mutableState.value
        mutableState.value = when (event) {
            MapEvent.Retry, MapEvent.StyleChanged -> current.copy(
                status = MapLoadStatus.Loading, revision = current.revision + 1,
            )
            MapEvent.ToggleLayer -> current.copy(
                satellite = !current.satellite, status = MapLoadStatus.Loading, revision = current.revision + 1,
            )
            is MapEvent.NativeStatus -> if (event.revision == current.revision) {
                current.copy(status = event.status)
            } else current
            is MapEvent.CameraChanged -> current.copy(camera = event.camera)
            is MapEvent.Control -> current.copy(
                command = event.command, commandId = current.commandId + 1,
                notice = if (event.command == MapCommand.Recenter && !event.reliableUser) {
                    "Центр: Київ. Це не ваша поточна позиція: надійного визначення місцезнаходження немає."
                } else if (event.command == MapCommand.Recenter) "" else current.notice,
            )
            MapEvent.SearchRequested -> current.copy(
                notice = "Пошук адрес і побудова маршрутів ще не підключені. Мапа та її жести доступні.",
            )
            MapEvent.PlacesRequested -> current.copy(
                notice = "Онлайн-каталог місць зупинки ще не підключений. Демо-місця тут не показуються.",
            )
            MapEvent.DismissNotice -> current.copy(notice = "")
        }
    }
}
