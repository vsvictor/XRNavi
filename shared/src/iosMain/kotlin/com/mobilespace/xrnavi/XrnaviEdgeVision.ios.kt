@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mobilespace.xrnavi

import com.exkmptracker.cooperative.EdgeVisionController
import com.exkmptracker.cooperative.VehicleClass
import com.exkmptracker.ios.IosTrackerClock
import com.exkmptracker.vision.BoxUnits
import com.exkmptracker.vision.CameraCalibration
import com.exkmptracker.vision.CameraPose
import com.exkmptracker.vision.VisionFrame
import com.exkmptracker.vision.VisionFrameSource
import com.exkmptracker.vision.VisionFrameSubscription
import com.exkmptracker.vision.VisionTiming
import com.exkmptracker.vision.YoloConfig
import com.exkmptracker.vision.iosEdgeVision
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.useContents
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceFormat
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVMediaTypeVideo
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataGetTypeID
import platform.CoreFoundation.CFGetTypeID
import platform.CoreMedia.CMGetAttachment
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferGetPresentationTimeStamp
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreMedia.CMVideoFormatDescriptionGetDimensions
import platform.CoreMedia.kCMSampleBufferAttachmentKey_CameraIntrinsicMatrix
import platform.CoreMotion.CMAttitudeReferenceFrameXTrueNorthZVertical
import platform.CoreMotion.CMDeviceMotion
import platform.CoreMotion.CMMagneticFieldCalibrationAccuracyHigh
import platform.CoreMotion.CMMagneticFieldCalibrationAccuracyLow
import platform.CoreMotion.CMMagneticFieldCalibrationAccuracyMedium
import platform.CoreMotion.CMMotionManager
import platform.CoreVideo.CVPixelBufferGetBaseAddress
import platform.CoreVideo.CVPixelBufferGetBytesPerRow
import platform.CoreVideo.CVPixelBufferGetHeight
import platform.CoreVideo.CVPixelBufferGetWidth
import platform.CoreVideo.CVPixelBufferLockBaseAddress
import platform.CoreVideo.CVPixelBufferUnlockBaseAddress
import platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey
import platform.CoreVideo.kCVPixelFormatType_32BGRA
import platform.CoreVideo.kCVReturnSuccess
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_queue_t
import kotlin.math.abs

private const val CONTACT_STD_DEV_PIXELS = 2.5
private const val MAX_GROUND_RANGE_METERS = 150.0
private const val POSE_SKEW_SECONDS = 0.05

internal suspend fun iosCooperativeVision(ego: VehicleClass): EdgeVisionController? {
    val plane = roadPlane(ego) ?: return null
    val model = NSBundle.mainBundle.URLForResource("uk4", withExtension = "mlmodelc") ?: return null
    val camera = IosCooperativeCamera.open(plane) ?: return null
    return iosEdgeVision(
        model,
        camera,
        camera.calibration,
        YoloConfig.ukrainianVehicles(BoxUnits.INPUT_PIXELS),
        VisionTiming(maxFps = 30, estimateRadialSpeed = false),
    )
}

internal suspend fun iosCameraAuthorized(): Boolean = suspendCancellableCoroutine { cont ->
    when (captureAuthorizationStatus(AVMediaTypeVideo)) {
        platform.AVFoundation.AVAuthorizationStatusAuthorized -> cont.resume(true)
        platform.AVFoundation.AVAuthorizationStatusNotDetermined ->
            requestCaptureAccess(AVMediaTypeVideo) { granted ->
                cont.resume(granted)
            }
        else -> cont.resume(false)
    }
}

