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
    Preview
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

    fun cancel() = copy(query = "")

    companion object {
        val Saver = listSaver<AddressSearchState, String>(
            save = { listOf(it.origin, it.destination, it.target.name, it.query) },
            restore = { AddressSearchState(it[0], it[1], SearchTarget.valueOf(it[2]), it[3]) }
        )
    }
}

internal data class DemoVehicleProfile(
    val id: String,
    val name: String,
    val category: String,
    val specification: String,
    val artwork: DemoVehicleArtwork
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
        if (sectionNavigation.section != DemoSection.Map && demoMode) {
            DemoAccountScreen(
                section = sectionNavigation.section,
                profile = DemoProfileState(vehicleClass, vehicleMode, selectedVehicleId),
                themeSettings = themeSettings,
                onSectionChange = { sectionNavigation = sectionNavigation.open(it) },
                onBack = { sectionNavigation = sectionNavigation.back() },
                onSelectVehicle = { vehicle ->
                    val selection = selectGarageVehicle(selectedVehicleId, vehicle)
                    if (selection.changed) {
                        vehicleClass = selection.vehicleClass
                        selectedVehicleId = selection.vehicleId
                        screenName = AppScreen.Map.name
                        sheetName = SheetLevel.Collapsed.name
                        sectionNavigation = sectionNavigation.back()
                        notice =
                            "Обрано наявний демопрофіль ${vehicle.name}. Його характеристики не підтверджують право власності."
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
                sheetLevel = sheetLevel,
                geoStatus = geoStatus,
                notice = notice,
                vehicleClass = vehicleClass,
                selectedVehicle = demoVehicleById(selectedVehicleId),
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
                onUseLocation = {
                    geoStatusName = GeoStatus.NotConfigured.name
                    notice = geoStatusMessage(GeoStatus.NotConfigured)
                },
                onSheetLevelChange = { sheetName = it.name },
                onPreview = {
                    val blockMessage = routePreviewBlockMessage(origin, destination, vehicleClass)
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

            AppScreen.Search -> SearchScreen(
                demoMode = demoMode,
                origin = origin,
                destination = destination,
                vehicleClass = vehicleClass,
                selectedVehicle = demoVehicleById(selectedVehicleId),
                target = target,
                query = query,
                status = searchStatus,
                vehicleMode = vehicleMode,
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
                    addresses = addresses.select(place)
                    notice =
                        "Демо-адресу вибрано; геокодування й маршрут провайдером не перевірялися."
                    screenName = AppScreen.Map.name
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
