package com.mobilespace.xrnavi

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileGarageRefreshTest {
    @Test
    fun profileOpeningReusesCachedActiveVehicleAndSelectionUpdatesIt() = runTest {
        val service = FakeGarageService()
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()
        assertEquals("a", bloc.state.value.active?.id)

        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()
        assertEquals(1, service.vehicleListCalls)
        assertEquals("a", bloc.state.value.active?.id)

        bloc.dispatch(GarageEvent.Select("t"))
        runCurrent()
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()
        assertEquals(1, service.vehicleListCalls)
        assertEquals("t", bloc.state.value.active?.id)
        assertEquals("t", service.active)
        assertEquals(1, service.setActiveCalls)
    }

    @Test
    fun profileOpeningDuringLoadReusesInFlightRequest() = runTest {
        val service = FakeGarageService()
        val first = CompletableDeferred<GarageSnapshot>()
        service.vehicleListResponse = first
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        bloc.dispatch(GarageEvent.ProfileOpened)
        first.complete(GarageSnapshot(service.vehicles, "a"))
        runCurrent()

        assertEquals(1, service.vehicleListCalls)
        assertEquals("a", bloc.state.value.active?.id)
        assertFalse(bloc.state.value.loading)
    }

    @Test
    fun profileOpeningRetriesPreviousFailure() = runTest {
        val service = FakeGarageService().apply { fail = true }
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        assertTrue(bloc.state.value.loadFailed)

        service.fail = false
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()
        assertEquals(2, service.vehicleListCalls)
        assertEquals("a", bloc.state.value.active?.id)
    }

    @Test
    fun profileOpeningAfterClearLoadsNewSessionData() = runTest {
        val service = FakeGarageService()
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()
        bloc.dispatch(GarageEvent.Clear)
        service.active = "t"
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()

        assertEquals(2, service.vehicleListCalls)
        assertEquals("t", bloc.state.value.active?.id)
    }

    @Test
    fun failedSelectionKeepsCachedVehicleAndDoesNotReload() = runTest {
        val service = FakeGarageService()
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()
        service.fail = true
        bloc.dispatch(GarageEvent.Select("t"))
        runCurrent()
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()

        assertEquals(1, service.vehicleListCalls)
        assertEquals("a", bloc.state.value.active?.id)
        assertTrue(bloc.state.value.error.isNotEmpty())
    }

    @Test
    fun explicitReloadDuringLoadIsNotDiscarded() = runTest {
        val service = FakeGarageService()
        val first = CompletableDeferred<GarageSnapshot>()
        service.vehicleListResponse = first
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()
        bloc.dispatch(GarageEvent.Load)
        service.active = "t"
        service.vehicleListResponse = null
        first.complete(GarageSnapshot(service.vehicles, "a"))
        runCurrent()

        assertEquals(2, service.vehicleListCalls)
        assertEquals("t", bloc.state.value.active?.id)
    }

    @Test
    fun deletingActiveVehicleUpdatesCacheWithoutReloadingProfile() = runTest {
        val service = FakeGarageService()
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()
        bloc.dispatch(GarageEvent.RequestDelete("a"))
        bloc.dispatch(GarageEvent.ConfirmDelete)
        runCurrent()
        bloc.dispatch(GarageEvent.ProfileOpened)
        runCurrent()

        assertEquals(1, service.vehicleListCalls)
        assertEquals("t", bloc.state.value.active?.id)
    }
}
