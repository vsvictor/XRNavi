package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.*
import com.mobilespace.xrnavi.presentation.routestops.RouteStopsBloc
import com.mobilespace.xrnavi.presentation.routestops.RouteStopsEffect
import com.mobilespace.xrnavi.presentation.routestops.RouteStopsNotice
import com.mobilespace.xrnavi.presentation.routestops.routeStopsBlocFactory
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferences
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferencesDraft
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferencesNotice
import com.mobilespace.xrnavi.domain.routepreferences.applyRoutePreferences
import com.mobilespace.xrnavi.presentation.routepreferences.routePreferencesDraftSaver
import com.mobilespace.xrnavi.presentation.routepreferences.routePreferencesSaver
import com.mobilespace.xrnavi.data.theme.rememberThemePreferenceStore
import com.mobilespace.xrnavi.presentation.theme.ThemeSettingsBloc
import com.mobilespace.xrnavi.presentation.theme.ThemeSettingsEvent
import com.mobilespace.xrnavi.presentation.theme.ThemeSettingsState
import com.mobilespace.xrnavi.presentation.theme.themeSettingsBlocFactory
import com.mobilespace.xrnavi.presentation.auth.SignInBloc
import com.mobilespace.xrnavi.presentation.auth.SignInBlocFactory
import com.mobilespace.xrnavi.presentation.auth.SignInEvent
import com.mobilespace.xrnavi.presentation.auth.RegistrationBloc
import com.mobilespace.xrnavi.presentation.auth.RegistrationBlocFactory
import com.mobilespace.xrnavi.presentation.auth.RegistrationEvent
import com.mobilespace.xrnavi.presentation.auth.RecoveryBloc
import com.mobilespace.xrnavi.presentation.auth.RecoveryBlocFactory
import com.mobilespace.xrnavi.presentation.auth.RecoveryEvent

internal enum class AppScreen {
    SignIn,
    Registration,
    Recovery,
    Map,
    Search,
    RouteSettings,
    RouteStops,
    RouteStopSearch,
    StopPlaces,
    SavedPlaces
}

internal fun routeSettingsReturnScreen(
    previousScreen: AppScreen,
    preferencesChanged: Boolean
): AppScreen = if (preferencesChanged) AppScreen.Map else previousScreen

internal enum class SearchTarget {
    Origin,
    Destination
}

internal fun searchQueryAfterTargetChange(
    currentTarget: SearchTarget,
    nextTarget: SearchTarget,
    query: String
): String = if (currentTarget == nextTarget) query else ""

internal data class AddressSearchState(
    val origin: String = "",
    val destination: String = "",
    val target: SearchTarget = SearchTarget.Destination,
    val query: String = ""
) {
    fun open(target: SearchTarget) = copy(target = target, query = "")

    fun activate(target: SearchTarget) = copy(
        target = target,
        query = searchQueryAfterTargetChange(this.target, target, query)
    )

    fun select(place: PlaceAddress) = when (target) {
        SearchTarget.Origin -> copy(origin = place.title, query = "")
        SearchTarget.Destination -> copy(destination = place.title, query = "")
    }

    fun selectSavedPlace(place: SavedPlace) = select(PlaceAddress(place.address, place.detail))

    fun cancel() = copy(query = "")

    companion object {
        val Saver = listSaver<AddressSearchState, String>(
            save = { listOf(it.origin, it.destination, it.target.name, it.query) },
            restore = { AddressSearchState(it[0], it[1], SearchTarget.valueOf(it[2]), it[3]) }
        )
    }

}

internal data class SavedPlaceAddressSelection(
    val addresses: AddressSearchState,
    val screen: AppScreen,
    val changed: Boolean
)

internal fun selectSavedPlaceAddress(
    addresses: AddressSearchState,
    place: SavedPlace,
    currentScreen: AppScreen
): SavedPlaceAddressSelection {
    val previousAddress = when (addresses.target) {
        SearchTarget.Origin -> addresses.origin
        SearchTarget.Destination -> addresses.destination
    }
    val updated = addresses.selectSavedPlace(place)
    val changed = previousAddress != place.address
    return SavedPlaceAddressSelection(
        addresses = updated,
        screen = if (changed) AppScreen.Map else currentScreen,
        changed = changed
    )
}

internal enum class GeoStatus {
    NotConfigured,
    PermissionDenied,
    ServiceDisabled,
    Acquiring,
    FreshFix,
    StaleFix;

    val isFreshPosition: Boolean
        get() = this == FreshFix
}

