package com.mobilespace.xrnavi

import com.exkmptracker.bloc.TrackerState
import com.exkmptracker.model.LocationSource
import com.exkmptracker.model.TrackEstimate
import com.exkmptracker.ports.TrackerClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerLocationBlocTest {
    private val estimate = TrackEstimate(50.45, 30.52, 4.5, 12.0, 85.0, 100.0, LocationSource.gps)

    private fun tracking() = TrackerIntegrationState(
        authenticated = true,
        tracking = TrackerState(running = true, estimate = estimate),
    )

    private fun permissions() = FakeLocationProvider().also { it.permission.value = LocationPermission.Granted }

    private fun TestScope.clock() = object : TrackerClock {
        override fun wallTimeMs() = 1_000_000L + testScheduler.currentTime
        override fun monotonicSeconds() = 100.0 + testScheduler.currentTime / 1_000.0
    }

    @Test fun mapPuckRecenterAndCurrentOriginShareFixWithoutLocate() = runTest {
        val tracker = MutableStateFlow(tracking())
        val native = permissions()
        val locations = TrackerLocationBloc(backgroundScope, tracker, native, clock())
        val geo = GeoBloc(TrackingLocationProvider(native, locations), backgroundScope,
            { clock().wallTimeMs() })
        geo.dispatch(GeoEvent.Observe)
        runCurrent()
        val fix = assertNotNull(locations.state.value)
        assertSame(fix, geo.state.value.reliableFix)
        assertEquals(0, native.permissionRequests)
        assertEquals(4.5, fix.accuracyMeters)
        assertEquals(85.0, fix.bearingDegrees)
        assertEquals(12.0, fix.speedMps)
        val route = listOf(GeoPoint(51.0, 31.0), GeoPoint(52.0, 32.0))
        val overlay = MapOverlay(route = route, user = GeoPoint(0.0, 0.0)).withTrackerLocation(fix)
        assertSame(fix, overlay.location)
        assertEquals(fix.point, overlay.user)
        assertEquals(route, overlay.route)
        assertNull(overlay.withTrackerLocation(null).user)
        assertNull(overlay.withTrackerLocation(null).location)

        tracker.value = tracking().copy(tracking = TrackerState(running = true,
            estimate = estimate.copy(latitude = 50.46)))
        runCurrent()
        assertEquals(50.46, geo.state.value.reliableFix?.point?.latitude)
        tracker.value = TrackerIntegrationState()
        runCurrent()
        assertNull(locations.state.value)
        assertNull(geo.state.value.reliableFix)
    }

    @Test fun trackerOnlyNeverSubscribesToNativeFixesEvenWithoutLoginOrWhileRegistering() = runTest {
        val tracker = MutableStateFlow(TrackerIntegrationState())
        val native = permissions()
        var nativeSubscriptions = 0
        val platform = object : LocationProvider by native {
            override fun fixes(): Flow<DeviceFix?> {
                nativeSubscriptions++
                return native.fixFlow
            }
        }
        val locations = TrackerLocationBloc(backgroundScope, tracker, platform, clock())
        val geo = GeoBloc(TrackingLocationProvider(platform, locations), backgroundScope,
            { clock().wallTimeMs() })
        geo.dispatch(GeoEvent.Observe)
        runCurrent()
        native.fixFlow.tryEmit(DeviceFix(GeoPoint(0.0, 0.0), clock().wallTimeMs()))
        tracker.value = TrackerIntegrationState(authenticated = true, registering = true)
        runCurrent()
        assertNull(locations.state.value)
        assertNull(geo.state.value.reliableFix)
        tracker.value = tracking()
        runCurrent()
        assertNotNull(geo.state.value.reliableFix)
        tracker.value = TrackerIntegrationState()
        runCurrent()
        assertNull(geo.state.value.reliableFix)
        assertEquals(0, nativeSubscriptions)
    }

    @Test fun stopMissingEstimatePermissionRevocationAndServiceOffInvalidateFix() = runTest {
        val tracker = MutableStateFlow(tracking())
        val native = permissions()
        val locations = TrackerLocationBloc(backgroundScope, tracker, native, clock())
        runCurrent()
        assertNotNull(locations.state.value)
        tracker.value = tracking().copy(tracking = tracking().tracking.copy(running = false))
        runCurrent()
        assertNull(locations.state.value)
        tracker.value = tracking()
        runCurrent()
        native.permission.value = LocationPermission.PermanentlyDenied
        runCurrent()
        assertNull(locations.state.value)
        native.permission.value = LocationPermission.Granted
        runCurrent()
        assertNotNull(locations.state.value)
        native.serviceEnabled.value = false
        runCurrent()
        assertNull(locations.state.value)
        native.serviceEnabled.value = true
        runCurrent()
        assertNotNull(locations.state.value)
        tracker.value = tracking().copy(tracking = TrackerState(running = true))
        runCurrent()
        assertNull(locations.state.value)
    }

    @Test fun unchangedEstimateExpiresAtExactFreshnessBoundaryWithoutFurtherEvents() = runTest {
        val tracker = MutableStateFlow(tracking())
        val locations = TrackerLocationBloc(backgroundScope, tracker, permissions(), clock())
        runCurrent()
        val fix = assertNotNull(locations.state.value)
        advanceTimeBy(30_000)
        tracker.value = tracking().copy(problem = TrackerIntegrationProblem.Runtime)
        runCurrent()
        assertSame(fix, locations.state.value)
        advanceTimeBy(30_000)
        runCurrent()
        assertNotNull(locations.state.value)
        advanceTimeBy(1)
        runCurrent()
        assertNull(locations.state.value)
        tracker.value = tracking().copy(registering = true)
        runCurrent()
        assertNull(locations.state.value)
    }

    @Test fun oldFutureAndNonFiniteObservationTimesAreNotCurrentLocations() = runTest {
        val tracker = MutableStateFlow(tracking().copy(tracking = TrackerState(running = true,
            estimate = estimate.copy(monotonicSeconds = 39.999))))
        val locations = TrackerLocationBloc(backgroundScope, tracker, permissions(), clock())
        runCurrent()
        assertNull(locations.state.value)
        for (timestamp in listOf(100.001, Double.NaN, Double.POSITIVE_INFINITY)) {
            tracker.value = tracking().copy(tracking = TrackerState(running = true,
                estimate = estimate.copy(monotonicSeconds = timestamp)))
            runCurrent()
            assertNull(locations.state.value)
        }
        tracker.value = tracking().copy(tracking = TrackerState(running = true,
            estimate = estimate.copy(monotonicSeconds = 95.0)))
        runCurrent()
        assertEquals(995_000L, locations.state.value?.timestampMillis)
        advanceTimeBy(55_001)
        runCurrent()
        assertNull(locations.state.value)
    }

    @Test fun unavailableCourseAndSpeedAreNotFabricated() = runTest {
        val tracker = MutableStateFlow(tracking().copy(tracking = TrackerState(running = true,
            estimate = estimate.copy(bearingDegrees = -1.0, speedMps = -1.0))))
        val locations = TrackerLocationBloc(backgroundScope, tracker, permissions(), clock())
        runCurrent()
        val fix = assertNotNull(locations.state.value)
        assertNull(fix.bearingDegrees)
        assertNull(fix.speedMps)
    }

    @Test fun stationaryEstimateTimestampReflectsTrackerUpdateTime() = runTest {
        val tracker = MutableStateFlow(tracking().copy(tracking = TrackerState(running = true,
            estimate = estimate.copy(source = LocationSource.stationary))))
        val locations = TrackerLocationBloc(backgroundScope, tracker, permissions(), clock())
        runCurrent()
        assertEquals(1_000_000L, assertNotNull(locations.state.value).timestampMillis)
        advanceTimeBy(30_000)
        tracker.value = tracking().copy(tracking = TrackerState(running = true,
            estimate = estimate.copy(source = LocationSource.stationary, monotonicSeconds = 130.0)))
        runCurrent()
        assertEquals(1_030_000L, assertNotNull(locations.state.value).timestampMillis)
        advanceTimeBy(30_001)
        runCurrent()
        assertNotNull(locations.state.value)
    }

    @Test fun resumeRechecksMonotonicAgeEvenIfDispatcherWasSuspended() = runTest {
        var monotonicSeconds = 100.0
        val clock = object : TrackerClock {
            override fun wallTimeMs() = 1_000_000L
            override fun monotonicSeconds() = monotonicSeconds
        }
        val tracker = MutableStateFlow(tracking())
        val native = permissions()
        val locations = TrackerLocationBloc(backgroundScope, tracker, native, clock)
        val provider = TrackingLocationProvider(native, locations)
        runCurrent()
        val original = assertNotNull(locations.state.value)
        monotonicSeconds = 130.0
        provider.refresh()
        runCurrent()
        assertSame(original, locations.state.value)
        monotonicSeconds = 161.0
        provider.refresh()
        runCurrent()
        assertNull(locations.state.value)
    }
}
