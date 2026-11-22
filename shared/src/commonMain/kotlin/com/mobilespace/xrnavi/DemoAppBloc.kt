package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DemoAppBloc(initial: DemoAppState = DemoAppState()) {
    private val mutableState = MutableStateFlow(initial)
    val state = mutableState.asStateFlow()

    fun dispatch(event: DemoAppEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: DemoAppState, event: DemoAppEvent): DemoAppState = when (event) {
            is DemoAppEvent.ScreenChanged -> state.copy(screen = event.screen)
            is DemoAppEvent.AuthScreenOpened -> state.copy(notice = "", screen = event.screen)
            DemoAppEvent.MapOpenedAfterSignIn, DemoAppEvent.DemoStarted -> state.copy(
                notice = "",
                sectionNavigation = DemoSectionNavigation(),
                screen = AppScreen.Map
            )

            DemoAppEvent.DemoExited -> state.copy(
                sectionNavigation = DemoSectionNavigation(),
                notice = "",
                screen = AppScreen.SignIn
            )

            DemoAppEvent.PreviewClosed -> state.copy(screen = AppScreen.Map)
            DemoAppEvent.MapLeft -> state.copy(notice = "", screen = AppScreen.SignIn)
            is DemoAppEvent.SectionOpened ->
                state.copy(sectionNavigation = state.sectionNavigation.open(event.section))

            DemoAppEvent.SectionBack ->
                state.copy(sectionNavigation = state.sectionNavigation.back())

            is DemoAppEvent.StopPlacesSectionOpened -> state.copy(
                screen = AppScreen.Map,
                sectionNavigation = state.sectionNavigation.open(event.section)
            )

            is DemoAppEvent.GarageVehicleSelected -> garageVehicleSelected(state, event)
            is DemoAppEvent.VehicleConfigurationApplied -> configurationApplied(state, event)
            is DemoAppEvent.VehicleClassChanged ->
                if (event.vehicleClass == state.vehicleClass) state else state.copy(
                    vehicleClass = event.vehicleClass,
                    selectedVehicleId = demoVehicleForClass(event.vehicleClass)?.id
                        ?: state.selectedVehicleId,
                    screen = AppScreen.Map,
                    notice = vehicleClassUnavailableMessage(event.vehicleClass).orEmpty()
                )

            is DemoAppEvent.VehicleModeChanged -> state.copy(vehicleMode = event.mode)
            is DemoAppEvent.SheetLevelChanged -> state.copy(sheet = event.level)
            is DemoAppEvent.NoticeShown -> state.copy(notice = event.message)
            DemoAppEvent.PreviewRequested -> {
                val block = routeStopsPreviewBlockMessage(state.routeStops.applied)
                    ?: routePreviewBlockMessage(
                        state.addresses.origin,
                        state.addresses.destination,
                        state.vehicleClass
                    )
                if (block != null) state.copy(notice = block)
                else state.copy(notice = "", screen = AppScreen.Preview)
            }

            DemoAppEvent.RouteStarted -> state.copy(
                notice = vehicleClassUnavailableMessage(state.vehicleClass)
                    ?: "Маршрут і навігацію не підключено. Це лише демонстраційний перегляд.",
                sheet = SheetLevel.Expanded
            )

            is DemoAppEvent.SearchOpened -> state.copy(
                previousScreen = state.screen,
                addresses = state.addresses.open(event.target),
                searchStatus = SearchStatus.Idle,
                screen = AppScreen.Search
            )

            is DemoAppEvent.SearchTargetChanged -> state.copy(
                addresses = state.addresses.activate(event.target),
                searchStatus = SearchStatus.Idle
            )

            is DemoAppEvent.SearchQueryChanged -> state.copy(
                addresses = state.addresses.copy(query = event.query),
                searchStatus = SearchStatus.Idle
            )

            DemoAppEvent.SearchRequested -> state.copy(searchRequest = state.searchRequest + 1)
            is DemoAppEvent.SearchStatusChanged -> state.copy(searchStatus = event.status)
            DemoAppEvent.SearchClosed -> closeSearch(state)
            DemoAppEvent.MapPickRequested -> state.copy(
                notice = "Вибір координат на мапі не підключено. Введіть адресу вручну.",
                screen = AppScreen.Map
            )

            is DemoAppEvent.DemoPlaceSelected -> {
                val selection = selectSavedPlaceAddress(
                    state.addresses, event.place.toSavedPlace(), state.previousScreen
                )
                state.copy(
                    addresses = selection.addresses,
                    notice = "Демо-адресу вибрано; геокодування й маршрут провайдером не перевірялися.",
                    screen = selection.screen
                )
            }

            is DemoAppEvent.SearchSectionChanged ->
                if (event.section == DemoSection.Map) closeSearch(state)
                else state.copy(sectionNavigation = state.sectionNavigation.open(event.section))

            DemoAppEvent.RouteStopsOpened -> state.copy(
                previousScreen = state.screen,
                routeStops = reduceRouteStops(state.routeStops, RouteStopsEvent.BeginEditing),
                routeStopsNotice = "",
                screen = AppScreen.RouteStops
            )

            is DemoAppEvent.RouteStopsEdited ->
                state.copy(routeStops = reduceRouteStops(state.routeStops, event.event))

            DemoAppEvent.RouteStopAdded ->
                openStopSearch(state, reduceRouteStops(state.routeStops, RouteStopsEvent.AddStop))

            is DemoAppEvent.RouteStopReplaceRequested -> openStopSearch(
                state,
                reduceRouteStops(state.routeStops, RouteStopsEvent.ReplaceAddress(event.stopId))
            )

            DemoAppEvent.RouteStopsEditingCancelled -> state.copy(
                routeStops = reduceRouteStops(state.routeStops, RouteStopsEvent.CancelEditing),
                routeStopsNotice = "",
                screen = state.previousScreen
            )

            is DemoAppEvent.RouteStopsSaved -> {
                val changed = routeStopsChanged(event.next.applied, state.routeStops.applied)
                state.copy(
                    routeStops = event.next,
                    routeStopsNotice = if (event.next.applied.isNotEmpty()) {
                        "Багатоточковий перегляд ще не підключено."
                    } else {
                        ""
                    },
                    screen = if (changed) AppScreen.Map else state.previousScreen
                )
            }

            is DemoAppEvent.RouteStopsSaveFailed -> state.copy(routeStopsNotice = event.message)
            is DemoAppEvent.RouteStopSearchPlaceSelected -> state.copy(
                routeStops = reduceRouteStops(state.routeStops, event.event),
                routeStopsNotice = "",
                screen = AppScreen.RouteStops
            )

            DemoAppEvent.RouteStopSearchCancelled -> state.copy(
                routeStops = reduceRouteStops(state.routeStops, RouteStopsEvent.CancelSearch),
                screen = AppScreen.RouteStops
            )

            is DemoAppEvent.StopPlacesDispatched -> stopPlacesDispatched(state, event.event)

            DemoAppEvent.RouteSettingsOpened -> state.copy(
                routePreferencesDraft = state.defaultRoutePreferencesDraft(),
                routeSettingsReturnScreen = state.screen,
                notice = "",
                screen = AppScreen.RouteSettings
            )

            is DemoAppEvent.RoutePreferencesChanged -> state.copy(
                routePreferencesDraft = state.routeSettingsDraft.update(event.preferences)
            )

            DemoAppEvent.RouteSettingsClosed -> state.copy(
                routePreferencesDraft = null,
                screen = state.routeSettingsReturnScreen
            )

            DemoAppEvent.GarageOpenedFromRouteSettings -> state.copy(
                sectionNavigation = DemoSectionNavigation().open(DemoSection.Garage)
            )

            DemoAppEvent.ConfigurationOpenedFromRouteSettings -> state.copy(
                sectionNavigation = DemoSectionNavigation()
                    .open(DemoSection.Garage)
                    .open(DemoSection.VehicleDetails)
                    .open(DemoSection.VehicleConfigurations)
            )

            DemoAppEvent.RoutePreferencesApplied -> {
                val result = applyRoutePreferences(
                    activeVehicleId = state.selectedVehicleId,
                    preferencesByVehicle = state.routePreferencesByVehicle,
                    draft = state.routeSettingsDraft,
                    activeConfigurationAvailable = state.activeConfigurationAvailable
                )
                if (result.applied) state.copy(
                    notice = result.message,
                    routePreferencesByVehicle = result.preferencesByVehicle,
                    routePreferencesDraft = null,
                    screen = routeSettingsReturnScreen(
                        state.routeSettingsReturnScreen, result.changed
                    )
                ) else state.copy(notice = result.message)
            }

            is DemoAppEvent.SavedPlacesOpened -> state.copy(
                savedPlacesOrigin = event.origin,
                savedPlacesEditKey = event.editPlace?.key,
                savedPlacesNotice = "",
                screen = AppScreen.SavedPlaces
            )

            is DemoAppEvent.SavedPlacesChanged ->
                state.copy(savedPlaces = event.places, savedPlacesNotice = "")

            is DemoAppEvent.SavedPlacesNoticeChanged ->
                state.copy(savedPlacesNotice = event.message)

            DemoAppEvent.SavedPlacesClosed -> state.copy(
                savedPlacesEditKey = null,
                screen = if (state.savedPlacesOrigin == SavedPlacesOrigin.Search) {
                    AppScreen.Search
                } else {
                    AppScreen.Map
                }
            )

            is DemoAppEvent.SavedPlaceAddressSelected -> {
                val selection = selectSavedPlaceAddress(
                    state.addresses, event.place, state.previousScreen
                )
                state.copy(
                    addresses = selection.addresses,
                    notice = "Збережену адресу вибрано для ${
                        if (state.addresses.target == SearchTarget.Origin) "початку" else "призначення"
                    }. Повторний вибір тієї самої адреси не змінює Preview.",
                    savedPlacesEditKey = null,
                    screen = selection.screen
                )
            }

            is DemoAppEvent.SavedPlaceUsedAsDestination -> {
                val selection = selectSavedPlaceAddress(
                    state.addresses.copy(target = SearchTarget.Destination),
                    event.place,
                    AppScreen.Map
                )
                state.copy(
                    addresses = selection.addresses.copy(query = ""),
                    notice = if (state.addresses.origin.isBlank()) {
                        "Адресу призначення вибрано. Початок не задано — виберіть його вручну на мапі."
                    } else {
                        "Адресу призначення вибрано. Демо-попередній перегляд не запускався."
                    },
                    savedPlacesEditKey = null,
                    screen = selection.screen,
                    sectionNavigation = DemoSectionNavigation()
                )
            }
        }

        private fun closeSearch(state: DemoAppState) =
            state.copy(screen = state.previousScreen, addresses = state.addresses.cancel())

        private fun openStopSearch(state: DemoAppState, routeStops: RouteStopsState) =
            state.copy(
                routeStops = routeStops,
                screen = if (routeStops.searchStopId != null) AppScreen.RouteStopSearch
                else state.screen
            )

        private fun garageVehicleSelected(
            state: DemoAppState,
            event: DemoAppEvent.GarageVehicleSelected
        ): DemoAppState {
            val selection = selectGarageVehicle(state.selectedVehicleId, event.vehicle)
            if (!selection.changed) return state
            return state.copy(
                vehicleClass = selection.vehicleClass,
                selectedVehicleId = selection.vehicleId,
                routePreferencesDraft = null,
                screen = AppScreen.Map,
                sheet = SheetLevel.Collapsed,
                sectionNavigation = state.sectionNavigation.back(),
                notice = "Обрано наявний демопрофіль ${event.vehicle.name}. Його характеристики не підтверджують право власності."
            )
        }

        private fun configurationApplied(
            state: DemoAppState,
            event: DemoAppEvent.VehicleConfigurationApplied
        ): DemoAppState {
            val result = applyVehicleConfiguration(
                activeVehicleId = state.selectedVehicleId,
                currentSelections = state.configurationSelections,
                configuration = event.configuration
            )
            if (!result.applied) return state.copy(notice = result.message)
            var next = state.copy(
                configurationSelections = result.selections,
                notice = result.message
            )
            if (
                result.parametersChanged &&
                (state.screen == AppScreen.Preview ||
                        (state.screen == AppScreen.RouteSettings &&
                                state.routeSettingsReturnScreen == AppScreen.Preview))
            ) {
                next = if (state.screen == AppScreen.RouteSettings) {
                    next.copy(routeSettingsReturnScreen = AppScreen.Map)
                } else {
                    next.copy(screen = AppScreen.Map)
                }.copy(
                    notice = "Параметри конфігурації змінено. Створіть попередній перегляд повторно."
                )
            }
            return next.copy(sectionNavigation = DemoSectionNavigation())
        }

        private fun stopPlacesDispatched(state: DemoAppState, event: StopPlacesEvent): DemoAppState {
            val transition = StopPlacesBloc.transition(state.stopPlaces, state.routeStops, event)
            var next = state.copy(
                stopPlaces = transition.catalog,
                routeStops = transition.routeStops
            )
            if (transition.beganEditing) next = next.copy(previousScreen = AppScreen.Map)
            if (transition.destination == StopPlacesDestination.Editor) {
                next = next.copy(routeStopsNotice = "")
            }
            transition.destination?.let { destination ->
                next = next.copy(
                    screen = when (destination) {
                        StopPlacesDestination.Catalog -> AppScreen.StopPlaces
                        StopPlacesDestination.Map -> AppScreen.Map
                        StopPlacesDestination.StopSearch -> AppScreen.RouteStopSearch
                        StopPlacesDestination.Editor -> AppScreen.RouteStops
                    }
                )
            }
            return next
        }
    }
}
