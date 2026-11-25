package com.mobilespace.xrnavi

internal enum class IosTrackingAuthorization {
    RequestWhenInUse,
    Foreground,
    Background,
    Denied,
}

/** Camera probe runs only while tracking is still requested and location is granted. */
internal fun iosCameraReconcileEnabled(
    permission: IosTrackingAuthorization,
    trackingSessionActive: Boolean,
): Boolean = trackingSessionActive && when (permission) {
    IosTrackingAuthorization.Foreground, IosTrackingAuthorization.Background -> true
    IosTrackingAuthorization.RequestWhenInUse, IosTrackingAuthorization.Denied -> false
}