internal enum class SearchStatus {
    Idle,
    Error
}

internal data class PlaceAddress(
    val title: String,
    val detail: String
)

@Composable
fun App(onThemeChanged: (Boolean) -> Unit = {}) {
    val themeStore = rememberThemePreferenceStore()
    val themeSettingsBloc: ThemeSettingsBloc = viewModel(
        factory = remember(themeStore) { themeSettingsBlocFactory(themeStore) }
    )
    val themeSettingsState by themeSettingsBloc.state.collectAsState()
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = themeSettingsState.preference.isDark(systemDarkTheme)
    LaunchedEffect(darkTheme) {
        onThemeChanged(darkTheme)
    }
    XrnaviTheme(darkTheme = darkTheme) {
        XrnaviApplication(
            themeSettings = themeSettingsState,
            onThemeEvent = themeSettingsBloc::dispatch,
            onToggleTheme = {
                themeSettingsBloc.dispatch(ThemeSettingsEvent.Toggle(systemDarkTheme))
            }
        )
    }
}

@Composable
private fun XrnaviApplication(
    themeSettings: ThemeSettingsState,
    onThemeEvent: (ThemeSettingsEvent) -> Unit,
    onToggleTheme: () -> Unit
) {
    val signInBloc: SignInBloc = viewModel(factory = SignInBlocFactory)
    val signInState by signInBloc.state.collectAsState()
    val registrationBloc: RegistrationBloc = viewModel(factory = RegistrationBlocFactory)
    val recoveryBloc: RecoveryBloc = viewModel(factory = RecoveryBlocFactory)
    val savedPlacesStore = rememberSavedPlacesStore()
    val savedPlacesBloc: SavedPlacesBloc = viewModel(
        factory = remember(savedPlacesStore) { savedPlacesBlocFactory(savedPlacesStore) }
    )
    val savedPlacesState by savedPlacesBloc.state.collectAsState()
    val savedPlacesNotice = when (savedPlacesState.notice) {
        SavedPlacesNotice.DataUnreadable ->
            stringResource(Res.string.saved_places_data_unreadable)
        SavedPlacesNotice.DataCorrupt ->
            stringResource(Res.string.saved_places_data_corrupt)
        SavedPlacesNotice.InvalidLabel ->
            stringResource(Res.string.saved_places_invalid_label)
        SavedPlacesNotice.SaveFailed ->
            stringResource(Res.string.saved_places_save_failed)
        null -> ""
    }
    val routeStopsStore = rememberRouteStopsStore()
    var hasRestoredRouteStops by rememberSaveable { mutableStateOf(false) }
    var savedRouteStopsState by rememberSaveable(stateSaver = RouteStopsState.Saver) {
        mutableStateOf(RouteStopsState())
    }
    val routeStopsBloc: RouteStopsBloc = viewModel(
        factory = remember(
            routeStopsStore,
            hasRestoredRouteStops,
            savedRouteStopsState
        ) {
            routeStopsBlocFactory(
                routeStopsStore,
                savedRouteStopsState.takeIf { hasRestoredRouteStops }
            )
        }
    )
    val routeStopsUiState by routeStopsBloc.state.collectAsState()
    val routeStopsState = routeStopsUiState.route
    SideEffect {
        savedRouteStopsState = routeStopsState
        hasRestoredRouteStops = true
    }
    val routeStopsNotice = when (routeStopsUiState.notice) {
        RouteStopsNotice.DataUnreadable ->
            stringResource(Res.string.route_stops_data_unreadable)
        RouteStopsNotice.SaveUnavailable ->
            stringResource(Res.string.route_stops_save_unavailable)
        RouteStopsNotice.SaveFailed ->
            stringResource(Res.string.route_stops_save_failed)
        RouteStopsNotice.MultiStopPreviewUnavailable ->
            stringResource(Res.string.route_stops_preview_unavailable)
        null -> ""
    }
    val routePreferencesNotices = mapOf(
        RoutePreferencesNotice.NoActiveVehicle to
            stringResource(Res.string.route_preferences_error_no_vehicle),
        RoutePreferencesNotice.WrongVehicle to
            stringResource(Res.string.route_preferences_error_wrong_vehicle),
        RoutePreferencesNotice.ConfigurationUnavailable to
            stringResource(Res.string.route_preferences_error_configuration),
        RoutePreferencesNotice.SavedChanged to
            stringResource(Res.string.route_preferences_saved_changed),
        RoutePreferencesNotice.SavedUnchanged to
            stringResource(Res.string.route_preferences_saved_unchanged)
    )
    var stopPlacesState by rememberSaveable(stateSaver = StopPlacesState.Saver) {
        mutableStateOf(StopPlacesState())
    }
    val savedPlaces = savedPlacesState.places
    var savedPlacesEditKey by rememberSaveable { mutableStateOf<String?>(null) }
    var screenName by rememberSaveable {
        mutableStateOf(AppScreen.SignIn.name)
    }
    var previousScreenName by rememberSaveable { mutableStateOf(AppScreen.Map.name) }
    val dispatchStopPlaces: (StopPlacesEvent) -> Unit = { event ->
        val transition = StopPlacesBloc.transition(stopPlacesState, event)
        stopPlacesState = transition.catalog
        transition.destination?.let { destination ->
            screenName = when (destination) {
                StopPlacesDestination.Catalog -> AppScreen.StopPlaces.name
                StopPlacesDestination.Map -> AppScreen.Map.name
                StopPlacesDestination.StopSearch -> AppScreen.RouteStopSearch.name
            }
        }
    }
    var addresses by rememberSaveable(stateSaver = AddressSearchState.Saver) {
        mutableStateOf(AddressSearchState())
    }
    val origin = addresses.origin
    val destination = addresses.destination
    val target = addresses.target
    val query = addresses.query
    var sheetName by rememberSaveable { mutableStateOf(SheetLevel.Collapsed.name) }
    var searchStatusName by rememberSaveable { mutableStateOf(SearchStatus.Idle.name) }
    var geoStatusName by rememberSaveable { mutableStateOf(GeoStatus.NotConfigured.name) }
    var notice by rememberSaveable { mutableStateOf("") }
    var routePreferencesByVehicle by rememberSaveable(stateSaver = routePreferencesSaver) {
        mutableStateOf<Map<String, RoutePreferences>>(emptyMap())
    }
    var routePreferencesDraft by rememberSaveable(stateSaver = routePreferencesDraftSaver) {
        mutableStateOf<RoutePreferencesDraft?>(null)
    }
    var routeSettingsReturnScreenName by rememberSaveable {
        mutableStateOf(AppScreen.Map.name)
    }
    val screen = AppScreen.valueOf(screenName)
    val sheetLevel = SheetLevel.valueOf(sheetName)
    val searchStatus = SearchStatus.valueOf(searchStatusName)
    val geoStatus = GeoStatus.valueOf(geoStatusName)
    val authUnavailableNotice = stringResource(
        Res.string.auth_sign_in_unavailable_notice
    )
    val mapCoordinateSelectionUnavailableNotice = stringResource(
        Res.string.map_coordinate_selection_unavailable_notice
    )
    val routeAddressNotSelected = stringResource(Res.string.route_stops_not_selected)
    val notConfiguredGeoNotice = stringResource(Res.string.geo_status_not_configured)
    val savedPlaceOriginSelectedNotice = stringResource(
        Res.string.saved_place_selected_notice,
        stringResource(Res.string.saved_place_origin_label)
    )
    val savedPlaceDestinationSelectedNotice = stringResource(
        Res.string.saved_place_selected_notice,
        stringResource(Res.string.saved_place_destination_label)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        val dispatchRouteStops: (RouteStopsEvent) -> Unit = { event ->
            val effect = routeStopsBloc.dispatch(event)
            when (event) {
                RouteStopsEvent.CancelEditing -> screenName = previousScreenName
                RouteStopsEvent.Save -> {
                    if (effect is RouteStopsEffect.Saved) {
                        screenName = if (effect.changed) {
                            AppScreen.Map.name
                        } else {
                            previousScreenName
                        }
                    }
                }
                else -> Unit
            }
        }
        val openSavedPlaces: (SavedPlace?) -> Unit = { editPlace ->
            savedPlacesEditKey = editPlace?.key
            savedPlacesBloc.dispatch(SavedPlacesEvent.ClearNotice)
            screenName = AppScreen.SavedPlaces.name
        }
        when (screen) {
            AppScreen.SignIn -> SignInScreen(
                email = signInState.email,
                onEmailChange = { signInBloc.dispatch(SignInEvent.EmailChanged(it)) },
                password = signInState.password,
                onPasswordChange = { signInBloc.dispatch(SignInEvent.PasswordChanged(it)) },
                notice = notice,
                onSignIn = {
                    // TODO: Add data/auth and domain/auth repository/use-case wiring; only a verified session may open Map.
                    notice = authUnavailableNotice
                },
                onRegister = {
                    signInBloc.dispatch(SignInEvent.ClearPassword)
                    notice = ""
                    screenName = AppScreen.Registration.name
                },
                onForgotPassword = {
                    signInBloc.dispatch(SignInEvent.ClearPassword)
                    recoveryBloc.dispatch(RecoveryEvent.Cancel)
                    notice = ""
                    screenName = AppScreen.Recovery.name
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.Registration -> RegistrationScreen(
                bloc = registrationBloc,
                onBack = {
                    registrationBloc.dispatch(RegistrationEvent.Cancel)
                    screenName = AppScreen.SignIn.name
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.Recovery -> RecoveryScreen(
                bloc = recoveryBloc,
                onBack = {
                    recoveryBloc.dispatch(RecoveryEvent.Cancel)
                    screenName = AppScreen.SignIn.name
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.Map -> MapScreen(
                origin = origin,
                destination = destination,
                routeStops = routeStopsState.applied,
                sheetLevel = sheetLevel,
                geoStatus = geoStatus,
                notice = notice,
                onOpenSearch = { searchTarget ->
                    previousScreenName = screenName
                    addresses = addresses.open(searchTarget)
                    searchStatusName = SearchStatus.Idle.name
                    screenName = AppScreen.Search.name
                },
                onOpenRouteStops = {
                    previousScreenName = screenName
                    routeStopsBloc.dispatch(RouteStopsEvent.BeginEditing)
                    screenName = AppScreen.RouteStops.name
                },
                onOpenStopPlaces = {
                    dispatchStopPlaces(StopPlacesEvent.Open(StopPlacesSource.Map))
                },
                onUseLocation = {
                    // TODO: Wire the platform location source and permission flow; set FreshFix only from a real position.
                    geoStatusName = GeoStatus.NotConfigured.name
                    notice = notConfiguredGeoNotice
                },
                onSheetLevelChange = { sheetName = it.name },
                onOpenRouteSettings = {
                    val applied = routePreferencesByVehicle[""] ?: RoutePreferences()
                    routePreferencesDraft = RoutePreferencesDraft(
                        vehicleId = "",
                        applied = applied,
                        selected = applied
                    )
                    routeSettingsReturnScreenName = screenName
                    notice = ""
                    screenName = AppScreen.RouteSettings.name
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.RouteStops -> RouteStopsScreen(
                origin = origin.ifBlank { routeAddressNotSelected },
                destination = destination.ifBlank { routeAddressNotSelected },
                stops = routeStopsState.visibleStops,
                notice = routeStopsNotice,
                canSave = routeStopsUiState.canSave,
                onAdd = {
                    dispatchRouteStops(RouteStopsEvent.AddStop)
                    if (routeStopsState.searchStopId != null) {
                        screenName = AppScreen.RouteStopSearch.name
                    }
                },
                onReplace = { stopId ->
                    dispatchRouteStops(RouteStopsEvent.ReplaceAddress(stopId))
                    if (routeStopsState.searchStopId != null) {
                        screenName = AppScreen.RouteStopSearch.name
                    }
                },
                onRemove = { dispatchRouteStops(RouteStopsEvent.RemoveStop(it)) },
                onMoveUp = { dispatchRouteStops(RouteStopsEvent.MoveUp(it)) },
                onMoveDown = { dispatchRouteStops(RouteStopsEvent.MoveDown(it)) },
                onSave = { dispatchRouteStops(RouteStopsEvent.Save) },
                onCancel = { dispatchRouteStops(RouteStopsEvent.CancelEditing) },
                onToggleTheme = onToggleTheme
            )

            AppScreen.RouteStopSearch -> {
                val searchStopId = routeStopsState.searchStopId
                if (searchStopId == null) {
                    screenName = AppScreen.RouteStops.name
                } else {
                    RouteStopSearchScreen(
                        stopId = searchStopId,
                        query = routeStopsState.query,
                        savedPlaces = savedPlaces,
                        onQueryChange = {
                            dispatchRouteStops(RouteStopsEvent.UpdateQuery(it))
                        },
                        onSelectSavedPlace = { place ->
                            dispatchRouteStops(
                                RouteStopsEvent.SelectSavedPlace(searchStopId, place)
                            )
                            screenName = AppScreen.RouteStops.name
                        },
                        onBack = {
                            dispatchRouteStops(RouteStopsEvent.CancelSearch)
                            screenName = AppScreen.RouteStops.name
                        },
                        onOpenStopPlaces = {
                            dispatchStopPlaces(StopPlacesEvent.Open(StopPlacesSource.StopSearch))
                        },
                        onToggleTheme = onToggleTheme
                    )
                }
            }

            AppScreen.StopPlaces -> StopPlacesScreen(
                state = stopPlacesState,
                onEvent = dispatchStopPlaces,
                onToggleTheme = onToggleTheme
            )

            AppScreen.RouteSettings -> {
                val draft = routePreferencesDraft
                    ?: RoutePreferencesDraft(
                        vehicleId = "",
                        applied = routePreferencesByVehicle[""] ?: RoutePreferences(),
                        selected = routePreferencesByVehicle[""] ?: RoutePreferences()
                    ).also { routePreferencesDraft = it }
                RouteSettingsScreen(
                    origin = origin,
                    destination = destination,
                    preferences = draft.selected,
                    hasPendingChanges = draft.hasPendingChanges,
                    canApply = false,
                    notice = notice,
                    onPreferencesChange = {
                        routePreferencesDraft = routePreferencesDraft?.update(it)
                    },
                    onBack = {
                        routePreferencesDraft = null
                        screenName = routeSettingsReturnScreenName
                    },
                    onApply = {
                        val result = applyRoutePreferences(
                            activeVehicleId = "",
                            preferencesByVehicle = routePreferencesByVehicle,
                            draft = draft,
                            activeConfigurationAvailable = false
                        )
                        notice = routePreferencesNotices.getValue(result.notice)
                        if (result.applied) {
                            routePreferencesByVehicle = result.preferencesByVehicle
                            routePreferencesDraft = null
                            screenName = routeSettingsReturnScreen(
                                AppScreen.valueOf(routeSettingsReturnScreenName),
                                result.changed
                            ).name
                        }
                    },
                    onToggleTheme = onToggleTheme
                )

            }

            AppScreen.Search -> SearchScreen(
                origin = origin,
                destination = destination,
                target = target,
                query = query,
                status = searchStatus,
                onTargetChange = {
                    addresses = addresses.activate(it)
                    searchStatusName = SearchStatus.Idle.name
                },
                onQueryChange = {
                    addresses = addresses.copy(query = it)
                    searchStatusName = SearchStatus.Idle.name
                },
                // TODO: Dispatch address queries to a configured place-search use case and display only provider results.
                onSearch = { searchStatusName = SearchStatus.Error.name },
                onBack = {
                    screenName = previousScreenName
                    addresses = addresses.cancel()
                },
                onPickMap = {
                    // TODO: Open the configured map SDK's coordinate picker and reverse-geocode its confirmed selection.
                    notice = mapCoordinateSelectionUnavailableNotice
                    screenName = AppScreen.Map.name
                },
                onOpenSavedPlaces = openSavedPlaces,
                onToggleTheme = onToggleTheme
            )

            AppScreen.SavedPlaces -> SavedPlacesScreen(
                    places = savedPlaces,
                    initialEditKey = savedPlacesEditKey,
                    notice = savedPlacesNotice,
                    onBack = {
                        savedPlacesEditKey = null
                        screenName = AppScreen.Search.name
                    },
                    onSelectAddress = { place ->
                        val selection = selectSavedPlaceAddress(
                            addresses,
                            place,
                            AppScreen.valueOf(previousScreenName)
                        )
                        addresses = selection.addresses
                        notice = if (target == SearchTarget.Origin) {
                            savedPlaceOriginSelectedNotice
                        } else {
                            savedPlaceDestinationSelectedNotice
                        }
                        savedPlacesEditKey = null
                        screenName = selection.screen.name
                    },
                    onRename = { key, label ->
                        savedPlacesBloc.dispatch(SavedPlacesEvent.Rename(key, label))
                    },
                    onDelete = { place ->
                        savedPlacesBloc.dispatch(SavedPlacesEvent.Delete(place.key))
                    },
                    onToggleTheme = onToggleTheme
                )
        }
    }
}

@Composable
internal fun geoStatusMessage(status: GeoStatus): String = when (status) {
    GeoStatus.NotConfigured -> stringResource(Res.string.geo_status_not_configured)
    GeoStatus.PermissionDenied -> stringResource(Res.string.geo_status_permission_denied)
    GeoStatus.ServiceDisabled -> stringResource(Res.string.geo_status_service_disabled)
    GeoStatus.Acquiring -> stringResource(Res.string.geo_status_acquiring)
    GeoStatus.FreshFix -> stringResource(Res.string.geo_status_fresh_fix)
    GeoStatus.StaleFix -> stringResource(Res.string.geo_status_stale_fix)
}
