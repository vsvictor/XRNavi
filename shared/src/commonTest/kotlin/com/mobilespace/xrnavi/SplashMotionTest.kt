package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals

class SplashMotionTest {
    @Test
    fun matchesFigmaAppearanceAndSettleKeyframes() {
        val initial = splashMotion(0, tablet = false)
        assertEquals(0.9f, initial.scale, 0.0001f)
        assertEquals(0.25f, initial.alpha, 0.0001f)
        assertEquals(10.5f, initial.translationX, 0.0001f)
        assertEquals(20.032222f, initial.translationY, 0.0001f)
        assertEquals(1f, splashMotion(450, false).alpha, 0.0001f)
        assertEquals(1.02f, splashMotion(480, false).scale, 0.0001f)
        assertEquals(-2.606444f, splashMotion(480, false).translationY, 0.0001f)
        assertEquals(21.809629f, splashMotion(0, true).translationY, 0.0001f)
        assertEquals(-2.8f, splashMotion(480, true).translationX, 0.0001f)
        assertEquals(-2.961926f, splashMotion(480, true).translationY, 0.0001f)
        for (tablet in listOf(false, true)) {
            val settled = splashMotion(720, tablet)
            assertEquals(1f, settled.scale, 0.0001f)
            assertEquals(0f, settled.translationX, 0.0001f)
            assertEquals(0f, settled.translationY, 0.0001f)
        }
    }

    @Test
    fun satelliteMakesOneTwoSecondRevolutionAfter200MillisecondDelay() {
        assertEquals(0f, splashMotion(0, false).orbitDegrees)
        assertEquals(0f, splashMotion(200, false).orbitDegrees)
        assertEquals(180f, splashMotion(1_200, false).orbitDegrees)
        assertEquals(360f, splashMotion(2_200, false).orbitDegrees)
        assertEquals(360f, splashMotion(2_400, false).orbitDegrees)
    }
}
