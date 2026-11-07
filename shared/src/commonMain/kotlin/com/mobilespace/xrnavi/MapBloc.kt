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

internal data class MapState(
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
    data class NativeStatus(val revision: Int, val status: MapLoadStatus) : MapEvent
    data class CameraChanged(val camera: MapCamera) : MapEvent
    data class Control(val command: MapCommand) : MapEvent
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
            is MapEvent.NativeStatus -> if (event.revision == current.revision) {
                current.copy(status = event.status)
            } else current
            is MapEvent.CameraChanged -> current.copy(camera = event.camera)
            is MapEvent.Control -> current.copy(
                command = event.command, commandId = current.commandId + 1,
                notice = if (event.command == MapCommand.Recenter) {
                    "Центр: Київ. Геолокацію не підключено; це не ваша поточна позиція."
                } else current.notice,
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
