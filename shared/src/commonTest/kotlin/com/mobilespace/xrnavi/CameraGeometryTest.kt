package com.mobilespace.xrnavi

import com.exkmptracker.cooperative.VehicleClass
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CameraGeometryTest {
    @Test
    fun profileLabelsMapOnlyPassengerAndCargo() {
        assertEquals(VehicleClass.CAR, egoVehicleClass("Легкове"))
        assertEquals(VehicleClass.CAR, egoVehicleClass("passenger"))
        assertEquals(VehicleClass.TRUCK, egoVehicleClass("Вантажне"))
        assertEquals(VehicleClass.TRUCK, egoVehicleClass("truck"))
        assertNull(egoVehicleClass("Спецтехніка"))
        assertNull(egoVehicleClass("bus"))
        assertNull(egoVehicleClass("тролейбус"))
    }

    @Test
    fun roadPlaneIsTheAppMountNotTheVisionSample() {
        val car = roadPlane(VehicleClass.CAR)!!
        val truck = roadPlane(VehicleClass.TRUCK)!!
        assertEquals(1.35, car.heightMeters)
        assertEquals(0.30, car.heightStdDevMeters)
        assertEquals(2.40, truck.heightMeters)
        assertTrue(abs(car.heightMeters - 2.0) > 0.2)
        assertNull(roadPlane(VehicleClass.BUS))
        assertNull(roadPlane(VehicleClass.TROLLEYBUS))
    }

    @Test
    fun activeArrayIntrinsicsRotateIntoTheUprightFrame() {
        val buffer = scaledPinhole(
            activeWidth = 4000,
            activeHeight = 3000,
            fx = 3200.0,
            fy = 3200.0,
            cx = 2000.0,
            cy = 1500.0,
            bufferWidth = 1280,
            bufferHeight = 960,
        )!!
        assertEquals(1024.0, buffer.fx, 1e-9)
        assertEquals(1024.0, buffer.fy, 1e-9)
        assertEquals(639.5, buffer.cx, 1e-9)
        assertEquals(479.5, buffer.cy, 1e-9)
        val upright = rotatePinhole(buffer, 90)!!
        assertEquals(960, upright.width)
        assertEquals(1280, upright.height)
        assertEquals(1024.0, upright.fx, 1e-9)
        assertEquals(959.0 - 479.5, upright.cx, 1e-9)
        assertEquals(639.5, upright.cy, 1e-9)
    }

    @Test
    fun rearCameraFacingNorthIsLevelWithZeroRoll() {
        val pose = opticalPose(Enu(east = 0.0, north = 1.0, up = 0.0), Enu(east = 1.0, north = 0.0, up = 0.0))!!
        assertEquals(0.0, pose.headingDegreesTrue, 1e-9)
        assertEquals(0.0, pose.pitchDownDegrees, 1e-9)
        assertEquals(0.0, pose.rollDegrees, 1e-9)
        assertNull(opticalPose(Enu(0.0, 0.0, -1.0), Enu(1.0, 0.0, 0.0)))
    }

    @Test
    fun rotatingTheFrameNinetyDegreesChangesRollAndKeepsOpticalAzimuth() {
        val optical = Enu(east = 0.0, north = 1.0, up = 0.0)
        val sensorRight = Enu(east = 1.0, north = 0.0, up = 0.0)
        val sensorDown = Enu(east = 0.0, north = 0.0, up = -1.0)
        val level = opticalPose(optical, publishedImageRight(sensorRight, sensorDown, 0)!!)!!
        val turned = opticalPose(optical, publishedImageRight(sensorRight, sensorDown, 90)!!)!!
        assertEquals(0.0, level.headingDegreesTrue, 1e-9)
        assertEquals(0.0, level.rollDegrees, 1e-9)
        assertEquals(level.headingDegreesTrue, turned.headingDegreesTrue, 1e-9)
        assertEquals(0.0, turned.pitchDownDegrees, 1e-9)
        assertEquals(-90.0, turned.rollDegrees, 1e-9)
    }

    @Test
    fun declinationMovesMagneticHeadingTowardTrueNorth() {
        assertEquals(12.0, trueHeading(350.0, 22.0)!!, 1e-9)
    }

    @Test
    fun zeroDistortionSamplesTheSameCornerPixel() {
        val lens = LensDistortion(0.0, 0.0, 0.0, 0.0, 0.0)
        val (u, v) = distortedSample(100.5, 80.5, 900.0, 900.0, 640.0, 360.0, lens)!!
        assertEquals(100.5, u, 1e-9)
        assertEquals(80.5, v, 1e-9)
    }

    @Test
    fun clockwiseRotationMovesTheTopLeftPixelToTheTopRight() {
        val source = RgbaImage(2, 1, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
        val turned = rotateRgba(source, 90)!!
        assertEquals(1, turned.width)
        assertEquals(2, turned.height)
        assertEquals(1, turned.rgba[0])
        assertEquals(5, turned.rgba[4])
    }

    @Test
    fun coreMotionNorthWestUpMatchesEastNorthUp() {
        val north = coreMotionToEnu(x = 1.0, y = 0.0, z = 0.0)
        assertEquals(0.0, north.east, 0.0)
        assertEquals(1.0, north.north, 0.0)
        val east = coreMotionToEnu(x = 0.0, y = -1.0, z = 0.0)
        assertEquals(1.0, east.east, 0.0)
        assertEquals(0.0, east.north, 0.0)
    }
}
