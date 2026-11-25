package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosCameraReconcileTest {
    @Test
    fun deniedLocationDoesNotOpenTheCamera() {
        assertFalse(iosCameraReconcileEnabled(IosTrackingAuthorization.Denied, trackingSessionActive = true))
        assertFalse(iosCameraReconcileEnabled(IosTrackingAuthorization.Denied, trackingSessionActive = false))
        assertFalse(iosCameraReconcileEnabled(IosTrackingAuthorization.RequestWhenInUse, trackingSessionActive = true))
        assertFalse(iosCameraReconcileEnabled(IosTrackingAuthorization.Foreground, trackingSessionActive = false))
        assertFalse(iosCameraReconcileEnabled(IosTrackingAuthorization.Background, trackingSessionActive = false))
    }

    @Test
    fun grantedLocationResumesTheCameraOnlyWhileTrackingStaysActive() {
        assertTrue(iosCameraReconcileEnabled(IosTrackingAuthorization.Foreground, trackingSessionActive = true))
        assertTrue(iosCameraReconcileEnabled(IosTrackingAuthorization.Background, trackingSessionActive = true))
    }
}
