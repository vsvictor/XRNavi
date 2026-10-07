package com.mobilespace.xrnavi.presentation.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.presentation.navigation.*

@Composable
internal fun ScreenHost(appViewModel: AppViewModel, entry: NavigationEntry) {
    val screenModel = viewModel<ViewModel>(
        viewModelStoreOwner = appViewModel.owner(entry),
        key = entry.id.toString()
    ) {
        appViewModel.container.create(entry)
    }
    when (entry.destination) {
        AppDestination.SignIn -> SignInScreen(
            viewModel = screenModel as SignInViewModel,
            onRegister = { appViewModel.open(AppDestination.Registration) },
            onAuthenticated = appViewModel::authenticated,
        )

        AppDestination.Registration -> RegistrationScreen(
            viewModel = screenModel as RegistrationViewModel,
            onSignIn = appViewModel::back,
            onJoinOrganization = { appViewModel.open(AppDestination.OrganizationInvitation) },
        )

        AppDestination.OrganizationInvitation -> OrganizationInvitationScreen(
            viewModel = screenModel as OrganizationInvitationViewModel,
            onBack = appViewModel::back,
            onDecline = appViewModel::back,
            onAccepted = { appViewModel.open(AppDestination.OrganizationSettings) },
        )

        AppDestination.Route -> RouteScreen(
            viewModel = screenModel as RouteViewModel,

            onOpenSettings = { appViewModel.open(AppDestination.RouteSettings) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenPreferences = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
            onStartRoute = { appViewModel.open(AppDestination.ActiveNavigation) },
            onOpenParkingStop = { appViewModel.open(AppDestination.ParkingStop) },
            onOpenWorkTrip = {

                appViewModel.open(AppDestination.WorkTasks)
            },
            onOpenNearbyStops = { appViewModel.open(AppDestination.NearbyStops) },
            onOpenAddressSearch = { appViewModel.open(AppDestination.AddressSearch) },
            onOpenTripPlan = {

                appViewModel.open(
                    AppDestination.TripPlan,
                    appViewModel.container.session.state.value.selectedTask?.id
                )
            },
        )

        AppDestination.AddressSearch -> AddressSearchScreen(
            viewModel = screenModel as AddressSearchViewModel,

            onBack = appViewModel::back,
            onSelectAddress = { appViewModel.back() },
            onChooseOnMap = { },
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.NearbyStops -> NearbyStopsScreen(
            viewModel = screenModel as NearbyStopsViewModel,
            onBack = appViewModel::back,
            onOpenParkingDetails = { appViewModel.open(AppDestination.ParkingStop) },
            onOpenFilters = { appViewModel.open(AppDestination.RouteSettings) },
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.WorkTasks -> WorkTasksScreen(
            viewModel = screenModel as WorkTasksViewModel,
            onOpenTaskWithId = { appViewModel.open(AppDestination.WorkTripDetails, it) },
            onBack = appViewModel::back,
            onOpenTask = {

                appViewModel.open(AppDestination.WorkTripDetails)
            },
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.WorkTripDetails -> WorkTripDetailsScreen(
            viewModel = screenModel as WorkTripDetailsViewModel,
            onOpenTaskRoute = { appViewModel.open(AppDestination.Route, it) },
            onOpenTripPlanWithId = { appViewModel.open(AppDestination.TripPlan, it) },
            onBack = appViewModel::back,
            onOpenTripPlan = {

                appViewModel.open(
                    AppDestination.TripPlan,
                    appViewModel.container.session.state.value.selectedTask?.id
                )
            },
        )

        AppDestination.TripPlan -> TripPlanScreen(
            viewModel = screenModel as TripPlanViewModel,
            onBack = appViewModel::back,
        )

        AppDestination.ParkingStop -> ParkingStopScreen(
            viewModel = screenModel as ParkingStopViewModel,
            onBack = appViewModel::back,
        )

        AppDestination.TripSummary -> TripSummaryScreen(
            viewModel = screenModel as TripSummaryViewModel,
            onOpenTripHistory = { appViewModel.open(AppDestination.TripHistory) },
            onBack = appViewModel::back,
            onReturnToMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenPreferences = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.Garage -> GarageScreen(
            viewModel = screenModel as GarageViewModel,
            onBackToMap = { appViewModel.tab(AppDestination.Route) },
            onOpenVehicleProfile = { appViewModel.open(AppDestination.VehicleProfile) },
            onAddVehicle = { appViewModel.open(AppDestination.AddVehicle) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.FordConfiguration -> FordConfigurationScreen(
            viewModel = screenModel as FordConfigurationViewModel,
            onValidateParameters = { appViewModel.open(AppDestination.VehicleProfile) },
            onBack = appViewModel::back,
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.AddVehicle -> AddVehicleScreen(
            viewModel = screenModel as AddVehicleViewModel,
            onBack = appViewModel::back,
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
            onSelectFord = { appViewModel.open(AppDestination.FordConfiguration) },
        )

        AppDestination.Preferences -> PreferencesScreen(
            viewModel = screenModel as PreferencesViewModel,
            onBack = appViewModel::back,
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
            onOpenOrganization = { appViewModel.open(AppDestination.OrganizationSettings) },
            onOpenOfflineMaps = { appViewModel.open(AppDestination.OfflineMaps) },
            onOpenDeviceDiagnostics = { appViewModel.open(AppDestination.DeviceDiagnostics) },
            onOpenPrivacy = {

                appViewModel.open(AppDestination.Privacy)
            },
        )

        AppDestination.DeviceDiagnostics -> DeviceDiagnosticsScreen(
            viewModel = screenModel as DeviceDiagnosticsViewModel,
            onBack = appViewModel::back,
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.OfflineMaps -> OfflineMapsScreen(
            viewModel = screenModel as OfflineMapsViewModel,
            onBack = appViewModel::back,
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.OrganizationSettings -> OrganizationSettingsScreen(
            viewModel = screenModel as OrganizationSettingsViewModel,
            onOpenWorkTasks = { appViewModel.open(AppDestination.WorkTasks) },
            onBack = appViewModel::back,
        )

        AppDestination.Profile -> ProfileScreen(
            viewModel = screenModel as ProfileViewModel,
            onOpenInvitation = { appViewModel.open(AppDestination.OrganizationInvitation) },
            onBack = appViewModel::back,
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenPreferences = { appViewModel.tab(AppDestination.Preferences) },
            onOpenOrganization = { appViewModel.open(AppDestination.OrganizationSettings) },
            onOpenTripHistory = { appViewModel.open(AppDestination.TripHistory) },
            onOpenWorkTasks = {

                appViewModel.open(AppDestination.WorkTasks)
            },
            onOpenPrivacy = {

                appViewModel.open(AppDestination.Privacy)
            },
        )

        AppDestination.Privacy -> PrivacyScreen(
            viewModel = screenModel as PrivacyViewModel,
            onBack = appViewModel::back,
            onDeleteAccount = { appViewModel.open(AppDestination.DeleteAccount) },
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.DeleteAccount -> DeleteAccountScreen(
            viewModel = screenModel as AccountDeletionViewModel,
            onBack = appViewModel::back,
            onCancel = appViewModel::back,
        )

        AppDestination.TripHistory -> TripHistoryScreen(
            viewModel = screenModel as TripHistoryViewModel,
            onBack = appViewModel::back,
            onOpenMap = { appViewModel.tab(AppDestination.Route) },
            onOpenGarage = { appViewModel.tab(AppDestination.Garage) },
            onOpenSettings = { appViewModel.tab(AppDestination.Preferences) },
            onOpenProfile = { appViewModel.tab(AppDestination.Profile) },
        )

        AppDestination.VehicleProfile -> VehicleProfileScreen(
            viewModel = screenModel as VehicleProfileViewModel,
            onBack = appViewModel::back,
            onOpenConfigurations = { appViewModel.open(AppDestination.VehicleConfigurations) },
        )

        AppDestination.VehicleConfigurations -> VehicleConfigurationsScreen(
            viewModel = screenModel as VehicleConfigurationsViewModel,
            onBack = appViewModel::back,
            onOpenHelp = { appViewModel.open(AppDestination.VehicleProfile) },
        )

        AppDestination.RouteSettings -> RouteSettingsScreen(
            viewModel = screenModel as RouteSettingsViewModel,
            onBack = appViewModel::back,
            onApply = appViewModel::back,
        )

        AppDestination.ActiveNavigation -> ActiveNavigationScreen(
            viewModel = screenModel as ActiveNavigationViewModel,
            onOpenNearbyStops = { appViewModel.open(AppDestination.NearbyStops) },
            onStop = appViewModel::finishPreview,
            onOverview = { appViewModel.tab(AppDestination.Route) },
            onOpenXr = { appViewModel.open(AppDestination.XrNavigation) },
        )

        AppDestination.XrNavigation -> XrNavigationScreen(
            viewModel = screenModel as XrNavigationViewModel,
            onBackToMap = appViewModel::back,
        )
    }
}
