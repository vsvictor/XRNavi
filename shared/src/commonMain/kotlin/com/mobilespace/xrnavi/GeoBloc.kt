package com.mobilespace.xrnavi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal enum class GeoPhase { Unknown, Denied, PermanentlyDenied, ServiceOff, Acquiring, TimedOut, Reliable, Stale }

/** Timing policy is provisional: final values are owned by native QA/backend and must not be treated as product claims. */
internal data class GeoPolicy(
    val staleAfterMillis: Long = 60_000,
    val acquireTimeoutMillis: Long = 30_000,
    val tickMillis: Long = 5_000,
)

internal data class GeoState(
    val permission: LocationPermission = LocationPermission.NotDetermined,
    val serviceEnabled: Boolean = true,
    val active: Boolean = false,
    val fix: DeviceFix? = null,
    val acquiringSince: Long = 0,
    val now: Long = 0,
    val staleAfterMillis: Long = GeoPolicy().staleAfterMillis,
    val acquireTimeoutMillis: Long = GeoPolicy().acquireTimeoutMillis,
) {
    val phase: GeoPhase
        get() = when {
            permission == LocationPermission.Denied -> GeoPhase.Denied
            permission == LocationPermission.PermanentlyDenied -> GeoPhase.PermanentlyDenied
            permission == LocationPermission.NotDetermined -> GeoPhase.Unknown
            !serviceEnabled -> GeoPhase.ServiceOff
            fix != null -> if (now - fix.timestampMillis <= staleAfterMillis) GeoPhase.Reliable else GeoPhase.Stale
            !active -> GeoPhase.Unknown
            now - acquiringSince > acquireTimeoutMillis -> GeoPhase.TimedOut
            else -> GeoPhase.Acquiring
        }

    /** Only a fresh fix may be used as a "current place" origin. */
    val reliableFix: DeviceFix? get() = if (phase == GeoPhase.Reliable) fix else null
}

internal sealed interface GeoEvent {
    /** The user asked for the current place (locate button or "my location" origin). */
    data object Locate : GeoEvent
    data object Refresh : GeoEvent
    data object OpenSettings : GeoEvent
    data object Stop : GeoEvent
}

internal class GeoBloc(
    private val provider: LocationProvider,
    private val scope: CoroutineScope,
    private val clock: () -> Long,
    private val policy: GeoPolicy = GeoPolicy(),
) {
    private val mutableState = MutableStateFlow(
        GeoState(
            permission = provider.permission.value,
            serviceEnabled = provider.serviceEnabled.value,
            now = clock(),
            staleAfterMillis = policy.staleAfterMillis,
            acquireTimeoutMillis = policy.acquireTimeoutMillis,
        ),
    )
    val state = mutableState.asStateFlow()
    private var fixJob: Job? = null
    private var tickJob: Job? = null

    init {
        scope.launch {
            combine(provider.permission, provider.serviceEnabled) { permission, enabled -> permission to enabled }
                .collect { (permission, enabled) ->
                    mutableState.value = mutableState.value.copy(permission = permission, serviceEnabled = enabled)
                    reconcile()
                }
        }
    }

    fun dispatch(event: GeoEvent) {
        when (event) {
            GeoEvent.Locate -> {
                val current = mutableState.value
                if (current.permission == LocationPermission.NotDetermined) provider.requestPermission()
                mutableState.value = current.copy(active = true, acquiringSince = clock(), now = clock())
                reconcile()
            }
            GeoEvent.Refresh -> provider.refresh()
            GeoEvent.OpenSettings -> when {
                !mutableState.value.serviceEnabled -> provider.openLocationSettings()
                else -> provider.openAppSettings()
            }
            GeoEvent.Stop -> {
                mutableState.value = mutableState.value.copy(active = false)
                reconcile()
            }
        }
    }

    private fun reconcile() {
        val current = mutableState.value
        val collecting = current.active &&
            current.permission == LocationPermission.Granted && current.serviceEnabled
        if (!collecting) {
            fixJob?.cancel(); fixJob = null
            tickJob?.cancel(); tickJob = null
            return
        }
        if (fixJob == null) fixJob = scope.launch {
            provider.fixes().collect { fix ->
                val latest = mutableState.value
                if (latest.fix == null || fix.timestampMillis >= latest.fix.timestampMillis) {
                    mutableState.value = latest.copy(fix = fix, now = clock())
                }
            }
        }
        if (tickJob == null) tickJob = scope.launch {
            while (true) {
                delay(policy.tickMillis)
                mutableState.value = mutableState.value.copy(now = clock())
            }
        }
    }
}
