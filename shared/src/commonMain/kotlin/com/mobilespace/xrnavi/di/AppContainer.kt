package com.mobilespace.xrnavi.di

import androidx.lifecycle.ViewModel
import com.mobilespace.xrnavi.data.*
import com.mobilespace.xrnavi.domain.*
import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.presentation.navigation.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Explicit composition root. Replace adapters here when real services become available. */
class AppContainer(
    val session: SessionRepository = InMemorySessionRepository(
        SessionState(destination = "Львів, Площа Ринок, 1", organizationMember = true)),
    private val authentication: AuthenticationRepository = UnconfiguredAuthenticationRepository(),
    private val preferences: PreferencesRepository = InMemoryPreferencesRepository(),
    private val navigation: NavigationDemoRepository = DemoNavigationRepository(),
    private val vehicles: VehicleRepository = DemoVehicleRepository(),
    private val vehicleOperations: VehicleOperationsRepository = UnconfiguredVehicleConfigurationsService,
    private val deletion: AccountDeletionRepository = UnconfiguredAccountDeletionService(),
    private val offlineMaps: OfflineMapsRepository = UnconfiguredOfflineMapsService(),
    private val invitations: OrganizationInvitationRepository = UnconfiguredOrganizationInvitationService(),
    private val history: TripHistoryRepository = UnconfiguredTripHistoryService(),
    private val work: WorkTripRepository = UnconfiguredWorkTripService(),
    private val plan: TripPlanRepository = UnconfiguredTripPlanService(),
    private val personalData: PersonalDataRepository = UnconfiguredPersonalDataRepository(),
    private val organization: OrganizationRepository = UnconfiguredOrganizationRepository(),
    private val tasks: DemoWorkTasks = DemoWorkTasks(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    fun create(entry: NavigationEntry): ViewModel = when (entry.destination) {
        AppDestination.SignIn -> SignInViewModel(SignIn(authentication))
        AppDestination.Registration -> RegistrationViewModel(Register(authentication))
        AppDestination.Route -> RouteViewModel(session)
        AppDestination.AddressSearch -> AddressSearchViewModel(session, SearchDemoAddresses(navigation), session.state.value.destination)
        AppDestination.RouteSettings -> RouteSettingsViewModel(session)
        AppDestination.NearbyStops -> NearbyStopsViewModel(FilterDemoStops(navigation))
        AppDestination.ParkingStop -> ParkingStopViewModel()
        AppDestination.ActiveNavigation -> ActiveNavigationViewModel()
        AppDestination.XrNavigation -> XrNavigationViewModel()
        AppDestination.TripSummary -> TripSummaryViewModel()
        AppDestination.Garage -> GarageViewModel(session)
        AppDestination.AddVehicle -> AddVehicleViewModel(vehicles)
        AppDestination.FordConfiguration -> FordConfigurationViewModel(session, vehicles)
        AppDestination.VehicleProfile -> VehicleProfileViewModel(session, vehicles, vehicleOperations)
        AppDestination.VehicleConfigurations -> VehicleConfigurationsViewModel(session, vehicles, vehicleOperations)
        AppDestination.Preferences -> PreferencesViewModel(preferences)
        AppDestination.Profile -> ProfileViewModel(session)
        AppDestination.Privacy -> PrivacyViewModel(ManagePersonalData(personalData))
        AppDestination.OrganizationSettings -> OrganizationSettingsViewModel(preferences, session, ContactOrganization(organization))
        AppDestination.DeviceDiagnostics -> DeviceDiagnosticsViewModel()
        AppDestination.DeleteAccount -> AccountDeletionViewModel(RequestAccountDeletion(deletion), ExportAccountData(deletion), dispatcher)
        AppDestination.OfflineMaps -> OfflineMapsViewModel(PerformOfflineMapsAction(offlineMaps), dispatcher)
        AppDestination.OrganizationInvitation -> OrganizationInvitationViewModel(
            AcceptOrganizationInvitation(invitations, session), "XR-2406", "andrii@ukr.net", dispatcher)
        AppDestination.TripHistory -> TripHistoryViewModel(PerformTripHistoryAction(history), dispatcher)
        AppDestination.WorkTasks -> WorkTasksViewModel(
            PerformWorkTripAction(work, session), OpenChosenWorkTask(session), session, tasks.current, tasks.offered, dispatcher)
        AppDestination.WorkTripDetails -> WorkTripDetailsViewModel(
            PerformWorkTripAction(work, session), session, tasks.find(entry.taskId), dispatcher)
        AppDestination.TripPlan -> TripPlanViewModel(AddTripPlanStop(plan, session), session, tasks.find(entry.taskId), dispatcher)
    }
}
