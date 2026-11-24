@file:OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)

package com.mobilespace.xrnavi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Size
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.exkmptracker.cooperative.EdgeVisionController
import com.exkmptracker.cooperative.VehicleClass
import com.exkmptracker.vision.BoxUnits
import com.exkmptracker.vision.CameraCalibration
import com.exkmptracker.vision.CameraPose
import com.exkmptracker.vision.VisionFrame
import com.exkmptracker.vision.VisionFrameSource
import com.exkmptracker.vision.VisionFrameSubscription
import com.exkmptracker.vision.VisionTiming
import com.exkmptracker.vision.YoloConfig
import com.exkmptracker.vision.androidEdgeVision
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.roundToInt

private const val CONTACT_STD_DEV_PIXELS = 2.5
private const val MAX_GROUND_RANGE_METERS = 150.0
private const val POSE_SKEW_NANOS = 50_000_000L

fun xrnaviCameraForeground(resumed: Boolean) {
    XrnaviCameraGate.setForeground(resumed)
}

fun xrnaviEdgeVision(context: Context): EdgeVisionController? {
    val app = context.applicationContext
    val ego = xrnaviCooperativeVehicleClass() ?: return null
    val plane = roadPlane(ego) ?: return null
    if (ContextCompat.checkSelfPermission(app, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        return null
    }
    val model = uk4ModelFile(app) ?: return null
    val manager = app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = selectBackCamera(manager) ?: return null
    val prepared = prepareBackCamera(manager.getCameraCharacteristics(cameraId), plane) ?: return null
    val source = XrnaviFrameSource(app, cameraId, prepared)
    return androidEdgeVision(
        model,
        source,
        prepared.calibration,
        YoloConfig.ukrainianVehicles(BoxUnits.INPUT_PIXELS),
        VisionTiming(maxFps = 30, estimateRadialSpeed = false),
    )
}

private object XrnaviCameraGate {
    @Volatile
    var foreground: Boolean = false
        private set
    private val listeners = mutableListOf<(Boolean) -> Unit>()

    fun setForeground(value: Boolean) {
        foreground = value
        val current = synchronized(listeners) { listeners.toList() }
        current.forEach { it(value) }
    }

    fun listen(listener: (Boolean) -> Unit): () -> Unit {
        synchronized(listeners) { listeners += listener }
        return {
            synchronized(listeners) { listeners -= listener }
        }
    }
}

private data class PreparedCamera(
    val bufferWidth: Int,
    val bufferHeight: Int,
    val clockwise: Int,
    val lens: LensDistortion,
    val upright: Pinhole,
    val calibration: CameraCalibration,
    val remap: IntArray?,
)

private fun prepareBackCamera(chars: CameraCharacteristics, plane: RoadPlane): PreparedCamera? {
    val orientation = chars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: return null
    if (orientation % 90 != 0) return null
    val active = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return null
    val intrinsic = readIntrinsics(chars, active.width(), active.height()) ?: return null
    if (abs(intrinsic.skew) > 0.02 * intrinsic.fx) return null
    val sizes = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        ?.getOutputSizes(ImageFormat.YUV_420_888)
        ?: return null
    val buffer = chooseBufferSize(sizes, active.width(), active.height()) ?: return null
    val scaled = scaledPinhole(
        active.width(), active.height(), intrinsic.fx, intrinsic.fy, intrinsic.cx, intrinsic.cy,
        buffer.width, buffer.height,
    ) ?: return null
    val upright = rotatePinhole(scaled, orientation) ?: return null
    val lens = readDistortion(chars)
    val calibration = try {
        CameraCalibration(
            imageWidth = upright.width,
            imageHeight = upright.height,
            fx = upright.fx,
            fy = upright.fy,
            cx = upright.cx,
            cy = upright.cy,
            cameraHeightMeters = plane.heightMeters,
            heightStdDevMeters = plane.heightStdDevMeters,
            contactStdDevPixels = CONTACT_STD_DEV_PIXELS,
            maxGroundRangeMeters = MAX_GROUND_RANGE_METERS,
        )
    } catch (_: IllegalArgumentException) {
        return null
    }
    val remap = if (lens.trivial()) null else remapUpright(upright, lens)
    return PreparedCamera(buffer.width, buffer.height, orientation, lens, upright, calibration, remap)
}

private data class RawIntrinsics(val fx: Double, val fy: Double, val cx: Double, val cy: Double, val skew: Double)

private fun readIntrinsics(chars: CameraCharacteristics, activeWidth: Int, activeHeight: Int): RawIntrinsics? {
    val reported = chars.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION)
    if (reported != null && reported.size >= 5) {
        return RawIntrinsics(
            reported[0].toDouble(), reported[1].toDouble(), reported[2].toDouble(),
            reported[3].toDouble(), reported[4].toDouble(),
        )
    }
    val focal = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.firstOrNull() ?: return null
    val physical = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) ?: return null
    if (physical.width <= 0f || physical.height <= 0f) return null
    return RawIntrinsics(
        fx = (focal / physical.width * activeWidth).toDouble(),
        fy = (focal / physical.height * activeHeight).toDouble(),
        cx = activeWidth / 2.0,
        cy = activeHeight / 2.0,
        skew = 0.0,
    )
}

