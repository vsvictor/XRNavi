@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mobilespace.xrnavi

import com.exkmptracker.bloc.TrackerState
import com.exkmptracker.ios.IosTrackerRuntime
import com.exkmptracker.model.TrackingSession
import com.exkmptracker.transport.RpcTrackingTransport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.CoreLocation.*
import platform.darwin.NSObject

internal class IosAppTrackingRuntime(private val transport: RpcTrackingTransport) : AppTrackingRuntime {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var runtime: IosTrackerRuntime? = null
    private var reconcileJob: Job? = null
    private var tracking = false
    private val visionLock = Mutex()

    private fun tracker(): IosTrackerRuntime =
        runtime ?: IosTrackerRuntime(transport) { xrnaviCooperativeVehicleClass() }.also {
            runtime = it
        }

    private fun ensureReconcile(active: IosTrackerRuntime, permission: IosTrackingAuthorization) {
        if (!iosCameraReconcileEnabled(permission, tracking)) return
        if (reconcileJob?.isActive == true) return
        reconcileJob = scope.launch {
            while (isActive) {
                try {
                    reconcile(active)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    // Section 1 keeps running when the camera cannot be rebuilt.
                }
                delay(1_000)
            }
        }
    }

    private suspend fun cancelReconcile() {
        reconcileJob?.cancelAndJoin()
        reconcileJob = null
    }

    private suspend fun reconcile(active: IosTrackerRuntime) {
        visionLock.withLock {
            val ego = xrnaviCooperativeVehicleClass()
            val installed = active.visionClass()
            if (ego == installed) return@withLock
            val vision = if (ego != null && iosCameraAuthorized()) iosCooperativeVision(ego) else null
            if (ego != null && vision == null) {
                if (installed != null) active.replaceVision(null, null)
                return@withLock
            }
            val built = if (vision != null) ego else null
            if (built == installed) return@withLock
            active.replaceVision(vision, built)
        }
    }
    private val manager = CLLocationManager()
    private val authorization = MutableStateFlow(manager.authorizationStatus)
    private val mutableState = MutableStateFlow(TrackerState())
    override val state = mutableState.asStateFlow()
    private var observer: Job? = null
    private var permissionObserver: Job? = null
    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            authorization.value = manager.authorizationStatus
        }
    }

    init { manager.delegate = delegate }

    override suspend fun start(session: TrackingSession) = withContext(Dispatchers.Main.immediate) {
        tracking = true
        val active = tracker()
        permissionObserver = scope.launch {
            try {
                authorization.collectLatest { status ->
                    val permission = iosTrackingAuthorization(status)
                    when (permission) {
                        IosTrackingAuthorization.RequestWhenInUse -> {
                            mutableState.value = TrackerState(error = "Location permission is required")
                            manager.requestWhenInUseAuthorization()
                        }
                        IosTrackingAuthorization.Foreground, IosTrackingAuthorization.Background -> {
                            active.startConfirmedSession(session,
                                backgroundLocationConfigured = permission == IosTrackingAuthorization.Background)
                            ensureReconcile(active, permission)
                        }
                        IosTrackingAuthorization.Denied -> {
                            cancelReconcile()
                            active.stop()
                            mutableState.value = TrackerState(error = "Location permission was denied or restricted")
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                cancelReconcile()
                mutableState.value = TrackerState(error = "Unable to start location tracking")
            }
        }
        observer = scope.launch {
            active.state.collect {
                if (it.running || it.error != null) {
                    mutableState.value = it
                }
            }
        }
    }

    override suspend fun stop() = withContext(Dispatchers.Main.immediate) {
        tracking = false
        cancelReconcile()
        permissionObserver?.cancelAndJoin()
        permissionObserver = null
        observer?.cancelAndJoin()
        observer = null
        runtime?.stop()
        mutableState.value = TrackerState()
    }
}
