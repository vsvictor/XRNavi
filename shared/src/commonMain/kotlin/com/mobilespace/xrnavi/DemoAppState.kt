package com.mobilespace.xrnavi

internal data class DemoAppState(
    val screen: AppScreen = AppScreen.Map,
    val previousScreen: AppScreen = AppScreen.Map,
    val sheet: SheetLevel = SheetLevel.Collapsed,
    val searchStatus: SearchStatus = SearchStatus.Idle,
    val searchRequest: Int = 0,
    val geoStatus: GeoStatus = GeoStatus.NotConfigured,
    val notice: String = "",
    val vehicleClass: String = "Легкове",
    val selectedVehicleId: String = demoVehicle.id,
    val vehicleMode: String = "Особистий",
    val configurationSelections: Map<String, String> = defaultVehicleConfigurationSelections(),
    val routePreferencesByVehicle: Map<String, RoutePreferences> = emptyMap(),
    val routePreferencesDraft: RoutePreferencesDraft? = null,
    val routeSettingsReturnScreen: AppScreen = AppScreen.Map,
    val sectionNavigation: DemoSectionNavigation = DemoSectionNavigation(),
    val addresses: AddressSearchState = AddressSearchState(),
    val routeStops: RouteStopsState = RouteStopsState(),
    val routeStopsNotice: String = "",
    val stopPlaces: StopPlacesState = StopPlacesState(),
    val savedPlaces: List<SavedPlace> = emptyList(),
    val savedPlacesNotice: String = "",
    val savedPlacesOrigin: SavedPlacesOrigin = SavedPlacesOrigin.Search,
    val savedPlacesEditKey: String? = null
) {
    val activeConfigurationAvailable: Boolean
        get() = vehicleConfigurationFor(selectedVehicleId, configurationSelections)
            ?.availability == VehicleConfigurationAvailability.Available

    val activeVehicle: DemoVehicleProfile?
        get() = demoVehicleById(selectedVehicleId)?.withConfiguration(
            vehicleConfigurationFor(selectedVehicleId, configurationSelections)
        )

    val routeSettingsDraft: RoutePreferencesDraft
        get() = routePreferencesDraft ?: defaultRoutePreferencesDraft()

    fun defaultRoutePreferencesDraft(): RoutePreferencesDraft {
        val applied = routePreferencesByVehicle[selectedVehicleId] ?: RoutePreferences()
        return RoutePreferencesDraft(selectedVehicleId, applied, applied)
    }
}

internal sealed interface DemoAppEvent {
    data class ScreenChanged(val screen: AppScreen) : DemoAppEvent
    data class AuthScreenOpened(val screen: AppScreen) : DemoAppEvent
    data object MapOpenedAfterSignIn : DemoAppEvent
    data object DemoStarted : DemoAppEvent
    data object DemoExited : DemoAppEvent
    data object PreviewClosed : DemoAppEvent
    data object MapLeft : DemoAppEvent
    data class SectionOpened(val section: DemoSection) : DemoAppEvent
    data object SectionBack : DemoAppEvent
    data class StopPlacesSectionOpened(val section: DemoSection) : DemoAppEvent
    data class GarageVehicleSelected(val vehicle: DemoVehicleProfile) : DemoAppEvent
    data class VehicleConfigurationApplied(val configuration: DemoVehicleConfiguration) : DemoAppEvent
    data class VehicleClassChanged(val vehicleClass: String) : DemoAppEvent
    data class VehicleModeChanged(val mode: String) : DemoAppEvent
    data class SheetLevelChanged(val level: SheetLevel) : DemoAppEvent
    data class NoticeShown(val message: String) : DemoAppEvent
    data object PreviewRequested : DemoAppEvent
    data object RouteStarted : DemoAppEvent

    data class SearchOpened(val target: SearchTarget) : DemoAppEvent
    data class SearchTargetChanged(val target: SearchTarget) : DemoAppEvent
    data class SearchQueryChanged(val query: String) : DemoAppEvent
    data object SearchRequested : DemoAppEvent
    data class SearchStatusChanged(val status: SearchStatus) : DemoAppEvent
    data object SearchClosed : DemoAppEvent
    data object MapPickRequested : DemoAppEvent
    data class DemoPlaceSelected(val place: DemoPlace) : DemoAppEvent
    data class SearchSectionChanged(val section: DemoSection) : DemoAppEvent

    data object RouteStopsOpened : DemoAppEvent
    data class RouteStopsEdited(val event: RouteStopsEvent) : DemoAppEvent
    data object RouteStopAdded : DemoAppEvent
    data class RouteStopReplaceRequested(val stopId: String) : DemoAppEvent
    data object RouteStopsEditingCancelled : DemoAppEvent
    data class RouteStopsSaved(val next: RouteStopsState) : DemoAppEvent
    data class RouteStopsSaveFailed(val message: String) : DemoAppEvent
    data class RouteStopSearchPlaceSelected(val event: RouteStopsEvent) : DemoAppEvent
    data object RouteStopSearchCancelled : DemoAppEvent
    data class StopPlacesDispatched(val event: StopPlacesEvent) : DemoAppEvent

    data object RouteSettingsOpened : DemoAppEvent
    data class RoutePreferencesChanged(val preferences: RoutePreferences) : DemoAppEvent
    data object RouteSettingsClosed : DemoAppEvent
    data object GarageOpenedFromRouteSettings : DemoAppEvent
    data object ConfigurationOpenedFromRouteSettings : DemoAppEvent
    data object RoutePreferencesApplied : DemoAppEvent

    data class SavedPlacesOpened(
        val origin: SavedPlacesOrigin,
        val editPlace: SavedPlace?
    ) : DemoAppEvent
    data class SavedPlacesChanged(val places: List<SavedPlace>) : DemoAppEvent
    data class SavedPlacesNoticeChanged(val message: String) : DemoAppEvent
    data object SavedPlacesClosed : DemoAppEvent
    data class SavedPlaceAddressSelected(val place: SavedPlace) : DemoAppEvent
    data class SavedPlaceUsedAsDestination(val place: SavedPlace) : DemoAppEvent
}