private fun readDistortion(chars: CameraCharacteristics): LensDistortion {
    val reported = chars.get(CameraCharacteristics.LENS_DISTORTION)
    if (reported == null || reported.size < 5) return LensDistortion(0.0, 0.0, 0.0, 0.0, 0.0)
    return LensDistortion(
        reported[0].toDouble(), reported[1].toDouble(), reported[2].toDouble(),
        reported[3].toDouble(), reported[4].toDouble(),
    )
}

private fun chooseBufferSize(sizes: Array<Size>, activeWidth: Int, activeHeight: Int): Size? {
    val aspect = activeWidth.toDouble() / activeHeight
    return sizes
        .filter { size ->
            size.width >= 640 && size.height >= 480 &&
                abs(size.width.toDouble() / size.height - aspect) <= 0.02
        }
        .minByOrNull { size -> abs(size.width * size.height - 1280 * 720) }
}

private fun remapUpright(upright: Pinhole, lens: LensDistortion): IntArray {
    val cornerCx = upright.cx + 0.5
    val cornerCy = upright.cy + 0.5
    val map = IntArray(upright.width * upright.height) { -1 }
    for (y in 0 until upright.height) {
        for (x in 0 until upright.width) {
            val sample = distortedSample(
                x + 0.5, y + 0.5, upright.fx, upright.fy, cornerCx, cornerCy, lens,
            ) ?: continue
            val sx = (sample.first - 0.5).roundToInt()
            val sy = (sample.second - 0.5).roundToInt()
            if (sx in 0 until upright.width && sy in 0 until upright.height) {
                map[y * upright.width + x] = sy * upright.width + sx
            }
        }
    }
    return map
}

private fun selectBackCamera(manager: CameraManager): String? =
    manager.cameraIdList.mapNotNull { id ->
        val chars = try {
            manager.getCameraCharacteristics(id)
        } catch (_: Exception) {
            return@mapNotNull null
        }
        if (chars.get(CameraCharacteristics.LENS_FACING) != CameraCharacteristics.LENS_FACING_BACK) return@mapNotNull null
        val compatible = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            ?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE) == true
        val focal = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.maxOrNull() ?: 0f
        Triple(id, compatible, focal)
    }.sortedWith(compareByDescending<Triple<String, Boolean, Float>> { it.second }.thenByDescending { it.third })
        .firstOrNull()
        ?.first

