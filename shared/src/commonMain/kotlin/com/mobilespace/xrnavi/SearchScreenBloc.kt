package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class SearchScreenState(
    val duplicatePlace: SavedPlace? = null,
    val saveNotice: String = ""
)

internal sealed interface SearchScreenEvent {
    data object PlaceSaved : SearchScreenEvent
    data class DuplicatePlace(val place: DemoPlace) : SearchScreenEvent
    data object InvalidPlace : SearchScreenEvent
    data object DismissDuplicate : SearchScreenEvent
    data object Reset : SearchScreenEvent
}

internal class SearchScreenBloc {
    private val mutableState = MutableStateFlow(SearchScreenState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: SearchScreenEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: SearchScreenState, event: SearchScreenEvent): SearchScreenState =
            when (event) {
                SearchScreenEvent.PlaceSaved -> state.copy(
                    duplicatePlace = null,
                    saveNotice = "Адресу додано до локального обраного."
                )

                is SearchScreenEvent.DuplicatePlace -> state.copy(
                    duplicatePlace = event.place.toSavedPlace(),
                    saveNotice = ""
                )

                SearchScreenEvent.InvalidPlace -> state.copy(
                    duplicatePlace = null,
                    saveNotice = "Цю адресу не вдалося перевірити для збереження."
                )

                SearchScreenEvent.DismissDuplicate -> state.copy(duplicatePlace = null)
                SearchScreenEvent.Reset -> SearchScreenState()
            }
    }
}
