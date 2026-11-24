@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mobilespace.xrnavi

import platform.AVFoundation.AVCaptureDevice
import xrnavi.capture.xrnaviAuthorizationStatus
import xrnavi.capture.xrnaviEnableDistortionCorrection
import xrnavi.capture.xrnaviRequestAccess

internal actual fun captureAuthorizationStatus(mediaType: String?): Long =
    xrnaviAuthorizationStatus(mediaType)

internal actual fun requestCaptureAccess(mediaType: String?, onComplete: (Boolean) -> Unit) {
    xrnaviRequestAccess(mediaType, onComplete)
}

internal actual fun enableGeometricDistortionCorrection(device: AVCaptureDevice) {
    xrnaviEnableDistortionCorrection(device)
}
