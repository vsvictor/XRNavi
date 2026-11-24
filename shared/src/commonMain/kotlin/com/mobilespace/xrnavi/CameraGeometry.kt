package com.mobilespace.xrnavi

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

internal data class Pinhole(
    val width: Int,
    val height: Int,
    val fx: Double,
    val fy: Double,
    val cx: Double,
    val cy: Double,
)

internal data class Enu(val east: Double, val north: Double, val up: Double)

internal data class OpticalPose(
    val headingDegreesTrue: Double,
    val pitchDownDegrees: Double,
    val rollDegrees: Double,
)

internal data class LensDistortion(
    val k1: Double,
    val k2: Double,
    val k3: Double,
    val p1: Double,
    val p2: Double,
) {
    fun trivial(): Boolean = listOf(k1, k2, k3, p1, p2).all { abs(it) < 1e-8 }
}

internal data class RgbaImage(val width: Int, val height: Int, val rgba: ByteArray)

/**
 * Scales active-array corner intrinsics onto a buffer that shows that full array, then shifts
 * the principal point into the tracker's pixel-index coordinates.
 */
internal fun scaledPinhole(
    activeWidth: Int,
    activeHeight: Int,
    fx: Double,
    fy: Double,
    cx: Double,
    cy: Double,
    bufferWidth: Int,
    bufferHeight: Int,
): Pinhole? {
    if (activeWidth <= 0 || activeHeight <= 0 || bufferWidth <= 0 || bufferHeight <= 0) return null
    if (listOf(fx, fy).any { !it.isFinite() || it <= 0.0 }) return null
    if (!cx.isFinite() || !cy.isFinite()) return null
    val sx = bufferWidth.toDouble() / activeWidth
    val sy = bufferHeight.toDouble() / activeHeight
    return Pinhole(
        width = bufferWidth,
        height = bufferHeight,
        fx = fx * sx,
        fy = fy * sy,
        cx = cx * sx - 0.5,
        cy = cy * sy - 0.5,
    )
}

/** Clockwise rotation of a full-frame pinhole into the published upright image. */
internal fun rotatePinhole(buffer: Pinhole, clockwiseDegrees: Int): Pinhole? {
    val w = buffer.width
    val h = buffer.height
    return when (clockwiseDegrees.mod(360)) {
        0 -> buffer
        90 -> Pinhole(h, w, buffer.fy, buffer.fx, (h - 1) - buffer.cy, buffer.cx)
        180 -> Pinhole(w, h, buffer.fx, buffer.fy, (w - 1) - buffer.cx, (h - 1) - buffer.cy)
        270 -> Pinhole(h, w, buffer.fy, buffer.fx, buffer.cy, (w - 1) - buffer.cx)
        else -> null
    }
}

/**
 * Optical-axis attitude in an east-north-up frame.
 * [optical] looks out of the lens. [imageRight] is +x of the published upright frame.
 * Pitch is positive below the horizon. Positive roll turns image-right toward level-down.
 * A ray too close to vertical has no stable heading and is withheld.
 */
/**
 * +x of the frame after a clockwise rotation of the sensor buffer.
 * [imageRight] and [imageDown] belong to the buffer before that rotation.
 * A 90° clockwise turn puts the buffer's image-up on the published +x.
 */
internal fun publishedImageRight(imageRight: Enu, imageDown: Enu, clockwiseDegrees: Int): Enu? =
    when (clockwiseDegrees.mod(360)) {
        0 -> imageRight
        90 -> Enu(-imageDown.east, -imageDown.north, -imageDown.up)
        180 -> Enu(-imageRight.east, -imageRight.north, -imageRight.up)
        270 -> imageDown
        else -> null
    }

internal fun opticalPose(optical: Enu, imageRight: Enu): OpticalPose? {
    val o = unit(optical) ?: return null
    val right = unit(imageRight) ?: return null
    val horizontal = hypot(o.east, o.north)
    if (horizontal < 0.2) return null
    val heading = (atan2(o.east, o.north) * DEGREE + 360.0) % 360.0
    val pitchDown = atan2(-o.up, horizontal) * DEGREE
    if (pitchDown <= -89.0 || pitchDown >= 89.0) return null
    val up = Enu(0.0, 0.0, 1.0)
    val levelRight = unit(cross(o, up)) ?: return null
    val levelDown = unit(cross(o, levelRight)) ?: return null
    val roll = atan2(dot(right, levelDown), dot(right, levelRight)) * DEGREE
    if (roll !in -180.0..180.0) return null
    return OpticalPose(heading, pitchDown, roll)
}

internal fun trueHeading(magneticHeading: Double, declinationDegrees: Double): Double? {
    if (!magneticHeading.isFinite() || !declinationDegrees.isFinite()) return null
    return (magneticHeading + declinationDegrees).mod(360.0)
}

/** Brown-Conrady sample point in corner pixels for a corrected corner pixel. */
internal fun distortedSample(
    u: Double,
    v: Double,
    fx: Double,
    fy: Double,
    cx: Double,
    cy: Double,
    lens: LensDistortion,
): Pair<Double, Double>? {
    if (listOf(u, v, fx, fy, cx, cy).any { !it.isFinite() } || fx == 0.0 || fy == 0.0) return null
    val x = (u - cx) / fx
    val y = (v - cy) / fy
    val r2 = x * x + y * y
    val radial = 1.0 + lens.k1 * r2 + lens.k2 * r2 * r2 + lens.k3 * r2 * r2 * r2
    val x2 = x * x
    val y2 = y * y
    val xy = x * y
    val xd = x * radial + 2.0 * lens.p1 * xy + lens.p2 * (r2 + 2.0 * x2)
    val yd = y * radial + lens.p1 * (r2 + 2.0 * y2) + 2.0 * lens.p2 * xy
    return (xd * fx + cx) to (yd * fy + cy)
}

internal fun rotateRgba(source: RgbaImage, clockwiseDegrees: Int): RgbaImage? {
    val turns = clockwiseDegrees.mod(360)
    if (turns == 0) return source
    if (turns != 90 && turns != 180 && turns != 270) return null
    val w = source.width
    val h = source.height
    if (w <= 0 || h <= 0 || source.rgba.size < w * h * 4) return null
    val dw = if (turns == 180) w else h
    val dh = if (turns == 180) h else w
    val out = ByteArray(dw * dh * 4)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val (dx, dy) = when (turns) {
                90 -> (h - 1 - y) to x
                180 -> (w - 1 - x) to (h - 1 - y)
                else -> y to (w - 1 - x)
            }
            val from = (y * w + x) * 4
            val to = (dy * dw + dx) * 4
            out[to] = source.rgba[from]
            out[to + 1] = source.rgba[from + 1]
            out[to + 2] = source.rgba[from + 2]
            out[to + 3] = source.rgba[from + 3]
        }
    }
    return RgbaImage(dw, dh, out)
}

/** CoreMotion reference (north, west, up) into east-north-up. */
internal fun coreMotionToEnu(x: Double, y: Double, z: Double): Enu = Enu(east = -y, north = x, up = z)

private const val DEGREE = 180.0 / PI

private fun dot(a: Enu, b: Enu) = a.east * b.east + a.north * b.north + a.up * b.up

private fun cross(a: Enu, b: Enu) = Enu(
    a.north * b.up - a.up * b.north,
    a.up * b.east - a.east * b.up,
    a.east * b.north - a.north * b.east,
)

private fun unit(v: Enu): Enu? {
    val length = hypot(v.east, hypot(v.north, v.up))
    if (!length.isFinite() || length < 1e-6) return null
    return Enu(v.east / length, v.north / length, v.up / length)
}
