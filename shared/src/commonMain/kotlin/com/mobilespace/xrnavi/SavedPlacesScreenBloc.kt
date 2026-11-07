package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class SavedPlacesScreenState(
    val editingPlace: SavedPlace? = null,
    val editedLabel: String = "",
    val deletingPlace: SavedPlace? = null
)

internal sealed interface SavedPlacesScreenEvent {
    data class Edit(val place: SavedPlace) : SavedPlacesScreenEvent
    data class EditLabel(val value: String) : SavedPlacesScreenEvent
    data object SaveEdit : SavedPlacesScreenEvent
    data object CancelEdit : SavedPlacesScreenEvent
    data class RequestDelete(val place: SavedPlace) : SavedPlacesScreenEvent
    data object ConfirmDelete : SavedPlacesScreenEvent
    data object CancelDelete : SavedPlacesScreenEvent
    data class SelectInitialEdit(val place: SavedPlace?) : SavedPlacesScreenEvent
    data object Reset : SavedPlacesScreenEvent
}

internal class SavedPlacesScreenBloc {
    private val mutableState = MutableStateFlow(SavedPlacesScreenState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: SavedPlacesScreenEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(
            state: SavedPlacesScreenState,
            event: SavedPlacesScreenEvent
        ): SavedPlacesScreenState = when (event) {
            is SavedPlacesScreenEvent.Edit -> state.copy(
                editingPlace = event.place,
                editedLabel = event.place.label
            )

            is SavedPlacesScreenEvent.EditLabel -> state.copy(editedLabel = event.value)
            SavedPlacesScreenEvent.SaveEdit,
            SavedPlacesScreenEvent.CancelEdit -> state.copy(
                editingPlace = null,
                editedLabel = ""
            )

            is SavedPlacesScreenEvent.RequestDelete -> state.copy(deletingPlace = event.place)
            SavedPlacesScreenEvent.ConfirmDelete,
            SavedPlacesScreenEvent.CancelDelete -> state.copy(deletingPlace = null)

            is SavedPlacesScreenEvent.SelectInitialEdit -> {
                val place = event.place
                if (place != null && state.editingPlace?.key != place.key) {
                    state.copy(editingPlace = place, editedLabel = place.label)
                } else {
                    state
                }
            }

            SavedPlacesScreenEvent.Reset -> SavedPlacesScreenState()
        }
    }
}
