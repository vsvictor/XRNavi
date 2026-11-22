package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeLocationProvider : LocationProvider {
    override val permission = MutableStateFlow(LocationPermission.NotDetermined)
    override val serviceEnabled = MutableStateFlow(true)
    val fixFlow = MutableSharedFlow<DeviceFix>(extraBufferCapacity = 8)
    var permissionRequests = 0
    var appSettingsOpened = 0
    var locationSettingsOpened = 0

    override fun refresh() = Unit
    override fun requestPermission() { permissionRequests++ }
    override fun openAppSettings() { appSettingsOpened++ }
    override fun openLocationSettings() { locationSettingsOpened++ }
    override fun fixes(): Flow<DeviceFix> = fixFlow
}
