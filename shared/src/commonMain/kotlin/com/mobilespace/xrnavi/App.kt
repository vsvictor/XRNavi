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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel

internal enum class AppScreen {
    SignIn,
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
    var darkOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = darkOverride ?: systemDarkTheme
    LaunchedEffect(darkTheme) {
        onThemeChanged(darkTheme)
    }
    XrnaviTheme(darkTheme = darkTheme) {
        XrnaviApplication(
            onToggleTheme = {
                darkOverride = !(darkOverride ?: systemDarkTheme)
            }
        )
    }
}

@Composable
private fun XrnaviApplication(onToggleTheme: () -> Unit) {
    val signInViewModel: SignInViewModel = viewModel(factory = SignInViewModelFactory)
    var screenName by rememberSaveable {
        mutableStateOf(initialScreenForSession(SessionProbe.hasConfiguredSession()).name)
    }
    var previousScreenName by rememberSaveable { mutableStateOf(AppScreen.Map.name) }
    var demoMode by rememberSaveable { mutableStateOf(false) }
    var origin by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var sheetName by rememberSaveable { mutableStateOf(SheetLevel.Collapsed.name) }
    var targetName by rememberSaveable { mutableStateOf(SearchTarget.Destination.name) }
    var query by rememberSaveable { mutableStateOf("") }
    var searchStatusName by rememberSaveable { mutableStateOf(SearchStatus.Idle.name) }
    var searchRequest by rememberSaveable { mutableIntStateOf(0) }
    var geoStatusName by rememberSaveable { mutableStateOf(GeoStatus.NotConfigured.name) }
    var notice by rememberSaveable { mutableStateOf("") }
    var vehicleClass by rememberSaveable { mutableStateOf("Легкове") }
    var vehicleMode by rememberSaveable { mutableStateOf("Особистий") }
    val screen = AppScreen.valueOf(screenName)
    val sheetLevel = SheetLevel.valueOf(sheetName)
    val target = SearchTarget.valueOf(targetName)
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
        when (screen) {
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
                    screenName = AppScreen.Map.name
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
                vehicleMode = vehicleMode,
                onVehicleClassChange = { vehicleClass = it },
                onOpenSearch = { searchTarget ->
                    previousScreenName = screenName
                    targetName = searchTarget.name
                    query = ""
                    searchStatusName = SearchStatus.Idle.name
                    screenName = AppScreen.Search.name
                },
                onUseLocation = {
                    geoStatusName = GeoStatus.NotConfigured.name
                    notice = geoStatusMessage(GeoStatus.NotConfigured)
                },
                onSheetLevelChange = { sheetName = it.name },
                onPreview = {
                    if (origin.isBlank() || destination.isBlank()) {
                        notice = "Вкажіть початкову та кінцеву точки, щоб переглянути демо-маршрут."
                    } else {
                        notice = ""
                        screenName = AppScreen.Preview.name
                    }
                },
                onStartRoute = {
                    notice = if (vehicleClass != "Легкове") {
                        "Для цього типу транспорту сумісність маршруту не перевірена. Запуск вимкнено."
                    } else {
                        "Маршрут і навігацію не підключено. Це лише демонстраційний перегляд."
                    }
                    sheetName = SheetLevel.Expanded.name
                },
                onBack = {
                    screenName = if (screen == AppScreen.Preview) {
                        AppScreen.Map.name
                    } else {
                        notice = ""
                        AppScreen.SignIn.name
                    }
                },
                onToggleTheme = onToggleTheme
            )

            AppScreen.Search -> SearchScreen(
                demoMode = demoMode,
                origin = origin,
                destination = destination,
                target = target,
                query = query,
                status = searchStatus,
                vehicleMode = vehicleMode,
                onVehicleModeChange = { vehicleMode = it },
                onTargetChange = {
                    query = searchQueryAfterTargetChange(target, it, query)
                    targetName = it.name
                    searchStatusName = SearchStatus.Idle.name
                },
                onQueryChange = {
                    query = it
                    searchStatusName = SearchStatus.Idle.name
                },
                onSearch = { searchRequest += 1 },
                onBack = {
                    screenName = previousScreenName
                    query = ""
                },
                onPickMap = {
                    notice = "Вибір координат на мапі не підключено. Введіть адресу вручну."
                    screenName = AppScreen.Map.name
                },
                onSelectPlace = { place ->
                    if (target == SearchTarget.Origin) {
                        origin = place.title
                    } else {
                        destination = place.title
                    }
                    notice =
                        "Демо-адресу вибрано; геокодування й маршрут провайдером не перевірялися."
                    screenName = if (origin.isNotBlank() && destination.isNotBlank()) {
                        AppScreen.Preview.name
                    } else {
                        previousScreenName
                    }
                    query = ""
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
