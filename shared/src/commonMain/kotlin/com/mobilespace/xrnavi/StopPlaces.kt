package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal enum class StopPlaceCategory(val label: String) {
    Parking("Стоянки"), Rest("Відпочинок"), Fuel("Пальне"), Charging("Зарядки"),
    Service("Сервіс"), Weighing("Вагові"), Border("Кордон")
}

internal data class StopPlace(
    val id: String,
    val name: String,
    val category: StopPlaceCategory,
    val address: DemoPlace,
    val description: String
)

internal val stopPlaces = listOf(
    StopPlace(
        "demo-parking-01",
        "Демонстраційна стоянка 01",
        StopPlaceCategory.Parking,
        demoPlaces[0],
        "Синтетичний приклад стоянки. Це не підтвердження існування об’єкта за адресою."
    ),
    StopPlace(
        "demo-rest-01", "Демонстраційне місце відпочинку 01", StopPlaceCategory.Rest,
        demoPlaces[1], "Синтетичний приклад місця відпочинку, не рекомендація безпечної зупинки."
    ),
    StopPlace(
        "demo-fuel-01", "Демонстраційна АЗС 01", StopPlaceCategory.Fuel,
        demoPlaces[3], "Синтетичний приклад АЗС. Послуги та можливість заїзду не перевірено."
    )
)

internal data class StopPlacesState(
    val category: StopPlaceCategory = StopPlaceCategory.Parking,
    val query: String = "",
    val selectedId: String? = null,
    val notice: String = "",
    val source: StopPlacesSource = StopPlacesSource.Map,
    val targetStopId: String? = null
) {
    val results: List<StopPlace>
        get() = stopPlaces.filter {
            it.category == category && (
                    query.isBlank() || listOf(
                        it.name,
                        it.address.title,
                        it.address.detail
                    ).any { text ->
                        text.contains(query.trim(), ignoreCase = true)
                    }
                    )
        }
    val selected: StopPlace? get() = stopPlaces.firstOrNull { it.id == selectedId }

    companion object {
        val Saver = listSaver<StopPlacesState, String>(
            save = {
                listOf(
                    it.category.name,
                    it.query,
                    it.selectedId.orEmpty(),
                    it.notice,
                    it.source.name,
                    it.targetStopId.orEmpty()
                )
            },
            restore = {
                StopPlacesState(
                    StopPlaceCategory.valueOf(it[0]), it[1], it[2].ifBlank { null }, it[3],
                    StopPlacesSource.valueOf(it[4]), it[5].ifBlank { null }
                )
            }
        )
    }
}

internal enum class StopPlacesSource { Map, StopSearch }
internal enum class StopPlacesDestination { Catalog, Map, StopSearch, Editor }
internal data class StopPlacesTransition(
    val catalog: StopPlacesState,
    val routeStops: RouteStopsState,
    val destination: StopPlacesDestination? = null,
    val beganEditing: Boolean = false
)

internal sealed interface StopPlacesEvent {
    data class Open(val source: StopPlacesSource) : StopPlacesEvent
    data class Category(val category: StopPlaceCategory) : StopPlacesEvent
    data class Query(val value: String) : StopPlacesEvent
    data class Details(val id: String) : StopPlacesEvent
    data object BackToList : StopPlacesEvent
    data object Back : StopPlacesEvent
    data object AddSelected : StopPlacesEvent
}

internal object StopPlacesBloc {
    fun reduce(state: StopPlacesState, event: StopPlacesEvent): StopPlacesState = when (event) {
        is StopPlacesEvent.Open -> StopPlacesState(source = event.source)
        is StopPlacesEvent.Category -> state.copy(
            category = event.category,
            selectedId = null,
            notice = ""
        )

        is StopPlacesEvent.Query -> state.copy(query = event.value, notice = "")
        is StopPlacesEvent.Details -> if (stopPlaces.any { it.id == event.id }) {
            state.copy(selectedId = event.id, notice = "")
        } else {
            state.copy(selectedId = null, notice = "Об’єкт відсутній у демокаталозі")
        }

        StopPlacesEvent.BackToList -> state.copy(selectedId = null, notice = "")
        StopPlacesEvent.Back -> state.copy(selectedId = null, notice = "")
        StopPlacesEvent.AddSelected -> state
    }

    fun transition(
        state: StopPlacesState,
        routeStops: RouteStopsState,
        event: StopPlacesEvent
    ): StopPlacesTransition = when (event) {
        is StopPlacesEvent.Open -> StopPlacesTransition(
            reduce(state, event).copy(
                targetStopId = if (event.source == StopPlacesSource.StopSearch) routeStops.searchStopId else null
            ),
            routeStops, StopPlacesDestination.Catalog
        )

        StopPlacesEvent.Back -> StopPlacesTransition(
            reduce(state, event), routeStops,
            if (state.selectedId != null) null
            else if (state.source == StopPlacesSource.Map) StopPlacesDestination.Map
            else StopPlacesDestination.StopSearch
        )

        StopPlacesEvent.AddSelected -> {
            val place = state.selected
            val missingTarget = state.source == StopPlacesSource.StopSearch &&
                    (!routeStops.isEditing || state.targetStopId == null ||
                            state.targetStopId != routeStops.searchStopId ||
                            (!routeStops.searchIsNew &&
                                    routeStops.draft?.none { it.stopId == state.targetStopId } == true))
            if (place == null || missingTarget) {
                StopPlacesTransition(
                    state.copy(
                        notice = if (missingTarget) "Ціль зупинки більше недоступна"
                        else "Об’єкт відсутній у демокаталозі"
                    ),
                    routeStops
                )
            } else {
                val begin = state.source == StopPlacesSource.Map
                val editing = if (begin) reduceRouteStops(
                    routeStops,
                    RouteStopsEvent.BeginEditing
                ) else routeStops
                StopPlacesTransition(
                    state, addStopPlaceToDraft(editing, place),
                    StopPlacesDestination.Editor, begin
                )
            }
        }

        else -> StopPlacesTransition(reduce(state, event), routeStops)
    }
}

internal fun addStopPlaceToDraft(state: RouteStopsState, place: StopPlace): RouteStopsState {
    if (!state.isEditing) return state
    val targeted = if (state.searchStopId == null) {
        reduceRouteStops(state, RouteStopsEvent.AddStop)
    } else {
        state
    }
    val stopId = targeted.searchStopId ?: return state
    return reduceRouteStops(
        targeted,
        RouteStopsEvent.SelectSavedPlace(
            stopId, SavedPlace(
                address = place.address.title, detail = place.address.detail, label = place.name
            )
        )
    )
}
