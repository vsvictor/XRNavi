@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mobilespace.xrnavi

import com.exkmptracker.bloc.TrackerState
import com.exkmptracker.ios.IosTrackerRuntime
import com.exkmptracker.model.TrackingSession
import com.exkmptracker.transport.RpcTrackingTransport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import platform.CoreLocation.*
import platform.darwin.NSObject

internal class IosAppTrackingRuntime(transport: RpcTrackingTransport) : AppTrackingRuntime {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val runtime = IosTrackerRuntime(transport)
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
                            runtime.startConfirmedSession(session,
                                backgroundLocationConfigured = permission == IosTrackingAuthorization.Background)
                        }
                        IosTrackingAuthorization.Denied -> {
                            runtime.stop()
                            mutableState.value = TrackerState(error = "Location permission was denied or restricted")
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = TrackerState(error = "Unable to start location tracking")
            }
        }
        observer = scope.launch {
            runtime.state.collect {
                if (it.running || it.error != null) {
                    mutableState.value = it
                }
            }
        }
    }

    override suspend fun stop() = withContext(Dispatchers.Main.immediate) {
        permissionObserver?.cancelAndJoin()
        permissionObserver = null
        observer?.cancelAndJoin()
        observer = null
        runtime.stop()
        mutableState.value = TrackerState()
    }
}
