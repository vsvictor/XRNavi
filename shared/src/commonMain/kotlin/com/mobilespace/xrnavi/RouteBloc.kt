package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class RouteField { Origin, Destination }

internal sealed interface RouteStatus {
    data object Idle : RouteStatus
    data object Loading : RouteStatus
    data class Ready(val route: PlannedRoute) : RouteStatus
    data class Failed(val message: String) : RouteStatus
}

internal data class RouteState(
    val origin: RoutePlace? = null,
    val destination: RoutePlace? = null,
    val editing: RouteField? = null,
    val query: String = "",
    val suggestions: List<RoutePlace> = emptyList(),
    val searching: Boolean = false,
    val searchError: String = "",
    val searchEmpty: Boolean = false,
    val originIsCurrent: Boolean = false,
    val previousRoute: PlannedRoute? = null,
    val status: RouteStatus = RouteStatus.Idle,
    val vehicleClass: String = "Легкове",
    val sheet: SheetLevel = SheetLevel.Collapsed,
    val notice: String = "",
)

internal enum class SearchPhase { Idle, Loading, Results, Empty, Error }

internal val RouteState.searchPhase: SearchPhase
    get() = when {
        query.trim().length < 2 -> SearchPhase.Idle
        searching -> SearchPhase.Loading
        searchError.isNotEmpty() -> SearchPhase.Error
        searchEmpty -> SearchPhase.Empty
        suggestions.isNotEmpty() -> SearchPhase.Results
        else -> SearchPhase.Idle
    }

/** An old route stays visible but must be marked outdated while a new one is built or failed. */
internal val RouteState.routeStale: Boolean
    get() = previousRoute != null && status !is RouteStatus.Ready

internal sealed interface RouteEvent {
    data object RetrySearch : RouteEvent
    data class UseCurrentLocation(val place: RoutePlace) : RouteEvent
    data class OpenSearch(val field: RouteField) : RouteEvent
    data object CloseSearch : RouteEvent
    data class QueryChanged(val query: String, val near: GeoPoint?) : RouteEvent
    data class SelectPlace(val place: RoutePlace) : RouteEvent
    data object Swap : RouteEvent
    data object Retry : RouteEvent
    data class SelectClass(val vehicleClass: String) : RouteEvent
    data class SetSheet(val level: SheetLevel) : RouteEvent
    data class StartRequested(val reliableOrigin: Boolean = true) : RouteEvent
    data object DismissNotice : RouteEvent
}

