package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AuthenticatedAccountScreen(
    state: AuthenticatedAccountState,
    dispatch: (AuthenticatedAccountEvent) -> Unit,
    themeSettings: ThemeSettingsViewModel,
    onLogout: () -> Unit,
    mapFactory: PlatformMapFactory,
    routeService: RoutePlanningService,
    garageService: GarageService,
    vehiclePhotoService: VehiclePhotoService,
    vehiclePhotoSelections: VehiclePhotoSelections,
    sessionProvider: () -> AuthSession?,
    garageViewModel: GarageViewModel,
    locationProvider: LocationProvider,
    onToggleTheme: () -> Unit,
    onOpenSavedPlaces: () -> Unit,
) {
    val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory)
    val mapState by mapViewModel.bloc.state.collectAsStateWithLifecycle()
    val routeViewModel: RouteViewModel = viewModel(factory = routeViewModelFactory(routeService))
    val routeState by routeViewModel.bloc.state.collectAsStateWithLifecycle()
    val garageState by garageViewModel.bloc.state.collectAsStateWithLifecycle()
    val addViewModel: AddVehicleViewModel =
        viewModel(factory = addVehicleViewModelFactory(garageService, sessionProvider))
    val addState by addViewModel.bloc.state.collectAsStateWithLifecycle()
    androidx.compose.runtime.DisposableEffect(addViewModel) {
        onDispose {
            addViewModel.bloc.dispatch(AddVehicleEvent.Close)
        }
    }
    androidx.compose.runtime.LaunchedEffect(state.section) {
        when (state.section) {
            DemoSection.Profile -> garageViewModel.bloc.dispatch(GarageEvent.ProfileOpened)
            DemoSection.Garage -> if (
                !garageViewModel.bloc.state.value.loaded && !garageViewModel.bloc.state.value.loading
            ) garageViewModel.bloc.dispatch(GarageEvent.Load)
            else -> Unit
        }
    }
    androidx.compose.runtime.LaunchedEffect(addState.added) {
        if (addState.added != null) garageViewModel.bloc.dispatch(GarageEvent.Load)
    }
    val detailsViewModel: VehicleDetailsViewModel =
        viewModel(factory = VehicleDetailsViewModelFactory)
    val detailsState by detailsViewModel.bloc.state.collectAsStateWithLifecycle()
    val photosViewModel: VehiclePhotosViewModel =
        viewModel(factory = remember(vehiclePhotoService, vehiclePhotoSelections) {
            vehiclePhotosViewModelFactory(vehiclePhotoService, vehiclePhotoSelections)
        })
    val photosState by photosViewModel.bloc.state.collectAsStateWithLifecycle()
    val photoQuery = if (state.section != DemoSection.Garage) null else if (addState.open) {
        val make = addState.selectedMake?.name
        val model = addState.selectedModel?.name
        val year = addState.year
        if (make != null && model != null && year != null) VehiclePhotoQuery(make, model, year) else null
    } else garageState.vehicles.firstOrNull { it.id == detailsState.vehicleId }?.photoQuery()
    val photoVehicleId = if (state.section != DemoSection.Garage) null
        else if (addState.open) addState.added?.id ?: addState.editingVehicle?.id
        else detailsState.vehicleId
    val photoDraft = state.section == DemoSection.Garage && addState.open &&
        addState.added == null && addState.editingVehicle == null
    val photoAccountId = state.profile?.id
    androidx.compose.runtime.LaunchedEffect(photoQuery, photoVehicleId, photoDraft, photoAccountId) {
        photosViewModel.bloc.dispatch(VehiclePhotosEvent.Open(photoQuery, photoAccountId, photoVehicleId, photoDraft))
    }
    val photoSelectionKey = if (photoDraft) "draft" else photoVehicleId
    val photosContextMatches = photosState.query == photoQuery &&
        photosState.selectionKey == photoSelectionKey && photosState.accountId == photoAccountId
    val visiblePhotosState = if (photosContextMatches) photosState
        else VehiclePhotosState(query = photoQuery, selectionKey = "pending:$photoSelectionKey", accountId = photoAccountId)
    val geoViewModel: GeoViewModel = viewModel(factory = geoViewModelFactory(locationProvider))
    val geoState by geoViewModel.bloc.state.collectAsStateWithLifecycle()
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        geoViewModel.bloc.dispatch(GeoEvent.Refresh)
        onPauseOrDispose {}
    }
    AuthBackHandler {
        if (state.section == DemoSection.Garage && addViewModel.bloc.handleBack()) {
        } else if (state.section == DemoSection.Garage && detailsViewModel.bloc.handleBack()) {
        } else if (state.section == DemoSection.AccountSecurity) {
            dispatch(AuthenticatedAccountEvent.OpenSection(DemoSection.Profile))
        } else if (state.section == DemoSection.Profile) {
            dispatch(AuthenticatedAccountEvent.BackFromProfile)
        } else if (state.section != DemoSection.Map || !routeViewModel.bloc.handleBack()) {
            dispatch(AuthenticatedAccountEvent.OpenSection(DemoSection.Map))
        }
    }
    BoxWithConstraints(
        Modifier.fillMaxSize().testTag("authenticated-account")
    ) {
        val layout = AccountLayout.forWindow(maxWidth.value, maxHeight.value)
        val detailVehicle = garageState.vehicles.firstOrNull { it.id == detailsState.vehicleId }
        val navigate: (DemoSection) -> Unit = {
            dispatch(AuthenticatedAccountEvent.OpenSection(it))
        }
        var headerHeightPx by remember { mutableIntStateOf(0) }
        val garageList = state.section == DemoSection.Garage && !addState.open && detailVehicle == null
        val overlayHeader = state.section == DemoSection.Map || (garageList && layout.landscape)
        val headerHeight = with(LocalDensity.current) { headerHeightPx.toDp() }
        Column(Modifier.fillMaxSize()) {
            if (state.section != DemoSection.Garage &&
                !overlayHeader && state.section != DemoSection.Profile && state.section != DemoSection.AccountSecurity
            ) {
                MapTopBar(
                    demoMode = false,
                    preview = false,
                    vehicleMode = "Аккаунт",
                    onBack = {},
                    onOpenStopPlaces = {
                        dispatch(AuthenticatedAccountEvent.OpenSection(DemoSection.Map))
                        mapViewModel.bloc.dispatch(MapEvent.PlacesRequested)
                    },
                    onToggleTheme = onToggleTheme,
                )
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
            Row(Modifier.fillMaxSize()) {
                if (layout.landscape) BottomNavigation(
                    selectedSection = state.section,
                    onSectionChange = navigate,
                    rail = true,
                    compactRail = !layout.tablet,
                    accountStyle = true,
                    topPadding = if (overlayHeader) headerHeight else 0.dp,
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
                        headerHeight = headerHeight,
                    )
                } else if (state.section == DemoSection.Profile) Column(
                    Modifier.weight(1f).fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .accountPageBackground().padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(Modifier.testTag("authenticated-profile-loading"))
                        Text("Завантаження профілю…")
                    }
                    if (state.error.isNotEmpty()) {
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
                    state.profile?.let {
                        AuthenticatedProfileScreen(
                            profile = it,
                            layout = layout,
                            garageState = garageState,
                            onOpenGarage = { navigate(DemoSection.Garage) },
                            onRetryGarage = { garageViewModel.bloc.dispatch(GarageEvent.Load) },
                            onOpenVehicleDetails = { vehicleId ->
                                detailsViewModel.bloc.dispatch(VehicleDetailsEvent.Open(vehicleId))
                                navigate(DemoSection.Garage)
                            },
                            onOpenSecurity = { navigate(DemoSection.AccountSecurity) },
                            onOpenFavorites = onOpenSavedPlaces,
                            selectedTab = state.profileTab,
                            onSelectTab = { dispatch(AuthenticatedAccountEvent.SelectProfileTab(it)) },
                            onLogout = onLogout,
                        )
                    }
                    if (state.profile == null) Column(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    ) {
                        ProfileLogoutButton(onLogout)
                    }
                } else if (state.section == DemoSection.AccountSecurity) Column(
                    Modifier.weight(1f).fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .accountPageBackground(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.profile?.let { profile ->
                        AuthenticatedAccountSecurityScreen(
                            profile = profile,
                            onBack = { navigate(DemoSection.Profile) },
                        )
                    }
                } else if (state.section == DemoSection.Garage) Column(
                    Modifier.weight(1f).fillMaxWidth().testTag("authenticated-garage")
                        .verticalScroll(rememberScrollState())
                        .accountPageBackground()
                        .padding(
                            start = if (garageList && layout.landscape) 20.dp else 0.dp,
                            end = if (garageList && layout.landscape) 20.dp else 0.dp,
                            top = if (garageList && layout.landscape) 16.dp else 0.dp,
                            bottom = 16.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (addState.open) {
                        AccountPageLayout(
                            title = if (addState.editingVehicle == null) "Додати авто" else "Редагування авто",
                            onBack = { addViewModel.bloc.handleBack() },
                            hero = {
                                VehiclePhotoHero(
                                    title = if (addState.editingVehicle == null) "Додати авто" else "Редагування авто",
                                    onBack = { addViewModel.bloc.handleBack() },
                                    state = visiblePhotosState,
                                    dispatch = { if (photosContextMatches) photosViewModel.bloc.dispatch(it) },
                                )
                            },
                        ) {
                            AddVehicleScreen(
                                addState,
                                addViewModel.bloc::dispatch,
                                landscape = layout.landscape,
                                compactLandscape = !layout.tablet,
                            )
                        }
                    } else if (detailVehicle != null) {
                        AccountPageLayout(
                            title = "Профіль транспорту",
                            onBack = { detailsViewModel.bloc.dispatch(VehicleDetailsEvent.Close) },
                            hero = {
                                VehiclePhotoHero(
                                    title = "Профіль транспорту",
                                    onBack = { detailsViewModel.bloc.dispatch(VehicleDetailsEvent.Close) },
                                    state = visiblePhotosState,
                                    dispatch = { if (photosContextMatches) photosViewModel.bloc.dispatch(it) },
                                )
                            },
                        ) {
                            VehicleDetailsScreen(
                                detailVehicle,
                                landscape = layout.landscape,
                                compactLandscape = !layout.tablet,
                            )
                        }
                    } else {
                        GarageScreen(
                            garageState,
                            garageViewModel.bloc::dispatch,
                            onAddVehicle = { addViewModel.bloc.dispatch(AddVehicleEvent.Open) },
                            onOpenDetails = {
                                detailsViewModel.bloc.dispatch(VehicleDetailsEvent.Open(it))
                            },
                            onEditVehicle = { addViewModel.bloc.dispatch(AddVehicleEvent.Edit(it)) },
                            landscape = layout.landscape,
                            headerHeight = headerHeight,
                            vehiclePhotos = vehiclePhotoSelections.selections.collectAsStateWithLifecycle().value[photoAccountId].orEmpty(),
                        )
                    }
                } else Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(state.section.label, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        state.profile?.email ?: "Авторизований акаунт",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    when (state.section) {
                        DemoSection.Settings -> {
                            Text("Тема застосунку · на цьому пристрої")
                            ThemePreferenceOptions(themeSettings)
                            Text("Онлайн-налаштування, організації та інтеграції ще не підключено.")
                        }

                        else -> Unit
                    }
                }
            }
                if (overlayHeader) {
                    Box(
                        Modifier.fillMaxWidth().align(Alignment.TopCenter)
                            .onSizeChanged { headerHeightPx = it.height }
                    ) {
                        if (state.section == DemoSection.Garage) {
                            GarageListHeader()
                        } else {
                            MapTopBar(
                                demoMode = false,
                                preview = false,
                                vehicleMode = "Аккаунт",
                                onBack = {},
                                onOpenStopPlaces = {
                                    dispatch(AuthenticatedAccountEvent.OpenSection(DemoSection.Map))
                                    mapViewModel.bloc.dispatch(MapEvent.PlacesRequested)
                                },
                                onToggleTheme = onToggleTheme,
                                overlay = true,
                            )
                    }
                }
                }
            }
            if (!layout.landscape) BottomNavigation(
                selectedSection = state.section,
                onSectionChange = navigate,
            )
        }
        if (state.section == DemoSection.Garage &&
            (garageState.loading || addState.loading || addState.vinLoading ||
                    addState.equipment.loading || addState.equipment.bodiesLoading ||
                    addState.equipment.enginesLoading || addState.equipment.mileagesLoading)
        ) {
            GarageLoadingOverlay()
        }
    }
}
