package com.mobilespace.xrnavi

import platform.AVFoundation.AVCaptureDevice

internal expect fun captureAuthorizationStatus(mediaType: String?): Long

internal expect fun requestCaptureAccess(mediaType: String?, onComplete: (Boolean) -> Unit)

internal expect fun enableGeometricDistortionCorrection(device: AVCaptureDevice)
