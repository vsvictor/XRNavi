package com.mobilespace.xrnavi

import com.mobilespace.xrnavi.data.DemoNavigationRepository
import com.mobilespace.xrnavi.data.InMemorySessionRepository
import com.mobilespace.xrnavi.domain.*
import com.mobilespace.xrnavi.presentation.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigationArchitectureTest {
    @Test
    fun settingsDraftIsIsolatedUntilApplyAndSelectsExplicitVehicleContext() {
        val original = RoutingSettings(vehicleCategory = 0, avoidTolls = true)
        val session = InMemorySessionRepository(SessionState(routingSettings = original, organizationMember = true))
        val viewModel = RouteSettingsViewModel(session)
        viewModel.selectVehicle(1)
        viewModel.nextTrailer()
        viewModel.selectRoute(1)
        viewModel.toggleRestStop()
        assertEquals(original, session.state.value.routingSettings)
        viewModel.apply()
        assertEquals(viewModel.state.value.draft, session.state.value.routingSettings)
        assertEquals(VehicleId.Man, session.state.value.selectedVehicle)
        assertEquals(DrivingContext.Work, session.state.value.context)
        assertTrue(session.state.value.routingSettings.avoidTolls)
        viewModel.toggleAvoidTolls()
        assertTrue(session.state.value.routingSettings.avoidTolls)
        viewModel.beginEditing()
        assertEquals(session.state.value.routingSettings, viewModel.state.value.draft)
    }

    @Test
    fun selectingTaskCannotSilentlyEnterWork() {
        val session = InMemorySessionRepository(SessionState(organizationMember = true))
        assertFalse(SelectWorkTask(session)(WorkTask("DL-204", "Львів")))
        assertEquals(DrivingContext.Personal, session.state.value.context)
        assertNull(session.state.value.selectedTask)
        assertTrue(SelectDrivingContext(session)(DrivingContext.Work))
        assertTrue(SelectWorkTask(session)(WorkTask("DL-205", "Київ")))
        assertEquals("Київ", session.state.value.destination)
        SetRouteDestination(session)("Львів")
        assertNull(session.state.value.selectedTask)
        assertEquals(DrivingContext.Work, session.state.value.context)
    }

    @Test
    fun categorySelectionSynchronizesVehicleAndRequiresMembershipForCorporateAsset() {
        val session = InMemorySessionRepository()
        assertFalse(SelectRouteVehicle(session)(1))
        assertEquals(VehicleId.Mustang, session.state.value.selectedVehicle)
        session.update { it.copy(organizationMember = true) }
        assertTrue(SelectRouteVehicle(session)(1))
        assertEquals(VehicleId.Man, session.state.value.selectedVehicle)
        assertEquals(1, session.state.value.routingSettings.vehicleCategory)
        assertEquals(DrivingContext.Work, session.state.value.context)
        SelectRouteVehicle(session)(0)
        assertEquals(VehicleId.Mustang, session.state.value.selectedVehicle)
    }

    @Test
    fun demoSearchFiltersAndSelectionWritesDestination() {
        val search = SearchDemoAddresses(DemoNavigationRepository())
        assertEquals(emptyList(), search(""))
        assertEquals(listOf("lviv-market-1", "lviv-market-10"), search("Ринок Львів").map { it.id })
        assertEquals(emptyList(), search("неіснуюча адреса"))
        val session = InMemorySessionRepository()
        val viewModel = AddressSearchViewModel(session, search, "")
        viewModel.queryChanged("Хрещатик")
        assertEquals(1, viewModel.state.value.results.size)
        val destination = viewModel.state.value.results.single().destination
        viewModel.selectAddress(destination)
        assertEquals(destination, session.state.value.destination)
        assertEquals(destination, viewModel.state.value.selectedAddress)
    }

    @Test
    fun nearbyFiltersAffectFixturesInsteadOfOnlyChipStyle() {
        val filter = FilterDemoStops(DemoNavigationRepository())
        val viewModel = NearbyStopsViewModel(filter)
        assertEquals(1, viewModel.state.value.stops.size)
        viewModel.selectCategory(StopCategory.Fuel)
        assertTrue(viewModel.state.value.stops.isEmpty())
        viewModel.selectCategory(StopCategory.Rest)
        assertEquals("m06-parking", viewModel.state.value.stops.single().id)
        viewModel.toggleDistanceFilter()
        assertFalse(viewModel.state.value.distanceFilter)
    }

    @Test
    fun missingServicesOnlyProduceUnavailableNotices() {
        val parking = ParkingStopViewModel()
        parking.addStop()
        assertEquals(NavigationNotice.AddStopUnavailable, parking.state.value.notice)
        parking.call()
        assertEquals(NavigationNotice.PhoneUnavailable, parking.state.value.notice)
        val active = ActiveNavigationViewModel()
        active.report()
        assertEquals(NavigationNotice.ReportUnavailable, active.state.value.notice)
        active.recenter()
        assertEquals(NavigationNotice.LocationUnavailable, active.state.value.notice)
        active.toggleSound()
        assertFalse(active.state.value.soundEnabled)
        val summary = TripSummaryViewModel()
        summary.share()
        assertEquals(NavigationNotice.ShareUnavailable, summary.state.value.notice)
        assertEquals(RoutePreviewResult.MissingDestination, CheckRoutePreview()("", 0))
        assertEquals(RoutePreviewResult.TruckValidationUnavailable, CheckRoutePreview()("Львів", 1))
        assertEquals(RoutePreviewResult.DemoPreview, CheckRoutePreview()("Львів", 0))
    }
}
