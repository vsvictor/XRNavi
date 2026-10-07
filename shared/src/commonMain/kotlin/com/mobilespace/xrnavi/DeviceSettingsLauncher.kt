package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

internal enum class DeviceSettingsResult {
    Opened,
    Unavailable,
}

internal fun interface DeviceSettingsLauncher {
    fun open(onResult: (DeviceSettingsResult) -> Unit)
}

@Composable
internal expect fun rememberDeviceSettingsLauncher(): DeviceSettingsLauncher
