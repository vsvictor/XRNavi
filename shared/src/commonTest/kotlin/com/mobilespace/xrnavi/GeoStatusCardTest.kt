package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GeoStatusCardTest {
    @Test
    fun acquiringAndTimeoutDoNotShowCards() {
        val acquiring = GeoState(
            permission = LocationPermission.Granted,
            active = true,
            acquiringSince = 1_000,
            now = 2_000,
        )
        assertEquals(GeoPhase.Acquiring, acquiring.phase)
        assertNull(geoCardContent(acquiring))

        val timedOut = acquiring.copy(now = 32_000)
        assertEquals(GeoPhase.TimedOut, timedOut.phase)
        assertNull(geoCardContent(timedOut))
        assertNotNull(geoCardContent(acquiring.copy(permission = LocationPermission.Denied)))
    }
}
