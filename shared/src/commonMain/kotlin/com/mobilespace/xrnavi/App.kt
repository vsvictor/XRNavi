package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel

internal enum class AppScreen {
    SignIn,
    Registration,
    Recovery,
    Map,
    Search,
    Preview,
    RouteSettings,
    RouteStops,
    RouteStopSearch,
    SavedPlaces
}

internal fun initialScreenForSession(hasConfiguredSession: Boolean): AppScreen =
    if (hasConfiguredSession) AppScreen.Map else AppScreen.SignIn

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

    fun select(place: DemoPlace) = when (target) {
        SearchTarget.Origin -> copy(origin = place.title, query = "")
        SearchTarget.Destination -> copy(destination = place.title, query = "")
    }

    fun selectSavedPlace(place: SavedPlace) = select(DemoPlace(place.address, place.detail))

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

internal data class DemoVehicleProfile(
    val id: String,
    val name: String,
    val category: String,
    val specification: String,
    val artwork: DemoVehicleArtwork,
    val configurationName: String? = null
)

internal enum class DemoVehicleArtwork {
    Mustang,
    Passenger,
    Cargo,
    Special
}

internal val demoVehicle = DemoVehicleProfile(
    id = "ford-mustang-gt",
    name = "Ford Mustang GT",
    category = "Легкове",
    specification = "Легковий · 1,4 м · 1,8 т",
    artwork = DemoVehicleArtwork.Mustang
)

internal val demoVehicles = listOf(
    demoVehicle,
    DemoVehicleProfile(
        id = "demo-passenger-02",
        name = "Легковий автомобіль · демо 02",
        category = "Легкове",
        specification = "Легкове · характеристики не задані",
        artwork = DemoVehicleArtwork.Passenger
    ),
    DemoVehicleProfile(
        id = "demo-cargo-01",
        name = "Вантажний транспорт · демо 01",
        category = "Вантажне",
        specification = "Вантажне · характеристики не задані",
        artwork = DemoVehicleArtwork.Cargo
    ),
    DemoVehicleProfile(
        id = "demo-special-01",
        name = "Спецтехніка · демо 01",
        category = "Спецтехніка",
        specification = "Спецтехніка · характеристики не задані",
        artwork = DemoVehicleArtwork.Special
    )
)

internal fun demoVehicleById(vehicleId: String): DemoVehicleProfile? =
    demoVehicles.firstOrNull { it.id == vehicleId }

internal fun demoVehicleForClass(vehicleClass: String): DemoVehicleProfile? =
    demoVehicles.firstOrNull { it.category == vehicleClass }

internal fun routePreviewBlockMessage(
    origin: String,
    destination: String,
    vehicleClass: String
): String? = when {
    origin.isBlank() || destination.isBlank() ->
        "Вкажіть початкову та кінцеву точки, щоб переглянути демо-маршрут."

    demoVehicleForClass(vehicleClass) == null ->
        "Демо-профіль для транспорту «$vehicleClass» не налаштовано. Попередній перегляд недоступний."

    else -> null
}

internal fun vehicleClassUnavailableMessage(vehicleClass: String): String? =
    if (demoVehicleForClass(vehicleClass) != null) {
        null
    } else {
        "Демо-профіль для транспорту «$vehicleClass» не налаштовано. Виберіть категорію з наявними локальними демопрофілями."
    }

internal enum class GeoStatus {
    NotConfigured,
    PermissionDenied,
    ServiceDisabled,
    Acquiring,
    FreshFix,
    StaleFix
}

internal enum class SearchStatus {
    Idle,
    Loading,
    Results,
    Empty,
    Error
}

internal data class DemoPlace(
    val title: String,
    val detail: String
)

internal val demoPlaces = listOf(
    DemoPlace("площа Ринок, 1", "Львів · Львівська область · Ратуша"),
    DemoPlace("площа Ринок, 10", "Львів · Львівська область"),
    DemoPlace("площа Ринок, 1", "Дрогобич · Львівська область"),
    DemoPlace("вулиця Хрещатик, 22", "Київ · Київська міська рада")
)

