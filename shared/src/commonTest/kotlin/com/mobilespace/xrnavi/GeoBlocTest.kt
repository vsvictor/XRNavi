package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class GeoBlocTest {
    private val point = GeoPoint(50.45, 30.52)

    private fun TestScope.bloc(provider: FakeLocationProvider) =
        GeoBloc(provider, backgroundScope, { testScheduler.currentTime }, GeoPolicy(60_000, 30_000, 1_000))

    @Test
    fun locateAsksForPermissionOnlyWhenUndetermined() = runTest {
        val provider = FakeLocationProvider()
        val geo = bloc(provider)
        geo.dispatch(GeoEvent.Locate)
        assertEquals(1, provider.permissionRequests)
        provider.permission.value = LocationPermission.Granted
        runCurrent()
        geo.dispatch(GeoEvent.Locate)
        assertEquals(1, provider.permissionRequests)
        geo.dispatch(GeoEvent.Stop)
    }

    @Test
    fun deniedStatesNeverReportAFix() = runTest {
        val provider = FakeLocationProvider()
        val geo = bloc(provider)
        provider.permission.value = LocationPermission.Denied
        runCurrent()
        assertEquals(GeoPhase.Denied, geo.state.value.phase)
        provider.permission.value = LocationPermission.PermanentlyDenied
        runCurrent()
        assertEquals(GeoPhase.PermanentlyDenied, geo.state.value.phase)
        geo.dispatch(GeoEvent.OpenSettings)
        assertEquals(1, provider.appSettingsOpened)
        assertNull(geo.state.value.reliableFix)
    }

    @Test
    fun serviceOffOpensDeviceLocationSettings() = runTest {
        val provider = FakeLocationProvider()
        provider.permission.value = LocationPermission.Granted
        provider.serviceEnabled.value = false
        val geo = bloc(provider)
        runCurrent()
        assertEquals(GeoPhase.ServiceOff, geo.state.value.phase)
        geo.dispatch(GeoEvent.OpenSettings)
        assertEquals(1, provider.locationSettingsOpened)
    }

    @Test
    fun acquiringTimesOutThenFixBecomesReliableThenStale() = runTest {
        val provider = FakeLocationProvider()
        provider.permission.value = LocationPermission.Granted
        val geo = bloc(provider)
        geo.dispatch(GeoEvent.Locate)
        runCurrent()
        assertEquals(GeoPhase.Acquiring, geo.state.value.phase)
        advanceTimeBy(32_000)
        assertEquals(GeoPhase.TimedOut, geo.state.value.phase)
        provider.fixFlow.tryEmit(DeviceFix(point, testScheduler.currentTime))
        runCurrent()
        assertEquals(GeoPhase.Reliable, geo.state.value.phase)
        assertNotNull(geo.state.value.reliableFix)
        advanceTimeBy(62_000)
        assertEquals(GeoPhase.Stale, geo.state.value.phase)
        assertNull(geo.state.value.reliableFix)
        geo.dispatch(GeoEvent.Stop)
    }

    @Test
    fun oldLastKnownFixIsStaleNotReliable() = runTest {
        val provider = FakeLocationProvider()
        provider.permission.value = LocationPermission.Granted
        advanceTimeBy(500_000)
        val geo = bloc(provider)
        geo.dispatch(GeoEvent.Locate)
        runCurrent()
        provider.fixFlow.tryEmit(DeviceFix(point, 1_000))
        runCurrent()
        assertEquals(GeoPhase.Stale, geo.state.value.phase)
        geo.dispatch(GeoEvent.Stop)
    }
}
