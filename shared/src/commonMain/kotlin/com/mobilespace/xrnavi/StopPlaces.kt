package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal enum class StopPlaceCategory {
    Parking,
    Rest,
    Fuel,
    Charging,
    Service,
    Weighing,
    Border
}

internal data class StopPlacesState(
    val category: StopPlaceCategory = StopPlaceCategory.Parking,
    val query: String = "",
    val source: StopPlacesSource = StopPlacesSource.Map
) {
    // TODO: Load and filter results through an injected POI repository instead of this empty projection.
    val results: List<StopPlace> get() = emptyList()

    companion object {
        val Saver = listSaver<StopPlacesState, String>(
            save = { listOf(it.category.name, it.query, it.source.name) },
            restore = {
                StopPlacesState(
                    category = StopPlaceCategory.valueOf(it[0]),
                    query = it[1],
                    source = StopPlacesSource.valueOf(it[2])
                )
            }
        )
    }
}

internal data class StopPlace(
    val id: String,
    val name: String,
    val category: StopPlaceCategory,
    val address: SavedPlace,
    val description: String
)

internal val stopPlaces = emptyList<StopPlace>()

internal enum class StopPlacesSource { Map, StopSearch }
internal enum class StopPlacesDestination { Catalog, Map, StopSearch }
internal data class StopPlacesTransition(
    val catalog: StopPlacesState,
    val destination: StopPlacesDestination? = null
)

internal sealed interface StopPlacesEvent {
    data class Open(val source: StopPlacesSource) : StopPlacesEvent
    data class Category(val category: StopPlaceCategory) : StopPlacesEvent
    data class Query(val value: String) : StopPlacesEvent
    data object Back : StopPlacesEvent
}

internal object StopPlacesBloc {
    fun reduce(state: StopPlacesState, event: StopPlacesEvent): StopPlacesState = when (event) {
        is StopPlacesEvent.Open -> StopPlacesState(source = event.source)
        is StopPlacesEvent.Category -> state.copy(category = event.category)
        is StopPlacesEvent.Query -> state.copy(query = event.value)
        StopPlacesEvent.Back -> state
    }

    fun transition(
        state: StopPlacesState,
        event: StopPlacesEvent
    ): StopPlacesTransition = when (event) {
        is StopPlacesEvent.Open -> StopPlacesTransition(
            reduce(state, event), StopPlacesDestination.Catalog
        )

        StopPlacesEvent.Back -> StopPlacesTransition(
            reduce(state, event),
            if (state.source == StopPlacesSource.Map) {
                StopPlacesDestination.Map
            } else {
                StopPlacesDestination.StopSearch
            }
        )

        else -> StopPlacesTransition(reduce(state, event))
    }
}
