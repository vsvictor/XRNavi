package com.mobilespace.xrnavi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

class AndroidLocationProvider(private val context: Context) : LocationProvider {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val prefs = context.getSharedPreferences("xrnavi_location", Context.MODE_PRIVATE)
    private var requester: ((Array<String>) -> Unit)? = null
    private var activity: Activity? = null
    private val mutablePermission = MutableStateFlow(readPermission())
    private val mutableService = MutableStateFlow(readService())

    override val permission: StateFlow<LocationPermission> = mutablePermission
    override val serviceEnabled: StateFlow<Boolean> = mutableService

    fun attach(activity: Activity, requester: (Array<String>) -> Unit) {
        this.activity = activity
        this.requester = requester
        refresh()
    }

    fun detach(activity: Activity) {
        if (this.activity === activity) {
            this.activity = null
            requester = null
        }
    }

    /** Called with the system result; a refusal without a rationale means the OS will not ask again. */
    fun onPermissionResult() {
        prefs.edit().putBoolean(ASKED, true).apply()
        refresh()
    }

    override fun refresh() {
        mutablePermission.value = readPermission()
        mutableService.value = readService()
    }

    override fun requestPermission() {
        requester?.invoke(permissions)
    }

    override fun openAppSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    override fun openLocationSettings() {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @SuppressLint("MissingPermission")
    override fun fixes(): Flow<DeviceFix> = callbackFlow {
        val listener = LocationListener { location -> trySend(location.toFix()) }
        if (readPermission() != LocationPermission.Granted) {
            close()
            return@callbackFlow
        }
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        providers.mapNotNull { manager.getLastKnownLocation(it) }
            .maxByOrNull { it.time }?.let { trySend(it.toFix()) }
        providers.forEach { manager.requestLocationUpdates(it, 2_000L, 0f, listener, Looper.getMainLooper()) }
        awaitClose { manager.removeUpdates(listener) }
    }

    private fun Location.toFix() = DeviceFix(GeoPoint(latitude, longitude), time)

    private fun readService(): Boolean = runCatching {
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }.getOrDefault(false)

    private fun readPermission(): LocationPermission {
        val granted = permissions.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) return LocationPermission.Granted
        if (!prefs.getBoolean(ASKED, false)) return LocationPermission.NotDetermined
        val canAskAgain = activity?.let {
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, permissions[0])
        } ?: false
        return if (canAskAgain) LocationPermission.Denied else LocationPermission.PermanentlyDenied
    }

    private companion object {
        const val ASKED = "asked"
    }
}
