package com.mobilespace.xrnavi

import com.mobilespace.xrnavi.data.*
import com.mobilespace.xrnavi.domain.*
import com.mobilespace.xrnavi.presentation.navigation.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class AppArchitectureTest {
    private fun AppNavigator.open(destination: AppDestination, task: String? = null) =
        open(destination, DrivingContext.Work, true, task)

    @Test
    fun taskDetailBackReturnsThroughCallerWithoutSelfLoop() {
        val navigator = AppNavigator()
        navigator.authenticated()
        navigator.selectTab(AppDestination.Profile)
        navigator.open(AppDestination.WorkTasks)
        navigator.open(AppDestination.WorkTripDetails, "DL-204")
        navigator.open(AppDestination.TripPlan, "DL-204")
        assertEquals("DL-204", navigator.state.current.taskId)
        assertTrue(navigator.back())
        assertEquals(AppDestination.WorkTripDetails, navigator.state.current.destination)
        navigator.back()
        assertEquals(AppDestination.WorkTasks, navigator.state.current.destination)
        navigator.back()
        assertEquals(AppDestination.Profile, navigator.state.current.destination)
        navigator.back()
        assertEquals(AppDestination.Route, navigator.state.current.destination)
        assertFalse(navigator.back())
    }

    @Test
    fun detailReturnsToActualCallerAndSettingsKeepNearbyCaller() {
        val navigator = AppNavigator()
        navigator.authenticated()
        navigator.open(AppDestination.ActiveNavigation)
        navigator.open(AppDestination.NearbyStops)
        navigator.open(AppDestination.ParkingStop)
        navigator.back()
        assertEquals(AppDestination.NearbyStops, navigator.state.current.destination)
        navigator.open(AppDestination.RouteSettings)
        navigator.back()
        assertEquals(AppDestination.NearbyStops, navigator.state.current.destination)
        navigator.back()
        assertEquals(AppDestination.ActiveNavigation, navigator.state.current.destination)
    }

    @Test
    fun organizationAndInvitationBackRestoreTheirCallers() {
        val navigator = AppNavigator()
        navigator.open(AppDestination.Registration)
        navigator.open(AppDestination.OrganizationInvitation)
        navigator.back()
        assertEquals(AppDestination.Registration, navigator.state.current.destination)
        navigator.authenticated()
        navigator.selectTab(AppDestination.Profile)
        navigator.open(AppDestination.OrganizationSettings)
        navigator.back()
        assertEquals(AppDestination.Profile, navigator.state.current.destination)
        navigator.open(AppDestination.OrganizationSettings, DrivingContext.Personal, false)
        assertEquals(AppDestination.OrganizationInvitation, navigator.state.current.destination)
        navigator.back()
        assertEquals(AppDestination.Profile, navigator.state.current.destination)
    }

    @Test
    fun privateTripsCannotOpenWorkTasksOrDetails() {
        val navigator = AppNavigator()
        navigator.authenticated()
        assertFalse(navigator.open(AppDestination.WorkTasks, DrivingContext.Personal, true))
        assertFalse(navigator.open(AppDestination.WorkTripDetails, DrivingContext.Work, false, "DL-204"))
        assertEquals(AppDestination.Route, navigator.state.current.destination)
    }

    @Test
    fun activeProfileTabDoesNotPopAndRootTabsDoNotBuildBackLoops() {
        val navigator = AppNavigator()
        navigator.authenticated()
        navigator.selectTab(AppDestination.Profile)
        val before = navigator.state
        navigator.selectTab(AppDestination.Profile)
        assertEquals(before, navigator.state)
        navigator.selectTab(AppDestination.Garage)
        navigator.selectTab(AppDestination.Preferences)
        assertEquals(2, navigator.state.entries.size)
        navigator.back()
        assertEquals(AppDestination.Route, navigator.state.current.destination)
    }

    @Test
    fun stoppedPreviewCannotResumeThroughBackAndSummaryOpensHistory() {
        val navigator = AppNavigator()
        navigator.authenticated()
        navigator.open(AppDestination.ActiveNavigation)
        navigator.open(AppDestination.XrNavigation)
        navigator.finishPreview()
        assertEquals(listOf(AppDestination.Route, AppDestination.TripSummary),
            navigator.state.entries.map { it.destination })
        navigator.open(AppDestination.TripHistory)
        assertEquals(AppDestination.TripHistory, navigator.state.current.destination)
        navigator.back()
        assertEquals(AppDestination.TripSummary, navigator.state.current.destination)
        navigator.back()
        assertEquals(AppDestination.Route, navigator.state.current.destination)
    }

    @Test
    fun fordValidationBackReturnsToSelectedFordNotAddVehicle() {
        val navigator = AppNavigator()
        navigator.authenticated()
        navigator.selectTab(AppDestination.Garage)
        navigator.open(AppDestination.AddVehicle)
        navigator.open(AppDestination.FordConfiguration)
        navigator.open(AppDestination.VehicleProfile)
        navigator.open(AppDestination.VehicleConfigurations)
        navigator.back()
        assertEquals(AppDestination.VehicleProfile, navigator.state.current.destination)
        navigator.back()
        assertEquals(AppDestination.FordConfiguration, navigator.state.current.destination)
    }

    @Test
    fun addressSearchBackDoesNotForgetCaller() {
        val navigator = AppNavigator()
        navigator.authenticated()
        navigator.open(AppDestination.TripPlan)
        navigator.open(AppDestination.AddressSearch)
        navigator.back()
        assertEquals(AppDestination.TripPlan, navigator.state.current.destination)
    }

    @Test
    fun credentialValidationCoversEmptyMalformedAndShortFields() {
        val validate = ValidateCredentials()
        assertEquals(ValidationError.Required, validate(Credentials("", "")).email)
        assertEquals(ValidationError.Required, validate(Credentials("a@b.ua", "")).password)
        assertEquals(ValidationError.InvalidEmail, validate(Credentials("a@@b.ua", "password")).email)
        assertEquals(ValidationError.ShortPassword, validate(Credentials("a@b.ua", "short")).password)
        assertTrue(validate(Credentials(" a@b.ua ", "password")).valid)
    }

    @Test
    fun registrationRequiresFullNameAndExplicitConsent() {
        val request = Registration("Андрій", Credentials("a@b.ua", "password"), AccountType.Personal, false)
        val errors = ValidateRegistration()(request)
        assertEquals(ValidationError.FullNameRequired, errors.fullName)
        assertEquals(ValidationError.ConsentRequired, errors.consent)
        assertTrue(ValidateRegistration()(request.copy(fullName = "Андрій Коваль", consent = true)).valid)
    }

    @Test
    fun invalidAuthNeverCallsRepositoryAndValidAuthIsNotBypassed() = runBlocking<Unit> {
        var calls = 0
        var normalized: Credentials? = null
        val repository = object : AuthenticationRepository {
            override suspend fun signIn(credentials: Credentials): AuthenticationResult {
                calls++
                normalized = credentials
                return AuthenticationResult.NotConfigured
            }
            override suspend fun register(registration: Registration) = AuthenticationResult.NotConfigured
        }
        assertIs<AuthenticationResult.Invalid>(SignIn(repository)(Credentials("invalid", "short")))
        assertEquals(0, calls)
        assertEquals(AuthenticationResult.NotConfigured, SignIn(repository)(Credentials(" a@b.ua ", "password")))
        assertEquals("a@b.ua", normalized?.email)
        assertEquals(1, calls)
        assertEquals(AuthenticationResult.NotConfigured, UnconfiguredAuthenticationRepository().signIn(Credentials("a@b.ua", "password")))
    }

    @Test
    fun preferencesAreSharedAcrossScreensWithoutClaimingPersistenceToBackend() {
        val preferences = InMemoryPreferencesRepository()
        preferences.update { it.copy(voiceEnabled = false, xrEnabled = true) }
        assertFalse(preferences.state.value.voiceEnabled)
        assertTrue(preferences.state.value.xrEnabled)
        preferences.update { it.copy(dispatchNotifications = false) }
        assertFalse(preferences.state.value.dispatchNotifications)
        assertTrue(preferences.state.value.xrEnabled)
    }

    @Test
    fun nonmemberCannotBecomeMemberBySelectingWorkContext() {
        val session = InMemorySessionRepository()
        assertFalse(SelectDrivingContext(session)(DrivingContext.Work))
        assertFalse(session.state.value.organizationMember)
        assertEquals(DrivingContext.Personal, session.state.value.context)
        session.update { it.copy(organizationMember = true) }
        assertTrue(SelectDrivingContext(session)(DrivingContext.Work))
        assertEquals(VehicleId.Man, session.state.value.selectedVehicle)
        assertEquals(1, session.state.value.routingSettings.vehicleCategory)
        assertTrue(SelectDrivingContext(session)(DrivingContext.Personal))
        assertTrue(session.state.value.organizationMember)
        assertEquals(0, session.state.value.routingSettings.vehicleCategory)
    }

    @Test
    fun settingsApplicationCannotSelectUnauthorizedCorporateVehicle() {
        val session = InMemorySessionRepository(SessionState(destination = "private"))
        val before = session.state.value
        assertFalse(ApplyRoutingSettings(session)(RoutingSettings(vehicleCategory = 1)))
        assertEquals(before, session.state.value)
    }
}
