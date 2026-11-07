package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthenticatedAccountScreen(
    state: AuthenticatedAccountState,
    dispatch: (AuthenticatedAccountEvent) -> Unit,
    themeSettings: ThemeSettingsViewModel,
    onLogout: () -> Unit,
    mapFactory: PlatformMapFactory,
    routeService: RoutePlanningService,
    locationProvider: LocationProvider,
    onToggleTheme: () -> Unit,
) {
    val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory)
    val mapState by mapViewModel.bloc.state.collectAsStateWithLifecycle()
    val routeViewModel: RouteViewModel = viewModel(factory = routeViewModelFactory(routeService))
    val routeState by routeViewModel.bloc.state.collectAsStateWithLifecycle()
    val geoViewModel: GeoViewModel = viewModel(factory = geoViewModelFactory(locationProvider))
    val geoState by geoViewModel.bloc.state.collectAsStateWithLifecycle()
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        geoViewModel.bloc.dispatch(GeoEvent.Refresh)
        onPauseOrDispose {}
    }
    AuthBackHandler {
        if (state.section != DemoSection.Map || !routeViewModel.bloc.handleBack()) {
            dispatch(AuthenticatedAccountEvent.OpenSection(DemoSection.Map))
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("authenticated-account")) {
        val layout = AccountLayout.forWindow(maxWidth.value, maxHeight.value)
        val navigate: (DemoSection) -> Unit = {
            dispatch(AuthenticatedAccountEvent.OpenSection(it))
        }
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            MapTopBar(
                demoMode = false,
                preview = false,
                vehicleMode = "Акаунт",
                onBack = {},
                onOpenStopPlaces = {
                    dispatch(AuthenticatedAccountEvent.OpenSection(DemoSection.Map))
                    mapViewModel.bloc.dispatch(MapEvent.PlacesRequested)
                },
                onToggleTheme = onToggleTheme,
            )
            Row(Modifier.weight(1f).fillMaxWidth()) {
                if (layout.landscape) BottomNavigation(
                    selectedSection = state.section,
                    onSectionChange = navigate,
                    rail = true,
                    compactRail = !layout.tablet,
                    accountStyle = true,
                )
                if (state.section == DemoSection.Map) {
                    AuthenticatedMapScreen(
                        factory = mapFactory,
                        state = mapState,
                        dispatch = mapViewModel.bloc::dispatch,
                        routeState = routeState,
                        routeDispatch = routeViewModel.bloc::dispatch,
                        geoState = geoState,
                        geoDispatch = geoViewModel.bloc::dispatch,
                        expired = state.expired,
                        onProfile = { navigate(DemoSection.Profile) },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                    )
                } else Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(state.section.label, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        state.profile?.email ?: "Авторизований акаунт",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (state.loading && state.section == DemoSection.Profile) {
                        CircularProgressIndicator(Modifier.testTag("authenticated-profile-loading"))
                        Text("Завантаження профілю…")
                    }
                    if (state.error.isNotEmpty() && state.section == DemoSection.Profile) {
                        Text(
                            state.error, Modifier.testTag("authenticated-profile-error"),
                            color = MaterialTheme.colorScheme.error,
                        )
                        if (!state.expired) Button(
                            onClick = { dispatch(AuthenticatedAccountEvent.RetryProfile) },
                            enabled = !state.loading,
                            modifier = Modifier.testTag("retry-authenticated-profile"),
                        ) { Text("Повторити завантаження профілю") }
                    }
                    when (state.section) {
                        DemoSection.Profile -> state.profile?.let { AuthenticatedProfileScreen(it) }
                        DemoSection.Garage -> {
                            Text("Гараж недоступний", style = MaterialTheme.typography.titleLarge)
                            Text("Сервіс транспорту акаунта ще не підключено. Список автомобілів не завантажено; це не означає, що гараж порожній.")
                        }
                        DemoSection.Settings -> {
                            Text("Тема застосунку · на цьому пристрої")
                            ThemePreferenceOptions(themeSettings)
                            Text("Онлайн-налаштування, організації та інтеграції ще не підключено.")
                        }
                        else -> Unit
                    }
                    if (state.section == DemoSection.Profile) Button(
                        onClick = onLogout,
                        modifier = Modifier.testTag("authenticated-logout"),
                    ) { Text("Вийти з акаунта") }
                }
            }
            if (!layout.landscape) BottomNavigation(
                selectedSection = state.section,
                onSectionChange = navigate,
            )
        }
    }
}