private class IosCooperativeCamera private constructor(
    private val session: AVCaptureSession,
    private val queue: dispatch_queue_t,
    private val delegate: FrameDelegate,
    val calibration: CameraCalibration,
    private val expected: ProbeIntrinsics,
) : VisionFrameSource {
    private val clock = IosTrackerClock()
    private val motion = OpticalMotion(clock)
    private var listener: ((VisionFrame) -> Unit)? = null
    private var foreground = true
    private var backgroundObserver: Any? = null
    private var foregroundObserver: Any? = null

    override fun subscribe(onFrame: (VisionFrame) -> Unit): VisionFrameSubscription {
        listener = onFrame
        delegate.onFrame = ::publish
        motion.start()
        val center = NSNotificationCenter.defaultCenter
        backgroundObserver = center.addObserverForName(
            UIApplicationDidEnterBackgroundNotification, null, NSOperationQueue.mainQueue,
        ) { _ -> setRunning(false) }
        foregroundObserver = center.addObserverForName(
            UIApplicationWillEnterForegroundNotification, null, NSOperationQueue.mainQueue,
        ) { _ -> setRunning(true) }
        setRunning(true)
        return VisionFrameSubscription {
            listener = null
            delegate.onFrame = null
            motion.stop()
            setRunning(false)
            backgroundObserver?.let { center.removeObserver(it) }
            foregroundObserver?.let { center.removeObserver(it) }
            backgroundObserver = null
            foregroundObserver = null
        }
    }

    private fun setRunning(active: Boolean) {
        foreground = active
        dispatch_async(queue) {
            if (active && listener != null) {
                if (!session.isRunning()) session.startRunning()
            } else if (session.isRunning()) {
                session.stopRunning()
            }
        }
    }

    private fun publish(frame: ProbeFrame) {
        val callback = listener ?: return
        if (!foreground) return
        if (frame.width != calibration.imageWidth || frame.height != calibration.imageHeight) return
        if (!closeTo(frame)) return
        val capture = hostToTracker(frame.hostSeconds)
        val attitude = motion.attitude(frame.hostSeconds)
        callback(
            VisionFrame(
                width = frame.width,
                height = frame.height,
                rowStrideBytes = frame.width * 4,
                rgba = frame.rgba,
                captureMonotonicSeconds = capture,
                pose = attitude?.pose,
                poseMonotonicSeconds = attitude?.trackerSeconds,
            ),
        )
    }

    private fun closeTo(frame: ProbeFrame): Boolean =
        abs(frame.fx - expected.fx) < 1.5 && abs(frame.fy - expected.fy) < 1.5 &&
            abs(frame.cx - expected.cx) < 1.5 && abs(frame.cy - expected.cy) < 1.5

    private fun hostToTracker(hostSeconds: Double): Double =
        clock.monotonicSeconds() - (NSProcessInfo.processInfo.systemUptime - hostSeconds)

    companion object {
        suspend fun open(plane: RoadPlane): IosCooperativeCamera? {
            val session = AVCaptureSession()
            val queue = dispatch_queue_create("xrnavi.camera", null) ?: return null
            val delegate = FrameDelegate()
            if (!configure(session, queue, delegate)) return null
            val first = CompletableDeferred<ProbeFrame?>()
            delegate.onFrame = { frame -> if (!first.isCompleted) first.complete(frame) }
            dispatch_async(queue) { session.startRunning() }
            val probe = withTimeoutOrNull(2_000) { first.await() }
            dispatch_async(queue) { if (session.isRunning()) session.stopRunning() }
            delegate.onFrame = null
            val frame = probe ?: return null
            val calibration = try {
                CameraCalibration(
                    imageWidth = frame.width,
                    imageHeight = frame.height,
                    fx = frame.fx,
                    fy = frame.fy,
                    cx = frame.cx - 0.5,
                    cy = frame.cy - 0.5,
                    cameraHeightMeters = plane.heightMeters,
                    heightStdDevMeters = plane.heightStdDevMeters,
                    contactStdDevPixels = CONTACT_STD_DEV_PIXELS,
                    maxGroundRangeMeters = MAX_GROUND_RANGE_METERS,
                )
            } catch (_: IllegalArgumentException) {
                return null
            }
            return IosCooperativeCamera(
                session, queue, delegate, calibration,
                ProbeIntrinsics(frame.fx, frame.fy, frame.cx, frame.cy),
            )
        }
    }
}

