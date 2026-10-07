package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
internal actual fun rememberDeviceSettingsLauncher(): DeviceSettingsLauncher = remember {
    DeviceSettingsLauncher { onResult ->
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (url == null) {
            onResult(DeviceSettingsResult.Unavailable)
        } else {
            UIApplication.sharedApplication.openURL(
                url,
                options = emptyMap<Any?, Any>(),
                completionHandler = { opened ->
                    onResult(
                        if (opened) DeviceSettingsResult.Opened
                        else DeviceSettingsResult.Unavailable,
                    )
                },
            )
        }
    }
}