private fun uk4ModelFile(context: Context): File? {
    val dest = File(context.filesDir, "uk4.tflite")
    return try {
        val size = try {
            context.assets.openFd("uk4.tflite").use { it.length }
        } catch (_: IOException) {
            null
        }
        if (!dest.isFile || dest.length() == 0L || (size != null && dest.length() != size)) {
            context.assets.open("uk4.tflite").use { input ->
                dest.outputStream().use { input.copyTo(it) }
            }
        }
        dest.takeIf { it.isFile && it.length() > 0L && (size == null || it.length() == size) }
    } catch (_: IOException) {
        null
    }
}

private class XrnaviFrameSource(
    private val context: Context,
    private val cameraId: String,
    private val prepared: PreparedCamera,
) : VisionFrameSource {
    private val main = Handler(Looper.getMainLooper())
    private var executor = Executors.newSingleThreadExecutor()
    private val pose = OpticalPoseTracker(context, prepared.clockwise)
    private var owner: GateLifecycle? = null
    private var listener: ((VisionFrame) -> Unit)? = null
    private var provider: ProcessCameraProvider? = null
    private var bound = false
    private var stopListening: (() -> Unit)? = null

    override fun subscribe(onFrame: (VisionFrame) -> Unit): VisionFrameSubscription {
        listener = onFrame
        pose.start()
        stopListening = XrnaviCameraGate.listen { resumed -> applyForeground(resumed) }
        applyForeground(XrnaviCameraGate.foreground)
        return VisionFrameSubscription {
            listener = null
            stopListening?.invoke()
            stopListening = null
            pose.stop()
            val retiring = executor
            executor = Executors.newSingleThreadExecutor()
            main.post {
                provider?.unbindAll()
                bound = false
                owner?.move(Lifecycle.State.CREATED)
            }
            retiring.shutdown()
        }
    }

    private fun gate(): GateLifecycle = owner ?: GateLifecycle().also { owner = it }

    private fun applyForeground(resumed: Boolean) {
        main.post {
            if (listener == null) return@post
            if (!resumed) {
                provider?.unbindAll()
                bound = false
                owner?.move(Lifecycle.State.CREATED)
                return@post
            }
            gate().move(Lifecycle.State.RESUMED)
            if (!bound) bind()
        }
    }

    private fun bind() {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (listener == null || !XrnaviCameraGate.foreground) return@addListener
            val cameraProvider = try {
                future.get()
            } catch (_: Exception) {
                return@addListener
            }
            provider = cameraProvider
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                Size(prepared.bufferWidth, prepared.bufferHeight),
                                ResolutionStrategy.FALLBACK_RULE_NONE,
                            ),
                        )
                        .build(),
                )
                .build()
            analysis.setAnalyzer(executor, ::analyze)
            val selector = CameraSelector.Builder()
                .addCameraFilter { cameras ->
                    cameras.filter { Camera2CameraInfo.from(it).cameraId == cameraId }
                }
                .build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(gate(), selector, analysis)
                bound = true
            } catch (_: Exception) {
                bound = false
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun analyze(image: ImageProxy) {
        try {
            val callback = listener ?: return
            if (image.width != prepared.bufferWidth || image.height != prepared.bufferHeight) return
            val plane = image.planes.firstOrNull() ?: return
            val packed = packRgba(plane.buffer, image.width, image.height, plane.rowStride, plane.pixelStride) ?: return
            val rotated = rotateRgba(RgbaImage(image.width, image.height, packed), prepared.clockwise) ?: return
            if (rotated.width != prepared.upright.width || rotated.height != prepared.upright.height) return
            val rgba = prepared.remap?.let { applyRemap(rotated, it) } ?: rotated.rgba
            val captureNs = elapsedNanos(image.imageInfo.timestamp)
            val attitude = pose.attitudeAt(captureNs)
            callback(
                VisionFrame(
                    width = rotated.width,
                    height = rotated.height,
                    rowStrideBytes = rotated.width * 4,
                    rgba = rgba,
                    captureMonotonicSeconds = captureNs / 1e9,
                    pose = attitude?.pose,
                    poseMonotonicSeconds = attitude?.elapsedNanos?.div(1e9),
                ),
            )
        } finally {
            image.close()
        }
    }
}

