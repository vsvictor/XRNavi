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
