package com.mobilespace.xrnavi

import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined

internal fun iosTrackingAuthorization(status: Int): IosTrackingAuthorization = when (status) {
    kCLAuthorizationStatusNotDetermined -> IosTrackingAuthorization.RequestWhenInUse
    kCLAuthorizationStatusAuthorizedWhenInUse -> IosTrackingAuthorization.Foreground
    kCLAuthorizationStatusAuthorizedAlways -> IosTrackingAuthorization.Background
    else -> IosTrackingAuthorization.Denied
}
