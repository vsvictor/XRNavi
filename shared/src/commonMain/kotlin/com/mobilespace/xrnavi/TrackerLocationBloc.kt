package com.mobilespace.xrnavi

import com.exkmptracker.ports.TrackerClock
import com.exkmptracker.model.TrackEstimate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.ceil

internal class TrackerLocationBloc(
    scope: CoroutineScope,
    tracker: StateFlow<TrackerIntegrationState>,
    permissions: LocationProvider,
    private val clock: TrackerClock = createTrackingClock(),
    private val staleAfterMillis: Long = GeoPolicy().staleAfterMillis,
) {
    private val mutableState = MutableStateFlow<DeviceFix?>(null)
    val state = mutableState.asStateFlow()
    private val refreshRevision = MutableStateFlow(0L)

    init {
        scope.launch {
            var observedEstimate: TrackEstimate? = null
            var observedFix: DeviceFix? = null
            combine(tracker, permissions.permission, permissions.serviceEnabled, refreshRevision) { tracking, permission, enabled, revision ->
                val estimate = tracking.tracking.estimate?.takeIf {
                    tracking.authenticated && tracking.tracking.running &&
                        permission == LocationPermission.Granted && enabled
                }
                estimate to revision
            }.distinctUntilChanged().collectLatest { (estimate, _) ->
                if (estimate == null) {
                    mutableState.value = null
                    return@collectLatest
                }
                val ageMillis = (clock.monotonicSeconds() - estimate.monotonicSeconds) * 1_000
                if (!ageMillis.isFinite() || ageMillis < 0 || ageMillis > staleAfterMillis) {
                    mutableState.value = null
                    return@collectLatest
                }
                if (observedEstimate != estimate) {
                    observedEstimate = estimate
                    observedFix = DeviceFix(
                        point = GeoPoint(estimate.latitude, estimate.longitude),
                        timestampMillis = clock.wallTimeMs() - ageMillis.toLong(),
                        accuracyMeters = estimate.accuracyMeters.takeIf { it.isFinite() && it >= 0.0 },
                        bearingDegrees = estimate.bearingDegrees.takeIf { it.isFinite() && it in 0.0..<360.0 },
                        speedMps = estimate.speedMps.takeIf { it.isFinite() && it >= 0.0 },
                    )
                }
                mutableState.value = observedFix
                // Expire even if the native runtime stops delivering events without stopping itself.
                delay(ceil(staleAfterMillis - ageMillis).toLong() + 1)
                mutableState.value = null
            }
        }
    }

    fun refresh() {
        refreshRevision.value++
    }
}