private class FrameDelegate : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    var onFrame: ((ProbeFrame) -> Unit)? = null

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        val sample = didOutputSampleBuffer ?: return
        val callback = onFrame ?: return
        val frame = readFrame(sample) ?: return
        callback(frame)
    }
}

private data class ProbeIntrinsics(val fx: Double, val fy: Double, val cx: Double, val cy: Double)

private data class ProbeFrame(
    val width: Int,
    val height: Int,
    val rgba: ByteArray,
    val fx: Double,
    val fy: Double,
    val cx: Double,
    val cy: Double,
    val hostSeconds: Double,
)

private data class Attitude(val pose: CameraPose, val trackerSeconds: Double)

private fun configure(
    session: AVCaptureSession,
    queue: dispatch_queue_t,
    delegate: FrameDelegate,
): Boolean = memScoped {
    val discovery = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
        AVMediaTypeVideo,
        AVCaptureDevicePositionBack,
    )
    val device = discovery.devices.firstOrNull() as? AVCaptureDevice ?: return false
    val format = device.formats.mapNotNull { it as? AVCaptureDeviceFormat }.minByOrNull { candidate ->
        val dimensions = CMVideoFormatDescriptionGetDimensions(candidate.formatDescription)
        val area = dimensions.useContents { width.toLong() * height.toLong() }
        abs(area - 1280L * 720L)
    } ?: return false
    val error = alloc<ObjCObjectVar<NSError?>>()
    if (!device.lockForConfiguration(error.ptr)) return false
    device.activeFormat = format
    enableGeometricDistortionCorrection(device)
    device.unlockForConfiguration()
    val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error.ptr) ?: return false
    session.beginConfiguration()
    if (!session.canAddInput(input)) {
        session.commitConfiguration()
        return false
    }
    session.addInput(input)
    val output = AVCaptureVideoDataOutput()
    output.videoSettings = mapOf(kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_32BGRA)
    output.alwaysDiscardsLateVideoFrames = true
    if (!session.canAddOutput(output)) {
        session.commitConfiguration()
        return false
    }
    session.addOutput(output)
    output.setSampleBufferDelegate(delegate, queue)
    val connection = output.connectionWithMediaType(AVMediaTypeVideo) ?: run {
        session.commitConfiguration()
        return false
    }
    if (connection.isVideoOrientationSupported()) connection.videoOrientation = AVCaptureVideoOrientationPortrait
    if (!connection.isCameraIntrinsicMatrixDeliverySupported()) {
        session.commitConfiguration()
        return false
    }
    connection.cameraIntrinsicMatrixDeliveryEnabled = true
    session.commitConfiguration()
    true
}

private fun readFrame(sample: CMSampleBufferRef): ProbeFrame? {
    val pixel = CMSampleBufferGetImageBuffer(sample) ?: return null
    if (CVPixelBufferLockBaseAddress(pixel, 0u) != kCVReturnSuccess) return null
    try {
        val width = CVPixelBufferGetWidth(pixel).toInt()
        val height = CVPixelBufferGetHeight(pixel).toInt()
        if (width <= 0 || height <= 0) return null
        val stride = CVPixelBufferGetBytesPerRow(pixel).toInt()
        val base = CVPixelBufferGetBaseAddress(pixel) ?: return null
        val bytes = base.reinterpret<ByteVar>()
        val rgba = ByteArray(width * height * 4)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val src = y * stride + x * 4
                val dst = (y * width + x) * 4
                rgba[dst] = bytes[src + 2]
                rgba[dst + 1] = bytes[src + 1]
                rgba[dst + 2] = bytes[src]
                rgba[dst + 3] = bytes[src + 3]
            }
        }
        val lens = intrinsicMatrix(sample) ?: return null
        val host = CMSampleBufferGetPresentationTimeStamp(sample).useContents {
            if (timescale == 0) return null
            value.toDouble() / timescale
        }
        return ProbeFrame(width, height, rgba, lens.fx, lens.fy, lens.cx, lens.cy, host)
    } finally {
        CVPixelBufferUnlockBaseAddress(pixel, 0u)
    }
}

