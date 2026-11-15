package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GarageBlocTest {
    @Test
    fun loadsVehiclesAndActive() = runTest {
        val bloc = GarageBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        assertEquals(2, bloc.state.value.vehicles.size)
        assertEquals("a", bloc.state.value.active?.id)
        assertEquals(listOf("passenger", "truck", "special"), bloc.state.value.categories)
        assertEquals(2, bloc.state.value.visibleVehicles.size)
    }

    @Test
    fun categoryFiltersAreAlwaysAvailableAndMatchServerCategoryAliases() = runTest {
        val service = FakeGarageService().apply {
            vehicles = listOf(
                GarageVehicle("a", "Car", "light_vehicle", VerificationStatus.Verified),
                GarageVehicle("s", "Special vehicle", "special_equipment", VerificationStatus.None),
            )
        }
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()

        assertEquals(listOf("passenger", "truck", "special"), bloc.state.value.categories)
        bloc.dispatch(GarageEvent.FilterChanged("special"))
        assertEquals(listOf("s"), bloc.state.value.visibleVehicles.map { it.id })
        bloc.dispatch(GarageEvent.FilterChanged("passenger"))
        assertEquals(listOf("a"), bloc.state.value.visibleVehicles.map { it.id })
    }

    @Test
    fun categoryFilterOnlyShowsMatchingVehiclesAndCanBeCleared() = runTest {
        val bloc = GarageBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        bloc.dispatch(GarageEvent.FilterChanged("truck"))
        assertEquals(listOf("t"), bloc.state.value.visibleVehicles.map { it.id })
        bloc.dispatch(GarageEvent.FilterChanged(null))
        assertEquals(2, bloc.state.value.visibleVehicles.size)
    }

    @Test
    fun categoryFilterIgnoresUnknownCategory() = runTest {
        val bloc = GarageBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        bloc.dispatch(GarageEvent.FilterChanged("unknown"))
        assertNull(bloc.state.value.selectedCategory)
        assertEquals(2, bloc.state.value.visibleVehicles.size)
    }

    @Test
    fun failedLoadDoesNotClaimGarageIsEmpty() = runTest {
        val service = FakeGarageService().apply { fail = true }
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        assertFalse(bloc.state.value.loaded)
        assertTrue(bloc.state.value.vehicles.isEmpty())
        assertTrue(bloc.state.value.loadFailed)
        assertTrue(bloc.state.value.error.isNotEmpty())
    }

    @Test
    fun failedSelectKeepsPreviousVehicle() = runTest {
        val service = FakeGarageService()
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        service.fail = true
        bloc.dispatch(GarageEvent.Select("t"))
        runCurrent()
        assertEquals("a", bloc.state.value.activeVehicleId)
        service.fail = false
        bloc.dispatch(GarageEvent.Select("t"))
        runCurrent()
        assertEquals("t", bloc.state.value.activeVehicleId)
    }

    @Test
    fun unverifiedTruckNeedsVerification() {
        assertTrue(FakeGarageService().vehicles[1].needsVerification)
        assertFalse(FakeGarageService().vehicles[0].needsVerification)
    }

    @Test
    fun withoutSessionReportsError() = runTest {
        val bloc = GarageBloc(FakeGarageService(), { null }, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        assertTrue(bloc.state.value.error.isNotEmpty())
    }
}
