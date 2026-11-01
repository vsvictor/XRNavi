package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class SavedPlacesUiState(
    val places: List<SavedPlace> = emptyList(),
    val notice: SavedPlacesNotice? = null,
    val canEdit: Boolean = true
)

internal enum class SavedPlacesNotice {
    DataUnreadable,
    DataCorrupt,
    InvalidLabel,
    SaveFailed
}

internal sealed interface SavedPlacesEvent {
    data class Rename(val key: String, val label: String) : SavedPlacesEvent
    data class Delete(val key: String) : SavedPlacesEvent
    data object ClearNotice : SavedPlacesEvent
}

internal class SavedPlacesBloc(
    loadSavedPlaces: LoadSavedPlaces,
    private val persistSavedPlaces: PersistSavedPlaces
) : ViewModel() {
    private val initialResult = loadSavedPlaces()
    private val mutableState = MutableStateFlow(initialResult.toUiState())
    val state: StateFlow<SavedPlacesUiState> = mutableState.asStateFlow()

    fun dispatch(event: SavedPlacesEvent) {
        val current = mutableState.value
        if (event == SavedPlacesEvent.ClearNotice) {
            mutableState.value = current.copy(notice = null)
            return
        }
        if (!current.canEdit) {
            mutableState.value = current.copy(notice = SavedPlacesNotice.DataUnreadable)
            return
        }
        val updated = when (event) {
            is SavedPlacesEvent.Rename -> {
                renameSavedPlace(current.places, event.key, event.label) ?: run {
                    mutableState.value = current.copy(
                        notice = SavedPlacesNotice.InvalidLabel
                    )
                    return
                }
            }

            is SavedPlacesEvent.Delete -> removeSavedPlace(current.places, event.key)
        }
        if (persistSavedPlaces(updated)) {
            mutableState.value = current.copy(places = updated, notice = null)
        } else {
            mutableState.value = current.copy(notice = SavedPlacesNotice.SaveFailed)
        }
    }

    private fun SavedPlacesDecodeResult.toUiState(): SavedPlacesUiState = when (this) {
        is SavedPlacesDecodeResult.Success ->
            SavedPlacesUiState(places = places)

        SavedPlacesDecodeResult.Invalid ->
            SavedPlacesUiState(
                notice = SavedPlacesNotice.DataCorrupt,
                canEdit = false
            )
    }
}

internal fun savedPlacesBlocFactory(store: SavedPlacesStore) = viewModelFactory {
    initializer {
        val repository = LocalSavedPlacesRepository(store)
        SavedPlacesBloc(
            loadSavedPlaces = LoadSavedPlaces(repository),
            persistSavedPlaces = PersistSavedPlaces(repository)
        )
    }
}
