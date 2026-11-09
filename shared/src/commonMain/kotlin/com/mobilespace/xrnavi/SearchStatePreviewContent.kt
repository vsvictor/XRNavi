package com.mobilespace.xrnavi

import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Shared body of S51/S52/S53; each state has its own screen file. */
@Composable
internal fun SearchStatePreviewContent(
    status: SearchStatus,
    onSearchTarget: AdditionalScreen,
    actions: AdditionalActions
) {
    val model: SearchStatePreviewViewModel = viewModel { SearchStatePreviewViewModel() }
    val state by model.bloc.state.collectAsStateWithLifecycle()
    val unavailable = actions.unavailable
    SearchScreen(
        demoMode = true, origin = "Київ · приклад", destination = "",
        vehicleClass = "Легкове", selectedVehicle = demoVehicle,
        target = SearchTarget.Destination, query = state.query,
        status = status,
        vehicleMode = "Особистий · приклад стану", savedPlaceKeys = emptySet(),
        onVehicleModeChange = { unavailable("Зміна режиму") },
        onTargetChange = { unavailable("Редагування адреси у прикладі") },
        onQueryChange = { model.bloc.dispatch(SearchStatePreviewEvent.Query(it)) },
        onSearch = { actions.open(onSearchTarget) },
        onBack = actions.back, onPickMap = { actions.open(AdditionalScreen.SelectedPoint) },
        onSelectPlace = { unavailable("Вибір адреси у прикладі") },
        onSavePlace = { unavailable("Збереження адреси у прикладі"); SavedPlaceAddStatus.Invalid },
        onOpenSavedPlaces = { unavailable("Обране у прикладі") },
        onSectionChange = actions.section, onToggleTheme = { unavailable("Зміна теми у прикладі") }
    )
}
