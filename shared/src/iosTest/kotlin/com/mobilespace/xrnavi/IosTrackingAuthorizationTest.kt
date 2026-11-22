package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted

class IosTrackingAuthorizationTest {
    @Test
    fun requestsOnlyWhenInUseAndUsesGrantedAuthorization() {
        assertEquals(IosTrackingAuthorization.RequestWhenInUse, iosTrackingAuthorization(kCLAuthorizationStatusNotDetermined))
        assertEquals(IosTrackingAuthorization.Foreground, iosTrackingAuthorization(kCLAuthorizationStatusAuthorizedWhenInUse))
        assertEquals(IosTrackingAuthorization.Background, iosTrackingAuthorization(kCLAuthorizationStatusAuthorizedAlways))
        assertEquals(IosTrackingAuthorization.Denied, iosTrackingAuthorization(kCLAuthorizationStatusDenied))
        assertEquals(IosTrackingAuthorization.Denied, iosTrackingAuthorization(kCLAuthorizationStatusRestricted))
    }
}
