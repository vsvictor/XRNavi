package com.mobilespace.xrnavi

import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun XrnaviApplication(
    dependencies: AppDependencies,
    themeSettings: ThemeSettingsViewModel,
    onToggleTheme: () -> Unit,
) {
    val accountViewModel: AuthenticatedAccountViewModel = viewModel(
        factory = remember(dependencies) {
            authenticatedAccountViewModelFactory(
                dependencies.authenticatedProfileService,
                dependencies.sessionManager,
            )
        }
    )
    val accountBloc = accountViewModel.bloc
    val accountState by accountBloc.state.collectAsStateWithLifecycle()
    LaunchedEffect(accountBloc) { accountBloc.dispatch(AuthenticatedAccountEvent.Restore) }
    val signInViewModel: SignInViewModel = viewModel(
        factory = remember(dependencies, accountBloc) {
            signInViewModelFactory(dependencies.signInAuthService) {
                accountBloc.dispatch(AuthenticatedAccountEvent.LoginCompleted(it))
            }
        }
    )
    val signInBloc = signInViewModel.bloc
    val signInState by signInBloc.state.collectAsStateWithLifecycle()
    val selectEmailAccount = rememberEmailAccountPicker(
        onSelected = { signInBloc.dispatch(SignInEvent.AccountSelected(it)) },
        onFailure = { signInBloc.dispatch(SignInEvent.AccountSelectionFailed(it)) },
    )
    LaunchedEffect(signInState.accountPickerRequested) {
        if (signInState.accountPickerRequested) {
            signInBloc.dispatch(SignInEvent.AccountPickerLaunched)
            selectEmailAccount()
        }
    }
    val registrationViewModel: RegistrationViewModel = viewModel(
        factory = remember(dependencies.registrationService) {
            registrationViewModelFactory(dependencies.registrationService)
        }
    )
    val previewEnabled = internalAuthPreviewEnabled()
    val recoveryProgressStore = rememberRecoveryProgressStore()
    val recoveryViewModel: RecoveryViewModel = viewModel(
        factory = remember(previewEnabled, dependencies.recoveryService, recoveryProgressStore) {
            recoveryViewModelFactory(previewEnabled, dependencies.recoveryService, recoveryProgressStore)
        }
    )
    val callbackViewModel: CallbackViewModel = viewModel(
        factory = remember(dependencies) { callbackViewModelFactory(dependencies.callbackService) }
    )
    val callbackState by callbackViewModel.bloc.state.collectAsStateWithLifecycle()
    LaunchedEffect(accountState.mode) {
        callbackViewModel.bloc.dispatch(
            if (accountState.mode == AccountMode.Authenticated) CallbackEvent.Started else CallbackEvent.Stopped
        )
    }
    ResetTokenDialog(callbackState.resetToken) {
        callbackViewModel.bloc.dispatch(CallbackEvent.ResetTokenDismissed)
    }
    val savedPlacesStore = rememberSavedPlacesStore()
    val routeStopsStore = rememberRouteStopsStore()
    val appModel: DemoAppViewModel = viewModel(
        factory = remember(savedPlacesStore, routeStopsStore) {
            demoAppViewModelFactory(savedPlacesStore, routeStopsStore)
        }
    )
    val app by appModel.bloc.state.collectAsStateWithLifecycle()
    val dispatch = appModel::dispatch
    LaunchedEffect(registrationViewModel.registered) {
        if (registrationViewModel.registered) {
            registrationViewModel.registrationHandled()
            dispatch(DemoAppEvent.ScreenChanged(AppScreen.SignIn))
        }
    }
    val demoMode = accountState.mode == AccountMode.Demo
    val origin = app.addresses.origin
    val destination = app.addresses.destination
    val additionalModel: AdditionalNavigationViewModel = viewModel { AdditionalNavigationViewModel() }
    val additionalBloc = additionalModel.bloc
    val additionalState by additionalBloc.state.collectAsStateWithLifecycle()
    LaunchedEffect(additionalState.sectionTarget) {
        additionalState.sectionTarget?.let {
            dispatch(DemoAppEvent.SectionOpened(it))
            additionalBloc.dispatch(AdditionalNavigationEvent.SectionHandled)
        }
    }
    LaunchedEffect(accountState.restoring, accountState.mode) {
        if (!accountState.restoring && accountState.mode == AccountMode.SignedOut &&
            recoveryViewModel.stage == RecoveryStage.CheckEmail
        ) dispatch(DemoAppEvent.AuthScreenOpened(AppScreen.Recovery))
    }
    val screen = if (
        accountState.mode == AccountMode.SignedOut &&
        app.screen !in listOf(AppScreen.SignIn, AppScreen.Registration, AppScreen.Recovery)
    ) AppScreen.SignIn else app.screen

    LaunchedEffect(signInState.openMap) {
        if (signInState.openMap) {
            dispatch(DemoAppEvent.MapOpenedAfterSignIn)
            signInBloc.dispatch(SignInEvent.MapOpened)
        }
    }

    LaunchedEffect(app.searchRequest) {
        if (app.searchRequest == 0) return@LaunchedEffect
        dispatch(DemoAppEvent.SearchStatusChanged(SearchStatus.Loading))
        withFrameNanos { }
        val normalizedQuery = appModel.bloc.state.value.addresses.query.trim()
        dispatch(
            DemoAppEvent.SearchStatusChanged(
                when {
                    !demoMode -> SearchStatus.Error
                    normalizedQuery.isEmpty() -> SearchStatus.Results
                    demoPlaces.any {
                        it.title.contains(normalizedQuery, ignoreCase = true) ||
                                it.detail.contains(normalizedQuery, ignoreCase = true)
                    } -> SearchStatus.Results

                    else -> SearchStatus.Empty
                }
            )
        )
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
            accountBloc.dispatch(AuthenticatedAccountEvent.Logout)
            dispatch(DemoAppEvent.DemoExited)
        }
        if (accountState.restoring) {
            SessionRestoringScreen()
        } else if (screen == AppScreen.SavedPlaces && accountState.mode == AccountMode.Authenticated) {
            SavedPlacesScreen(
                places = app.savedPlaces,
                initialEditKey = app.savedPlacesEditKey,
                allowAddressSelection = false,
                allowDestinationUse = true,
                notice = app.savedPlacesNotice,
                onBack = { dispatch(DemoAppEvent.SavedPlacesClosed) },
                onSelectAddress = { dispatch(DemoAppEvent.SavedPlaceAddressSelected(it)) },
                onUseAsDestination = { dispatch(DemoAppEvent.SavedPlaceUsedAsDestination(it)) },
                onRename = appModel::renamePlace,
                onDelete = appModel::deletePlace,
                onToggleTheme = onToggleTheme
            )
        } else if (accountState.mode == AccountMode.Authenticated) {
            AuthenticatedAccountScreen(
                state = accountState,
                dispatch = accountBloc::dispatch,
                themeSettings = themeSettings,
                onLogout = exitDemo,
                mapFactory = dependencies.mapFactory,
                routeService = dependencies.routePlanningService,
                garageService = dependencies.garageService,
                sessionProvider = accountBloc::currentSession,
                locationProvider = dependencies.locationProvider,
                onToggleTheme = onToggleTheme,
                onOpenSavedPlaces = {
                    dispatch(DemoAppEvent.SavedPlacesOpened(SavedPlacesOrigin.Profile, null))
                },
            )
        } else if (additionalState.screen != null && demoMode) {
            AdditionalScreens(additionalState, additionalBloc::dispatch)
        } else if (
            app.sectionNavigation.section != DemoSection.Map &&
            demoMode &&
            screen != AppScreen.SavedPlaces
        ) {
            DemoAccountScreen(
                section = app.sectionNavigation.section,
                profile = DemoProfileState(app.vehicleClass, app.vehicleMode, app.selectedVehicleId),
                configurationSelections = app.configurationSelections,
                themeSettings = themeSettings,
                onSectionChange = { dispatch(DemoAppEvent.SectionOpened(it)) },
                savedPlacesCount = app.savedPlaces.size,
                onOpenSavedPlaces = {
                    dispatch(DemoAppEvent.SavedPlacesOpened(SavedPlacesOrigin.Profile, null))
                },
                onOpenAdditional = { additionalBloc.dispatch(AdditionalNavigationEvent.Open(it)) },
                onBack = { dispatch(DemoAppEvent.SectionBack) },
                onSelectVehicle = { dispatch(DemoAppEvent.GarageVehicleSelected(it)) },
                onApplyVehicleConfiguration = {
                    dispatch(DemoAppEvent.VehicleConfigurationApplied(it))
                },
                onExitDemo = exitDemo,
                onRegister = {
                    exitDemo()
                    dispatch(DemoAppEvent.ScreenChanged(AppScreen.Registration))
                },
                onRecovery = {
                    exitDemo()
                    dispatch(DemoAppEvent.ScreenChanged(AppScreen.Recovery))
                }
            )
        } else when (screen) {
            AppScreen.SignIn -> SignInScreen(
                email = signInState.email,
                onEmailChange = { signInBloc.dispatch(SignInEvent.EmailChanged(it)) },
                password = signInState.password,
                onPasswordChange = { signInBloc.dispatch(SignInEvent.PasswordChanged(it)) },
                passwordVisible = signInState.passwordVisible,
                onTogglePasswordVisibility = {
                    signInBloc.dispatch(SignInEvent.TogglePasswordVisibility)
                },
                onSelectAccount = { signInBloc.dispatch(SignInEvent.SelectAccount) },
                submitting = signInState.submitting,
                notice = signInState.notice,
                onSignIn = { signInBloc.dispatch(SignInEvent.Submit) },
                onDemo = {
                    signInViewModel.clearPassword()
                    accountBloc.dispatch(AuthenticatedAccountEvent.ContinueWithoutLogin)
                    dispatch(DemoAppEvent.DemoStarted)
                },
                onRegister = {
                    signInViewModel.clearPassword()
                    dispatch(DemoAppEvent.AuthScreenOpened(AppScreen.Registration))
                },
                onForgotPassword = {
                    signInViewModel.clearPassword()
                    recoveryViewModel.cancel()
                    dispatch(DemoAppEvent.AuthScreenOpened(AppScreen.Recovery))
                }
            )

            AppScreen.Registration -> RegistrationScreen(
                viewModel = registrationViewModel,
                onBack = {
                    registrationViewModel.cancel()
                    dispatch(DemoAppEvent.ScreenChanged(AppScreen.SignIn))
                }
            )

            AppScreen.Recovery -> RecoveryScreen(
                model = recoveryViewModel,
                previewEnabled = previewEnabled,
                onBack = {
                    recoveryViewModel.cancel()
                    dispatch(DemoAppEvent.ScreenChanged(AppScreen.SignIn))
                }
            )

            AppScreen.Map, AppScreen.Preview -> MapScreen(
                preview = screen == AppScreen.Preview,
                demoMode = demoMode,
                origin = origin,
                destination = destination,
                routeStops = app.routeStops.applied,
                sheetLevel = app.sheet,
                geoStatus = app.geoStatus,
                notice = app.notice,
                vehicleClass = app.vehicleClass,
                selectedVehicle = app.activeVehicle,
                vehicleMode = app.vehicleMode,
                onVehicleClassChange = { dispatch(DemoAppEvent.VehicleClassChanged(it)) },
                onOpenSearch = { dispatch(DemoAppEvent.SearchOpened(it)) },
                onOpenRouteStops = { dispatch(DemoAppEvent.RouteStopsOpened) },
                onOpenStopPlaces = {
                    dispatch(DemoAppEvent.StopPlacesDispatched(StopPlacesEvent.Open(StopPlacesSource.Map)))
                },
                onUseLocation = {
                    additionalBloc.dispatch(AdditionalNavigationEvent.Open(AdditionalScreen.LocationEducation))
                },
                onSheetLevelChange = { dispatch(DemoAppEvent.SheetLevelChanged(it)) },
                onPreview = { dispatch(DemoAppEvent.PreviewRequested) },
                onStartRoute = { dispatch(DemoAppEvent.RouteStarted) },
                onOpenRouteSettings = { dispatch(DemoAppEvent.RouteSettingsOpened) },
                onBack = {
                    if (screen == AppScreen.Preview) {
                        dispatch(DemoAppEvent.PreviewClosed)
                    } else {
                        signInViewModel.clearPassword()
                        accountBloc.dispatch(AuthenticatedAccountEvent.Logout)
                        dispatch(DemoAppEvent.MapLeft)
                    }
                },
                onSectionChange = { dispatch(DemoAppEvent.SectionOpened(it)) },
                onOpenAdditional = { additionalBloc.dispatch(AdditionalNavigationEvent.Open(it)) },
                onToggleTheme = onToggleTheme,
                onMapLayers = {
                    dispatch(
                        DemoAppEvent.NoticeShown(
                            "Шари мапи не підключено. Показано лише демонстраційний макет."
                        )
                    )
                }
            )

            AppScreen.RouteStops -> RouteStopsScreen(
                origin = origin.ifBlank { "Не обрано" },
                destination = destination.ifBlank { "Не обрано" },
                stops = app.routeStops.visibleStops,
                notice = app.routeStopsNotice,
                canSave = appModel.canSaveRouteStops,
                onAdd = { dispatch(DemoAppEvent.RouteStopAdded) },
                onReplace = { dispatch(DemoAppEvent.RouteStopReplaceRequested(it)) },
                onRemove = { dispatch(DemoAppEvent.RouteStopsEdited(RouteStopsEvent.RemoveStop(it))) },
                onMoveUp = { dispatch(DemoAppEvent.RouteStopsEdited(RouteStopsEvent.MoveUp(it))) },
                onMoveDown = { dispatch(DemoAppEvent.RouteStopsEdited(RouteStopsEvent.MoveDown(it))) },
                onSave = appModel::saveRouteStops,
                onCancel = { dispatch(DemoAppEvent.RouteStopsEditingCancelled) },
                onToggleTheme = onToggleTheme
            )

            AppScreen.RouteStopSearch -> {
                val searchStopId = app.routeStops.searchStopId
                if (searchStopId == null) {
                    LaunchedEffect(Unit) { dispatch(DemoAppEvent.ScreenChanged(AppScreen.RouteStops)) }
                } else {
                    RouteStopSearchScreen(
                        stopId = searchStopId,
                        query = app.routeStops.query,
                        savedPlaces = app.savedPlaces,
                        onQueryChange = {
                            dispatch(DemoAppEvent.RouteStopsEdited(RouteStopsEvent.UpdateQuery(it)))
                        },
                        onSelectDemoPlace = { place ->
                            dispatch(
                                DemoAppEvent.RouteStopSearchPlaceSelected(
                                    RouteStopsEvent.SelectDemoPlace(searchStopId, place)
                                )
                            )
                        },
                        onSelectSavedPlace = { place ->
                            dispatch(
                                DemoAppEvent.RouteStopSearchPlaceSelected(
                                    RouteStopsEvent.SelectSavedPlace(searchStopId, place)
                                )
                            )
                        },
                        onBack = { dispatch(DemoAppEvent.RouteStopSearchCancelled) },
                        onOpenStopPlaces = {
                            dispatch(
                                DemoAppEvent.StopPlacesDispatched(
                                    StopPlacesEvent.Open(StopPlacesSource.StopSearch)
                                )
                            )
                        },
                        onToggleTheme = onToggleTheme
                    )
                }
            }

            AppScreen.StopPlaces -> StopPlacesScreen(
                state = app.stopPlaces,
                onEvent = { dispatch(DemoAppEvent.StopPlacesDispatched(it)) },
                onToggleTheme = onToggleTheme,
                onSectionChange = { dispatch(DemoAppEvent.StopPlacesSectionOpened(it)) }
            )

            AppScreen.RouteSettings -> {
                val draft = app.routeSettingsDraft
                RouteSettingsScreen(
                    origin = origin,
                    destination = destination,
                    vehicle = app.activeVehicle,
                    preferences = draft.selected,
                    hasPendingChanges = draft.hasPendingChanges,
                    canApply = draft.vehicleId == app.selectedVehicleId &&
                            app.activeConfigurationAvailable,
                    notice = app.notice,
                    onPreferencesChange = { dispatch(DemoAppEvent.RoutePreferencesChanged(it)) },
                    onBack = { dispatch(DemoAppEvent.RouteSettingsClosed) },
                    onOpenGarage = { dispatch(DemoAppEvent.GarageOpenedFromRouteSettings) },
                    onOpenConfiguration = {
                        dispatch(DemoAppEvent.ConfigurationOpenedFromRouteSettings)
                    },
                    onApply = { dispatch(DemoAppEvent.RoutePreferencesApplied) },
                    onToggleTheme = onToggleTheme
                )
            }

            AppScreen.Search -> SearchScreen(
                demoMode = demoMode,
                origin = origin,
                destination = destination,
                vehicleClass = app.vehicleClass,
                selectedVehicle = app.activeVehicle,
                target = app.addresses.target,
                query = app.addresses.query,
                status = app.searchStatus,
                vehicleMode = app.vehicleMode,
                savedPlaceKeys = app.savedPlaces.mapTo(mutableSetOf()) { it.key },
                onVehicleModeChange = { dispatch(DemoAppEvent.VehicleModeChanged(it)) },
                onTargetChange = { dispatch(DemoAppEvent.SearchTargetChanged(it)) },
                onQueryChange = { dispatch(DemoAppEvent.SearchQueryChanged(it)) },
                onSearch = { dispatch(DemoAppEvent.SearchRequested) },
                onBack = { dispatch(DemoAppEvent.SearchClosed) },
                onPickMap = { dispatch(DemoAppEvent.MapPickRequested) },
                onSelectPlace = { dispatch(DemoAppEvent.DemoPlaceSelected(it)) },
                onSavePlace = appModel::saveDemoPlace,
                onOpenSavedPlaces = { editPlace ->
                    dispatch(DemoAppEvent.SavedPlacesOpened(SavedPlacesOrigin.Search, editPlace))
                },
                onSectionChange = { dispatch(DemoAppEvent.SearchSectionChanged(it)) },
                onToggleTheme = onToggleTheme
            )

            AppScreen.SavedPlaces -> SavedPlacesScreen(
                places = app.savedPlaces,
                initialEditKey = app.savedPlacesEditKey,
                allowAddressSelection = app.savedPlacesOrigin == SavedPlacesOrigin.Search,
                allowDestinationUse = app.savedPlacesOrigin == SavedPlacesOrigin.Profile,
                notice = app.savedPlacesNotice,
                onBack = { dispatch(DemoAppEvent.SavedPlacesClosed) },
                onSelectAddress = { dispatch(DemoAppEvent.SavedPlaceAddressSelected(it)) },
                onUseAsDestination = { dispatch(DemoAppEvent.SavedPlaceUsedAsDestination(it)) },
                onRename = appModel::renamePlace,
                onDelete = appModel::deletePlace,
                onToggleTheme = onToggleTheme
            )
        }
    }
}
