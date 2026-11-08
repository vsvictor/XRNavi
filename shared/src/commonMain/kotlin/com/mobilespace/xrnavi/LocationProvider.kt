package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

enum class LocationPermission { NotDetermined, Granted, Denied, PermanentlyDenied }

data class DeviceFix(val point: GeoPoint, val timestampMillis: Long)

/** Platform geolocation. Implemented natively on Android and iOS and injected through [AppDependencies]. */
interface LocationProvider {
    val permission: StateFlow<LocationPermission>
    val serviceEnabled: StateFlow<Boolean>

    /** Re-reads permission and device service state, e.g. after returning from OS settings. */
    fun refresh()

    /** Shows the native permission prompt. Never repeats a prompt that the OS will not show. */
    fun requestPermission()
    fun openAppSettings()
    fun openLocationSettings()

    /** Emits fixes (the last known one first, with its original timestamp) while collected. */
    fun fixes(): Flow<DeviceFix>
}

/** Used when no native provider is injected (previews, tests): the state stays honestly unknown. */
class UnavailableLocationProvider : LocationProvider {
    override val permission: StateFlow<LocationPermission> = MutableStateFlow(LocationPermission.NotDetermined)
    override val serviceEnabled: StateFlow<Boolean> = MutableStateFlow(true)
    override fun refresh() = Unit
    override fun requestPermission() = Unit
    override fun openAppSettings() = Unit
    override fun openLocationSettings() = Unit
    override fun fixes(): Flow<DeviceFix> = emptyFlow()
}
