package com.mobilespace.xrnavi

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.exkmptracker.android.TrackerForegroundService
import com.exkmptracker.android.AndroidTracker
import com.exkmptracker.bloc.TrackerState
import com.exkmptracker.model.TrackingSession
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Application-owned bridge: Activity is retained only while it is resumed. */
class AndroidAppTrackingRuntime(
    private val context: Context,
    private val scope: CoroutineScope,
) : AppTrackingRuntime {
    private val mutableState = MutableStateFlow(TrackerState())
    override val state = mutableState.asStateFlow()
    private val foreground = MutableStateFlow<Activity?>(null)
    private val permissionsChanged = MutableStateFlow(0)
    private var request: ((Array<String>) -> Unit)? = null
    private var observer: Job? = null
    private var connection: ServiceConnection? = null
    private var bound = false
    private var serviceBinder: TrackerForegroundService.TrackerBinder? = null
    private val requested = mutableSetOf<String>()

    fun attach(activity: Activity, requester: (Array<String>) -> Unit) {
        request = requester
        foreground.value = activity
        xrnaviCameraForeground(true)
        permissionsChanged.value++
    }

    fun detach(activity: Activity) {
        if (foreground.value === activity) {
            xrnaviCameraForeground(false)
            foreground.value = null
            request = null
        }
    }

    fun permissionsReturned() { permissionsChanged.value++ }

    override suspend fun start(session: TrackingSession) = withContext(Dispatchers.Main.immediate) {
        while (true) {
            val activity = foreground.filterNotNull().first { !it.isFinishing && !it.isDestroyed }
            if (!activity.hasWindowFocus()) { delay(100); continue }
            val missing = requiredPermissions().filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missing.isEmpty()) {
                if (xrnaviCooperativeVehicleClass() != null) requestCameraOnce()
                watchForEgoCamera()
                mutableState.value = TrackerState(identity = session.identity)
                AndroidTracker.startConfirmedShift(activity, session)
                observeService()
                return@withContext
            }
            mutableState.value = TrackerState(error = "Location, background location and notification permissions are required")
            val revision = permissionsChanged.value
            // Android requires foreground and background location to be requested separately.
            val group = when {
                Manifest.permission.ACCESS_FINE_LOCATION in missing ->
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                Build.VERSION.SDK_INT >= 33 && Manifest.permission.POST_NOTIFICATIONS in missing ->
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                else -> arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            if (group.any { it !in requested }) {
                requested.addAll(group)
                request?.invoke(group)
            }
            permissionsChanged.first { it != revision }
        }
    }

    private suspend fun observeService() {
        val connected = CompletableDeferred<TrackerForegroundService.TrackerBinder>()
        val binding = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? TrackerForegroundService.TrackerBinder
                if (binder == null) connected.completeExceptionally(IllegalStateException("Invalid tracker service"))
                else connected.complete(binder)
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                mutableState.value = state.value.copy(running = false, error = "Tracker service disconnected")
            }
        }
        connection = binding
        val binder = withTimeout(10_000) {
            while (!bound) {
                bound = context.bindService(Intent(context, TrackerForegroundService::class.java), binding, 0)
                if (!bound) delay(100)
            }
            connected.await()
        }
        serviceBinder = binder
        withTimeoutOrNull(10_000) {
            binder.state.first { it.running || it.error != null }
        }
        val vision = (context.applicationContext as? XrnaviAndroidApplication)?.edgeVisionAttached == true
        if (binder.state.value.running && vision) {
            binder.section(2, true)
        }
        observer = scope.launch { binder.state.collect { mutableState.value = it } }
    }

    private var cameraAsked = false
    private var cameraWatch: Job? = null

    private fun watchForEgoCamera() {
        if (cameraWatch?.isActive == true) return
        cameraWatch = scope.launch {
            while (isActive) {
                if (xrnaviCooperativeVehicleClass() != null) {
                    requestCameraOnce()
                    return@launch
                }
                delay(500)
            }
        }
    }

    private suspend fun requestCameraOnce() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) return
        if (cameraAsked) return
        cameraAsked = true
        val revision = permissionsChanged.value
        request?.invoke(arrayOf(Manifest.permission.CAMERA))
        withTimeoutOrNull(60_000) { permissionsChanged.first { it != revision } }
    }

    override suspend fun stop() = withContext(Dispatchers.Main.immediate) {
        observer?.cancelAndJoin()
        observer = null
        cameraWatch?.cancel()
        cameraWatch = null
        val binder = serviceBinder
        if (binder != null) {
            binder.stopTracking()
            check(binder.state.value.error == null) { "Unable to clear the stopped tracker session" }
        } else AndroidTracker.stopFromHost(context)
        serviceBinder = null
        if (bound) connection?.let { context.unbindService(it) }
        bound = false
        connection = null
        mutableState.value = TrackerState()
    }

    private fun requiredPermissions() = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 29) add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }
}
