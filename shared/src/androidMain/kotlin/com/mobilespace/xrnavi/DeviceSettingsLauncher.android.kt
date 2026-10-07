package com.mobilespace.xrnavi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberDeviceSettingsLauncher(): DeviceSettingsLauncher {
    val context = LocalContext.current
    return remember(context) {
        DeviceSettingsLauncher { onResult ->
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
                onResult(DeviceSettingsResult.Opened)
            } catch (_: ActivityNotFoundException) {
                onResult(DeviceSettingsResult.Unavailable)
            }
        }
    }
}