internal class RouteBloc(
    private val service: RoutePlanningService,
    private val scope: CoroutineScope,
    private val searchDebounceMillis: Long = 350,
) {
    private val mutableState = MutableStateFlow(RouteState())
    val state = mutableState.asStateFlow()
    private var searchJob: Job? = null
    private var lastNear: GeoPoint? = null
    private var routeJob: Job? = null

    fun dispatch(event: RouteEvent) {
        val current = mutableState.value
        when (event) {
            is RouteEvent.OpenSearch -> {
                searchJob?.cancel()
                mutableState.value = current.copy(
                    editing = event.field, query = "", suggestions = emptyList(),
                    searching = false, searchError = "", searchEmpty = false,
                )
            }
            RouteEvent.CloseSearch -> {
                searchJob?.cancel()
                mutableState.value = current.copy(
                    editing = null, searching = false, searchError = "", searchEmpty = false,
                )
            }
            RouteEvent.RetrySearch -> search(current.query, lastNear)
            is RouteEvent.UseCurrentLocation -> {
                val field = current.editing ?: RouteField.Origin
                searchJob?.cancel()
                mutableState.value = when (field) {
                    RouteField.Origin -> current.copy(origin = event.place, originIsCurrent = true)
                    RouteField.Destination -> current.copy(destination = event.place)
                }.copy(editing = null, query = "", suggestions = emptyList(), searching = false, searchError = "", searchEmpty = false)
                planRoute()
            }
            is RouteEvent.QueryChanged -> search(event.query, event.near)
            is RouteEvent.SelectPlace -> select(event.place)
            RouteEvent.Swap -> {
                if (current.origin == null && current.destination == null) return
                mutableState.value = current.copy(
                    origin = current.destination, destination = current.origin, originIsCurrent = false,
                )
                planRoute()
            }
            RouteEvent.Retry -> planRoute()
            is RouteEvent.SelectClass -> mutableState.value = current.copy(vehicleClass = event.vehicleClass)
            is RouteEvent.SetSheet -> mutableState.value = current.copy(sheet = event.level)
            is RouteEvent.StartRequested -> mutableState.value = current.copy(
                notice = when {
                    current.status !is RouteStatus.Ready -> "Спершу побудуйте актуальний маршрут."
                    current.originIsCurrent && !event.reliableOrigin ->
                        "Початок «моє місцезнаходження» потребує надійної позиції. Дочекайтесь визначення або вкажіть початок вручну."
                    else -> "Покрокова навігація ще не підключена. Маршрут показано на мапі для огляду."
                },
            )
            RouteEvent.DismissNotice -> mutableState.value = current.copy(notice = "")
        }
    }

    /** Back goes one step up: search, then sheet levels. Returns false when the caller should handle it. */
    fun handleBack(): Boolean {
        val current = mutableState.value
        when {
            current.editing != null -> dispatch(RouteEvent.CloseSearch)
            current.sheet == SheetLevel.Expanded -> dispatch(RouteEvent.SetSheet(SheetLevel.Partial))
            current.sheet == SheetLevel.Partial -> dispatch(RouteEvent.SetSheet(SheetLevel.Collapsed))
            else -> return false
        }
        return true
    }

    private fun search(query: String, near: GeoPoint?) {
        lastNear = near
        searchJob?.cancel()
        val trimmed = query.trim()
        mutableState.value = mutableState.value.copy(
            query = query, searchError = "", searchEmpty = false,
            suggestions = if (trimmed.length < 2) emptyList() else mutableState.value.suggestions,
            searching = trimmed.length >= 2,
        )
        if (trimmed.length < 2) return
        searchJob = scope.launch {
            delay(searchDebounceMillis)
            try {
                val places = service.search(trimmed, near)
                mutableState.value = mutableState.value.copy(
                    suggestions = places, searching = false, searchError = "",
                    searchEmpty = places.isEmpty(),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: RoutePlanningException) {
                mutableState.value = mutableState.value.copy(
                    suggestions = emptyList(), searching = false, searchEmpty = false,
                    searchError = "Не вдалося виконати пошук. Спробуйте ще раз.",
                )
            }
        }
    }

    private fun select(place: RoutePlace) {
        searchJob?.cancel()
        val current = mutableState.value
        val field = current.editing ?: return
        mutableState.value = when (field) {
            RouteField.Origin -> current.copy(origin = place)
            RouteField.Destination -> current.copy(destination = place)
        }.copy(
            editing = null, query = "", suggestions = emptyList(), searching = false,
            searchError = "", searchEmpty = false,
            originIsCurrent = if (field == RouteField.Origin) false else current.originIsCurrent,
        )
        planRoute()
    }

    private fun planRoute() {
        routeJob?.cancel()
        val current = mutableState.value
        val from = current.origin
        val to = current.destination
        if (from == null || to == null) {
            mutableState.value = current.copy(status = RouteStatus.Idle, previousRoute = null)
            return
        }
        val previous = (current.status as? RouteStatus.Ready)?.route ?: current.previousRoute
        mutableState.value = current.copy(status = RouteStatus.Loading, previousRoute = previous)
        routeJob = scope.launch {
            val result = try {
                RouteStatus.Ready(service.route(from.point, to.point))
            } catch (error: CancellationException) {
                throw error
            } catch (error: RoutePlanningException) {
                RouteStatus.Failed(error.kind.message())
            }
            mutableState.value = mutableState.value.copy(
                status = result,
                previousRoute = if (result is RouteStatus.Ready) null else mutableState.value.previousRoute,
            )
        }
    }
}

private fun RoutePlanningException.Kind.message() = when (this) {
    RoutePlanningException.Kind.Unavailable -> "Сервіс маршрутів не налаштований: немає ключа Mapbox."
    RoutePlanningException.Kind.Network -> "Не вдалося зв’язатися із сервісом маршрутів. Перевірте мережу."
    RoutePlanningException.Kind.NoRoute -> "Маршрут між цими точками не знайдено."
    RoutePlanningException.Kind.Rejected -> "Сервіс маршрутів відхилив запит. Перевірте ключ Mapbox."
}