private class GateLifecycle : LifecycleOwner {
    private val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry

    init {
        registry.currentState = Lifecycle.State.CREATED
    }

    fun move(state: Lifecycle.State) {
        if (registry.currentState != state) registry.currentState = state
    }
}

private data class Attitude(val pose: CameraPose, val elapsedNanos: Long)

private class OpticalPoseTracker(context: Context, private val clockwise: Int) : SensorEventListener {
    private val sensors = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locations = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val rotation = sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    @Volatile private var matrix: FloatArray? = null
    @Volatile private var stampedAt: Long = 0L
    @Volatile private var accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE

    fun start() {
        rotation?.let { sensors.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stop() {
        sensors.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val next = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(next, event.values)
        matrix = next
        stampedAt = event.timestamp
        accuracy = event.accuracy
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        this.accuracy = accuracy
    }

    fun attitudeAt(captureElapsedNanos: Long): Attitude? {
        val current = matrix ?: return null
        val time = stampedAt
        if (abs(time - captureElapsedNanos) > POSE_SKEW_NANOS) return null
        val (headingStd, tiltStd) = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> 2.0 to 1.5
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> 5.0 to 3.0
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> 12.0 to 6.0
            else -> return null
        }
        val optical = Enu(-current[2].toDouble(), -current[5].toDouble(), -current[8].toDouble())
        val deviceX = Enu(current[0].toDouble(), current[3].toDouble(), current[6].toDouble())
        val deviceY = Enu(current[1].toDouble(), current[4].toDouble(), current[7].toDouble())
        val imageDown = Enu(-deviceY.east, -deviceY.north, -deviceY.up)
        val imageRight = publishedImageRight(deviceX, imageDown, clockwise) ?: return null
        val body = opticalPose(optical, imageRight) ?: return null
        val location = lastLocation() ?: return null
        val declination = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            location.time,
        ).declination.toDouble()
        val heading = trueHeading(body.headingDegreesTrue, declination) ?: return null
        val pose = try {
            CameraPose(heading, body.pitchDownDegrees, body.rollDegrees, headingStd, tiltStd)
        } catch (_: IllegalArgumentException) {
            return null
        }
        return Attitude(pose, time)
    }

    private fun lastLocation(): Location? = try {
        locations.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locations.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (_: SecurityException) {
        null
    }
}

private fun packRgba(buffer: ByteBuffer, width: Int, height: Int, rowStride: Int, pixelStride: Int): ByteArray? {
    if (pixelStride != 4 || rowStride < width * 4) return null
    val start = buffer.position()
    if (start + (height - 1) * rowStride + width * 4 > buffer.limit()) return null
    val out = ByteArray(width * height * 4)
    for (y in 0 until height) {
        val row = start + y * rowStride
        for (x in 0 until width) {
            val from = row + x * 4
            val to = (y * width + x) * 4
            out[to] = buffer.get(from)
            out[to + 1] = buffer.get(from + 1)
            out[to + 2] = buffer.get(from + 2)
            out[to + 3] = buffer.get(from + 3)
        }
    }
    return out
}

private fun applyRemap(source: RgbaImage, map: IntArray): ByteArray {
    val out = ByteArray(source.rgba.size)
    val pixels = source.width * source.height
    for (index in 0 until pixels) {
        val from = map[index]
        if (from < 0) continue
        val to = index * 4
        val src = from * 4
        out[to] = source.rgba[src]
        out[to + 1] = source.rgba[src + 1]
        out[to + 2] = source.rgba[src + 2]
        out[to + 3] = source.rgba[src + 3]
    }
    return out
}

private fun elapsedNanos(cameraNanos: Long): Long {
    val elapsed = SystemClock.elapsedRealtimeNanos()
    val age = elapsed - cameraNanos
    if (age in -POSE_SKEW_NANOS..5_000_000_000L) return cameraNanos
    return elapsed - (System.nanoTime() - cameraNanos)
}
