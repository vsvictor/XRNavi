package com.mobilespace.xrnavi

internal class StopPlacesBloc {
    companion object {
    fun reduce(state: StopPlacesState, event: StopPlacesEvent): StopPlacesState = when (event) {
        is StopPlacesEvent.Open -> StopPlacesState(source = event.source)
        is StopPlacesEvent.Category -> state.copy(
            category = event.category,
            selectedId = null,
            notice = ""
        )

        is StopPlacesEvent.Query -> state.copy(query = event.value, notice = "")
        is StopPlacesEvent.Unavailable -> state.copy(
            notice = "${event.action}: сервіс не підключено. Дію не виконано."
        )
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