@Composable
fun App(onThemeChanged: (Boolean) -> Unit = {}) {
    val themeStore = rememberThemePreferenceStore()
    val themeSettings: ThemeSettingsViewModel = viewModel(
        factory = remember(themeStore) { themeSettingsFactory(themeStore) }
    )
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = themeSettings.preference.isDark(systemDarkTheme)
    LaunchedEffect(darkTheme) {
        onThemeChanged(darkTheme)
    }
    XrnaviTheme(darkTheme = darkTheme) {
        XrnaviApplication(
            themeSettings = themeSettings,
            onToggleTheme = { themeSettings.toggle(systemDarkTheme) }
        )
    }
}

@Composable
private fun XrnaviApplication(themeSettings: ThemeSettingsViewModel, onToggleTheme: () -> Unit) {
    val signInViewModel: SignInViewModel = viewModel(factory = SignInViewModelFactory)
    val registrationViewModel: RegistrationViewModel = viewModel(factory = RegistrationViewModelFactory)
    val previewEnabled = internalAuthPreviewEnabled()
    val recoveryViewModel: RecoveryViewModel = viewModel(
        factory = remember(previewEnabled) { recoveryViewModelFactory(previewEnabled) }
    )
    val savedPlacesStore = rememberSavedPlacesStore()
    val routeStopsStore = rememberRouteStopsStore()
    val savedPlacesInitial = remember(savedPlacesStore) {
        decodeSavedPlaces(savedPlacesStore.read())
    }
    val routeStopsInitial = remember(routeStopsStore) {
        decodeRouteStopsSnapshot(routeStopsStore.read())
    }
    var routeStopsState by rememberSaveable(stateSaver = RouteStopsState.Saver) {
        mutableStateOf(
            RouteStopsState(
                applied = routeStopsInitial?.stops.orEmpty(),
                nextStopOrdinal = routeStopsInitial?.nextStopOrdinal ?: 1
            )
        )
    }
    var routeStopsNotice by rememberSaveable {
        mutableStateOf(
            if (routeStopsInitial == null) {
                "Дані зупинок не вдалося прочитати. Існуючий вміст не перезаписано."
            } else {
                ""
            }
        )
    }
    val savedPlacesLoaded = savedPlacesInitial as? SavedPlacesDecodeResult.Success
    var savedPlaces by remember(savedPlacesInitial) {
        mutableStateOf(savedPlacesLoaded?.places.orEmpty())
    }
    var savedPlacesNotice by rememberSaveable {
        mutableStateOf(
            if (savedPlacesInitial is SavedPlacesDecodeResult.Invalid) {
                "Збережені дані пошкоджені. Їх не перезаписано; зміни недоступні."
            } else {
                ""
            }
        )
    }
    var savedPlacesOriginName by rememberSaveable {
        mutableStateOf(SavedPlacesOrigin.Search.name)
    }
    var savedPlacesEditKey by rememberSaveable { mutableStateOf<String?>(null) }
    var screenName by rememberSaveable {
        mutableStateOf(initialScreenForSession(SessionProbe.hasConfiguredSession()).name)
    }
    var previousScreenName by rememberSaveable { mutableStateOf(AppScreen.Map.name) }
    var demoMode by rememberSaveable { mutableStateOf(false) }
    var addresses by rememberSaveable(stateSaver = AddressSearchState.Saver) {
        mutableStateOf(AddressSearchState())
    }
    val origin = addresses.origin
    val destination = addresses.destination
    val target = addresses.target
    val query = addresses.query
    var sheetName by rememberSaveable { mutableStateOf(SheetLevel.Collapsed.name) }
    var searchStatusName by rememberSaveable { mutableStateOf(SearchStatus.Idle.name) }
    var searchRequest by rememberSaveable { mutableIntStateOf(0) }
    var geoStatusName by rememberSaveable { mutableStateOf(GeoStatus.NotConfigured.name) }
    var notice by rememberSaveable { mutableStateOf("") }
    var vehicleClass by rememberSaveable { mutableStateOf("Легкове") }
    var selectedVehicleId by rememberSaveable { mutableStateOf(demoVehicle.id) }
    var vehicleMode by rememberSaveable { mutableStateOf("Особистий") }
    var configurationSelections by rememberSaveable(
        stateSaver = vehicleConfigurationSelectionsSaver
    ) {
        mutableStateOf(defaultVehicleConfigurationSelections())
    }
    var routePreferencesByVehicle by rememberSaveable(stateSaver = routePreferencesSaver) {
        mutableStateOf<Map<String, RoutePreferences>>(emptyMap())
    }
    var routePreferencesDraft by rememberSaveable(stateSaver = routePreferencesDraftSaver) {
        mutableStateOf<RoutePreferencesDraft?>(null)
    }
    var routeSettingsReturnScreenName by rememberSaveable {
        mutableStateOf(AppScreen.Map.name)
    }
    var sectionNavigation by rememberSaveable(stateSaver = DemoSectionNavigation.Saver) {
        mutableStateOf(DemoSectionNavigation())
    }
    val screen = AppScreen.valueOf(screenName)
    val sheetLevel = SheetLevel.valueOf(sheetName)
    val searchStatus = SearchStatus.valueOf(searchStatusName)
    val geoStatus = GeoStatus.valueOf(geoStatusName)

    LaunchedEffect(searchRequest) {
        if (searchRequest == 0) return@LaunchedEffect
        searchStatusName = SearchStatus.Loading.name
        withFrameNanos { }
        val normalizedQuery = query.trim()
        searchStatusName = when {
            !demoMode -> SearchStatus.Error.name
            normalizedQuery.isEmpty() -> SearchStatus.Results.name
            demoPlaces.any {
                it.title.contains(normalizedQuery, ignoreCase = true) ||
                        it.detail.contains(normalizedQuery, ignoreCase = true)
            } -> SearchStatus.Results.name

            else -> SearchStatus.Empty.name
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        val exitDemo = {
            signInViewModel.clearPassword()
            registrationViewModel.cancel()
            recoveryViewModel.cancel()
            demoMode = false
            sectionNavigation = DemoSectionNavigation()
            notice = ""
            screenName = AppScreen.SignIn.name
        }
        val persistSavedPlaces: (List<SavedPlace>) -> Boolean = { next ->
            if (savedPlacesLoaded == null) {
                savedPlacesNotice =
                    "Збережені дані не вдалося прочитати. Існуючий вміст не перезаписано."
                false
            } else if (savedPlacesStore.write(encodeSavedPlaces(next))) {
                savedPlaces = next
                savedPlacesNotice = ""
                true
            } else {
                savedPlacesNotice = "Не вдалося зберегти зміни на цьому пристрої."
                false
            }
        }
        val dispatchRouteStops: (RouteStopsEvent) -> Unit = { event ->
            when (event) {
                RouteStopsEvent.Save -> {
                    if (routeStopsInitial == null) {
                        routeStopsNotice =
                            "Дані зупинок недоступні для запису; попередній вміст не змінено."
                    } else {
                        val next = reduceRouteStops(routeStopsState, event)
                        if (routeStopsStore.write(
                                encodeRouteStopsSnapshot(
                                    RouteStopsSnapshot(next.applied, next.nextStopOrdinal)
                                )
                            )
                        ) {
                            val changed = routeStopsChanged(next.applied, routeStopsState.applied)
                            routeStopsState = next
                            routeStopsNotice = if (next.applied.isNotEmpty()) {
                                "Багатоточковий перегляд ще не підключено."
                            } else {
                                ""
                            }
                            screenName = if (changed) {
                                AppScreen.Map.name
                            } else {
                                previousScreenName
                            }
                        } else {
                            routeStopsNotice = "Не вдалося зберегти зупинки на цьому пристрої."
                        }
                    }
                }

                RouteStopsEvent.CancelEditing -> {
                    routeStopsState = reduceRouteStops(routeStopsState, event)
                    routeStopsNotice = ""
                    screenName = previousScreenName
                }

                else -> routeStopsState = reduceRouteStops(routeStopsState, event)
            }
        }
        val openSavedPlaces: (SavedPlacesOrigin, SavedPlace?) -> Unit = { source, editPlace ->
            savedPlacesOriginName = source.name
            savedPlacesEditKey = editPlace?.key
            savedPlacesNotice = ""
            screenName = AppScreen.SavedPlaces.name
        }
        val saveDemoPlace: (DemoPlace) -> SavedPlaceAddStatus = { place ->
            if (savedPlacesLoaded == null) {
                savedPlacesNotice =
                    "Збережені дані не вдалося прочитати. Нову адресу не додано."
                SavedPlaceAddStatus.Invalid
            } else {
                val result = addSavedPlace(savedPlaces, place)
                when (result.status) {
                    SavedPlaceAddStatus.Added ->
                        if (persistSavedPlaces(result.places)) {
                            SavedPlaceAddStatus.Added
                        } else {
                            SavedPlaceAddStatus.Invalid
                        }
                    SavedPlaceAddStatus.Duplicate -> SavedPlaceAddStatus.Duplicate
                    SavedPlaceAddStatus.Invalid -> SavedPlaceAddStatus.Invalid
                }
            }
        }
        if (
            sectionNavigation.section != DemoSection.Map &&
            demoMode &&
            screen != AppScreen.SavedPlaces
        ) {
            DemoAccountScreen(
                section = sectionNavigation.section,
                profile = DemoProfileState(vehicleClass, vehicleMode, selectedVehicleId),
                configurationSelections = configurationSelections,
                themeSettings = themeSettings,
                onSectionChange = { sectionNavigation = sectionNavigation.open(it) },
                savedPlacesCount = savedPlaces.size,
                onOpenSavedPlaces = { openSavedPlaces(SavedPlacesOrigin.Profile, null) },
                onBack = { sectionNavigation = sectionNavigation.back() },
                onSelectVehicle = { vehicle ->
                    val selection = selectGarageVehicle(selectedVehicleId, vehicle)
                    if (selection.changed) {
                        vehicleClass = selection.vehicleClass
                        selectedVehicleId = selection.vehicleId
                        routePreferencesDraft = null
                        screenName = AppScreen.Map.name
                        sheetName = SheetLevel.Collapsed.name
                        sectionNavigation = sectionNavigation.back()
                        notice =
                            "Обрано наявний демопрофіль ${vehicle.name}. Його характеристики не підтверджують право власності."
                    }
                },
                onApplyVehicleConfiguration = { configuration ->
                    val result = applyVehicleConfiguration(
                        activeVehicleId = selectedVehicleId,
                        currentSelections = configurationSelections,
                        configuration = configuration
                    )
                    if (result.applied) {
                        configurationSelections = result.selections
                        notice = result.message
                        if (
                            result.parametersChanged &&
                            (screenName == AppScreen.Preview.name ||
                                (screenName == AppScreen.RouteSettings.name &&
                                    routeSettingsReturnScreenName == AppScreen.Preview.name))
                        ) {
                            if (screenName == AppScreen.RouteSettings.name) {
                                routeSettingsReturnScreenName = AppScreen.Map.name
                            } else {
                                screenName = AppScreen.Map.name
                            }
                            notice =
                                "Параметри конфігурації змінено. Створіть попередній перегляд повторно."
                        }
                        sectionNavigation = DemoSectionNavigation()
                    } else {
                        notice = result.message
                    }
                },
                onExitDemo = exitDemo,
                onRegister = {
                    exitDemo()
                    screenName = AppScreen.Registration.name
                },
                onRecovery = {
                    exitDemo()
                    screenName = AppScreen.Recovery.name
                }
            )
        } else when (screen) {
            AppScreen.SignIn -> SignInScreen(
                email = signInViewModel.email,
                onEmailChange = signInViewModel::updateEmail,
                password = signInViewModel.password,
                onPasswordChange = signInViewModel::updatePassword,
                notice = notice,
                onSignIn = {
                    notice =
                        "Авторизацію не підключено. Дані входу не надсилалися й не зберігалися."
                },
                onDemo = {
                    signInViewModel.clearPassword()
                    demoMode = true
                    notice = ""
                    sectionNavigation = DemoSectionNavigation()
                    screenName = AppScreen.Map.name
                },
                onRegister = {
                    signInViewModel.clearPassword()
                    notice = ""
                    screenName = AppScreen.Registration.name
                },
                onForgotPassword = {
                    signInViewModel.clearPassword()
                    recoveryViewModel.cancel()
                    notice = ""
                    screenName = AppScreen.Recovery.name
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.Registration -> RegistrationScreen(
                viewModel = registrationViewModel,
                onBack = {
                    registrationViewModel.cancel()
                    screenName = AppScreen.SignIn.name
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.Recovery -> RecoveryScreen(
                model = recoveryViewModel,
                previewEnabled = previewEnabled,
                onBack = {
                    recoveryViewModel.cancel()
                    screenName = AppScreen.SignIn.name
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.Map, AppScreen.Preview -> MapScreen(
                preview = screen == AppScreen.Preview,
                demoMode = demoMode,
                origin = origin,
                destination = destination,
                routeStops = routeStopsState.applied,
                sheetLevel = sheetLevel,
                geoStatus = geoStatus,
                notice = notice,
                vehicleClass = vehicleClass,
                selectedVehicle = demoVehicleById(selectedVehicleId)?.withConfiguration(
                    vehicleConfigurationFor(selectedVehicleId, configurationSelections)
                ),
                vehicleMode = vehicleMode,
                onVehicleClassChange = { nextClass ->
                    if (nextClass != vehicleClass) {
                        vehicleClass = nextClass
                        selectedVehicleId = demoVehicleForClass(nextClass)?.id ?: selectedVehicleId
                        screenName = AppScreen.Map.name
                        notice = vehicleClassUnavailableMessage(nextClass).orEmpty()
                    }
                },
                onOpenSearch = { searchTarget ->
                    previousScreenName = screenName
                    addresses = addresses.open(searchTarget)
                    searchStatusName = SearchStatus.Idle.name
                    screenName = AppScreen.Search.name
                },
                onOpenRouteStops = {
                    previousScreenName = screenName
                    routeStopsState = reduceRouteStops(
                        routeStopsState,
                        RouteStopsEvent.BeginEditing
                    )
                    routeStopsNotice = ""
                    screenName = AppScreen.RouteStops.name
                },
                onUseLocation = {
                    geoStatusName = GeoStatus.NotConfigured.name
                    notice = geoStatusMessage(GeoStatus.NotConfigured)
                },
                onSheetLevelChange = { sheetName = it.name },
                onPreview = {
                    val blockMessage = routeStopsPreviewBlockMessage(routeStopsState.applied)
                        ?: routePreviewBlockMessage(origin, destination, vehicleClass)
                    if (blockMessage != null) {
                        notice = blockMessage
                    } else {
                        notice = ""
                        screenName = AppScreen.Preview.name
                    }
                },
                onStartRoute = {
                    notice = vehicleClassUnavailableMessage(vehicleClass)
                        ?: "Маршрут і навігацію не підключено. Це лише демонстраційний перегляд."
                    sheetName = SheetLevel.Expanded.name
                },
                onOpenRouteSettings = {
                    val applied = routePreferencesByVehicle[selectedVehicleId] ?: RoutePreferences()
                    routePreferencesDraft = RoutePreferencesDraft(
                        vehicleId = selectedVehicleId,
                        applied = applied,
                        selected = applied
                    )
                    routeSettingsReturnScreenName = screenName
                    notice = ""
                    screenName = AppScreen.RouteSettings.name
                },
                onBack = {
                    screenName = if (screen == AppScreen.Preview) {
                        AppScreen.Map.name
                    } else {
                        demoMode = false
                        notice = ""
                        AppScreen.SignIn.name
                    }
                },
                onSectionChange = { sectionNavigation = sectionNavigation.open(it) },
                onToggleTheme = onToggleTheme
            )

            AppScreen.RouteStops -> RouteStopsScreen(
                origin = origin.ifBlank { "Не обрано" },
                destination = destination.ifBlank { "Не обрано" },
                stops = routeStopsState.visibleStops,
                notice = routeStopsNotice,
                canSave = routeStopsInitial != null,
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
                        onSelectDemoPlace = { place ->
                            dispatchRouteStops(
                                RouteStopsEvent.SelectDemoPlace(searchStopId, place)
                            )
                            routeStopsNotice = ""
                            screenName = AppScreen.RouteStops.name
                        },
                        onSelectSavedPlace = { place ->
                            dispatchRouteStops(
                                RouteStopsEvent.SelectSavedPlace(searchStopId, place)
                            )
                            routeStopsNotice = ""
                            screenName = AppScreen.RouteStops.name
                        },
                        onBack = {
                            dispatchRouteStops(RouteStopsEvent.CancelSearch)
                            screenName = AppScreen.RouteStops.name
                        },
                        onToggleTheme = onToggleTheme
                    )
                }
            }

            AppScreen.RouteSettings -> {
                val activeVehicle = demoVehicleById(selectedVehicleId)?.withConfiguration(
                    vehicleConfigurationFor(selectedVehicleId, configurationSelections)
                )
                val draft = routePreferencesDraft
                    ?: RoutePreferencesDraft(
                        vehicleId = selectedVehicleId,
                        applied = routePreferencesByVehicle[selectedVehicleId] ?: RoutePreferences(),
                        selected = routePreferencesByVehicle[selectedVehicleId] ?: RoutePreferences()
                    ).also { routePreferencesDraft = it }
                RouteSettingsScreen(
                    origin = origin,
                    destination = destination,
                    vehicle = activeVehicle,
                    preferences = draft.selected,
                    hasPendingChanges = draft.hasPendingChanges,
                    canApply = draft.vehicleId == selectedVehicleId &&
                        vehicleConfigurationFor(selectedVehicleId, configurationSelections)
                            ?.availability == VehicleConfigurationAvailability.Available,
                    notice = notice,
                    onPreferencesChange = {
                        routePreferencesDraft = routePreferencesDraft?.update(it)
                    },
                    onBack = {
                        routePreferencesDraft = null
                        screenName = routeSettingsReturnScreenName
                    },
                    onOpenGarage = {
                        sectionNavigation = DemoSectionNavigation().open(DemoSection.Garage)
                    },
                    onOpenConfiguration = {
                        sectionNavigation = DemoSectionNavigation()
                            .open(DemoSection.Garage)
                            .open(DemoSection.VehicleDetails)
                            .open(DemoSection.VehicleConfigurations)
                    },
                    onApply = {
                        val result = applyRoutePreferences(
                            activeVehicleId = selectedVehicleId,
                            preferencesByVehicle = routePreferencesByVehicle,
                            draft = draft,
                            activeConfigurationAvailable = vehicleConfigurationFor(
                                selectedVehicleId,
                                configurationSelections
                            )?.availability == VehicleConfigurationAvailability.Available
                        )
                        notice = result.message
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
                demoMode = demoMode,
                origin = origin,
                destination = destination,
                vehicleClass = vehicleClass,
                selectedVehicle = demoVehicleById(selectedVehicleId)?.withConfiguration(
                    vehicleConfigurationFor(selectedVehicleId, configurationSelections)
                ),
                target = target,
                query = query,
                status = searchStatus,
                vehicleMode = vehicleMode,
                savedPlaceKeys = savedPlaces.mapTo(mutableSetOf()) { it.key },
                onVehicleModeChange = { vehicleMode = it },
                onTargetChange = {
                    addresses = addresses.activate(it)
                    searchStatusName = SearchStatus.Idle.name
                },
                onQueryChange = {
                    addresses = addresses.copy(query = it)
                    searchStatusName = SearchStatus.Idle.name
                },
                onSearch = { searchRequest += 1 },
                onBack = {
                    screenName = previousScreenName
                    addresses = addresses.cancel()
                },
                onPickMap = {
                    notice = "Вибір координат на мапі не підключено. Введіть адресу вручну."
                    screenName = AppScreen.Map.name
                },
                onSelectPlace = { place ->
                    val selection = selectSavedPlaceAddress(
                        addresses,
                        place.toSavedPlace(),
                        AppScreen.valueOf(previousScreenName)
                    )
                    addresses = selection.addresses
                    notice =
                        "Демо-адресу вибрано; геокодування й маршрут провайдером не перевірялися."
                    screenName = selection.screen.name
                },
                onSavePlace = saveDemoPlace,
                onOpenSavedPlaces = { editPlace ->
                    openSavedPlaces(SavedPlacesOrigin.Search, editPlace)
                },
                onSectionChange = {
                    if (it == DemoSection.Map) {
                        screenName = previousScreenName
                        addresses = addresses.cancel()
                    } else {
                        sectionNavigation = sectionNavigation.open(it)
                    }
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.SavedPlaces -> {
                val savedPlacesOrigin = SavedPlacesOrigin.valueOf(savedPlacesOriginName)
                SavedPlacesScreen(
                    places = savedPlaces,
                    initialEditKey = savedPlacesEditKey,
                    allowAddressSelection = savedPlacesOrigin == SavedPlacesOrigin.Search,
                    allowDestinationUse = savedPlacesOrigin == SavedPlacesOrigin.Profile,
                    notice = savedPlacesNotice,
                    onBack = {
                        savedPlacesEditKey = null
                        screenName = if (savedPlacesOrigin == SavedPlacesOrigin.Search) {
                            AppScreen.Search.name
                        } else {
                            AppScreen.Map.name
                        }
                    },
                    onSelectAddress = { place ->
                        val selection = selectSavedPlaceAddress(
                            addresses,
                            place,
                            AppScreen.valueOf(previousScreenName)
                        )
                        addresses = selection.addresses
                        notice =
                            "Збережену адресу вибрано для ${
                                if (target == SearchTarget.Origin) "початку" else "призначення"
                            }. Повторний вибір тієї самої адреси не змінює Preview."
                        savedPlacesEditKey = null
                        screenName = selection.screen.name
                    },
                    onUseAsDestination = { place ->
                        val selection = selectSavedPlaceAddress(
                            addresses.copy(target = SearchTarget.Destination),
                            place,
                            AppScreen.Map
                        )
                        addresses = selection.addresses.copy(query = "")
                        notice = if (origin.isBlank()) {
                            "Адресу призначення вибрано. Початок не задано — виберіть його вручну на мапі."
                        } else {
                            "Адресу призначення вибрано. Демо-попередній перегляд не запускався."
                        }
                        savedPlacesEditKey = null
                        screenName = selection.screen.name
                        sectionNavigation = DemoSectionNavigation()
                    },
                    onRename = { key, label ->
                        val updated = renameSavedPlace(savedPlaces, key, label)
                        if (updated == null) {
                            savedPlacesNotice = "Введіть непорожній ярлик і спробуйте ще раз."
                        } else {
                            persistSavedPlaces(updated)
                        }
                    },
                    onDelete = { place ->
                        persistSavedPlaces(removeSavedPlace(savedPlaces, place.key))
                    },
                    onToggleTheme = onToggleTheme
                )
            }
        }
    }
}

private object SessionProbe {
    fun hasConfiguredSession(): Boolean = false
}

internal fun geoStatusMessage(status: GeoStatus): String = when (status) {
    GeoStatus.NotConfigured -> "служба та дозвіл не перевірені — native adapter не підключено."
    GeoStatus.PermissionDenied -> "дозвіл відхилено; можна ввести початкову адресу вручну."
    GeoStatus.ServiceDisabled -> "службу місцезнаходження вимкнено; можна ввести адресу вручну."
    GeoStatus.Acquiring -> "очікуємо на актуальну позицію; остання відома не вважається поточною."
    GeoStatus.FreshFix -> "позицію отримано від підключеного native provider."
    GeoStatus.StaleFix -> "позиція застаріла; підтвердьте початкову адресу вручну."
}