private fun intrinsicMatrix(sample: CMSampleBufferRef): ProbeIntrinsics? {
    val attachment = CMGetAttachment(sample, kCMSampleBufferAttachmentKey_CameraIntrinsicMatrix, null) ?: return null
    if (CFGetTypeID(attachment) != CFDataGetTypeID()) return null
    val length = CFDataGetLength(attachment.reinterpret()).toInt()
    val pointer = CFDataGetBytePtr(attachment.reinterpret()) ?: return null
    val values = pointer.reinterpret<FloatVar>()
    fun at(index: Int) = values[index].toDouble()
    val fx: Double
    val fy: Double
    val cx: Double
    val cy: Double
    when {
        length >= 48 -> {
            fx = at(0)
            fy = at(5)
            cx = at(8)
            cy = at(9)
        }
        length >= 36 -> {
            fx = at(0)
            fy = at(4)
            cx = at(6)
            cy = at(7)
        }
        else -> return null
    }
    if (fx <= 0.0 || fy <= 0.0 || !cx.isFinite() || !cy.isFinite()) return null
    return ProbeIntrinsics(fx, fy, cx, cy)
}

private class OpticalMotion(private val clock: IosTrackerClock) {
    private val manager = CMMotionManager()
    private var sample: MotionSample? = null

    fun start() {
        if (!manager.isDeviceMotionAvailable()) return
        val frames = CMMotionManager.availableAttitudeReferenceFrames()
        if ((frames and CMAttitudeReferenceFrameXTrueNorthZVertical) == 0uL) return
        manager.deviceMotionUpdateInterval = 1.0 / 60.0
        manager.startDeviceMotionUpdatesUsingReferenceFrame(
            CMAttitudeReferenceFrameXTrueNorthZVertical,
            NSOperationQueue.mainQueue,
        ) { motion, _ -> motion?.let(::record) }
    }

    fun stop() {
        if (manager.isDeviceMotionActive()) manager.stopDeviceMotionUpdates()
        sample = null
    }

    fun attitude(hostSeconds: Double): Attitude? {
        val current = sample ?: return null
        if (abs(current.hostSeconds - hostSeconds) > POSE_SKEW_SECONDS) return null
        val matrix = current.matrix
        val optical = coreMotionToEnu(-matrix[2], -matrix[5], -matrix[8])
        val imageRight = coreMotionToEnu(matrix[0], matrix[3], matrix[6])
        val body = opticalPose(optical, imageRight) ?: return null
        val pose = try {
            CameraPose(
                body.headingDegreesTrue, body.pitchDownDegrees, body.rollDegrees,
                current.headingStd, current.tiltStd,
            )
        } catch (_: IllegalArgumentException) {
            return null
        }
        val tracker = clock.monotonicSeconds() - (NSProcessInfo.processInfo.systemUptime - current.hostSeconds)
        return Attitude(pose, tracker)
    }

    private fun record(motion: CMDeviceMotion) {
        val accuracy = motion.magneticField.useContents { accuracy }
        val spread = when (accuracy) {
            CMMagneticFieldCalibrationAccuracyHigh -> 2.0 to 1.5
            CMMagneticFieldCalibrationAccuracyMedium -> 5.0 to 3.0
            CMMagneticFieldCalibrationAccuracyLow -> 12.0 to 6.0
            else -> {
                sample = null
                return
            }
        }
        val matrix = motion.attitude.rotationMatrix.useContents {
            doubleArrayOf(m11, m12, m13, m21, m22, m23, m31, m32, m33)
        }
        sample = MotionSample(motion.timestamp, matrix, spread.first, spread.second)
    }
}

private data class MotionSample(
    val hostSeconds: Double,
    val matrix: DoubleArray,
    val headingStd: Double,
    val tiltStd: Double,
)
