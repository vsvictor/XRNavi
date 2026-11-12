package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PrimarySectionSwipeTest {
    @Test
    fun nextDirectionCyclesInConfiguredOrder() {
        assertEquals(DemoSection.Garage, primarySectionAfterSwipe(
            DemoSection.Map, PrimarySectionSwipeDirection.Next
        ))
        assertEquals(DemoSection.Settings, primarySectionAfterSwipe(
            DemoSection.Garage, PrimarySectionSwipeDirection.Next
        ))
        assertEquals(DemoSection.Profile, primarySectionAfterSwipe(
            DemoSection.Settings, PrimarySectionSwipeDirection.Next
        ))
        assertEquals(DemoSection.Map, primarySectionAfterSwipe(
            DemoSection.Profile, PrimarySectionSwipeDirection.Next
        ))
    }

    @Test
    fun previousDirectionCyclesInReverseOrder() {
        assertEquals(DemoSection.Profile, primarySectionAfterSwipe(
            DemoSection.Map, PrimarySectionSwipeDirection.Previous
        ))
        assertEquals(DemoSection.Map, primarySectionAfterSwipe(
            DemoSection.Garage, PrimarySectionSwipeDirection.Previous
        ))
        assertEquals(DemoSection.Garage, primarySectionAfterSwipe(
            DemoSection.Settings, PrimarySectionSwipeDirection.Previous
        ))
        assertEquals(DemoSection.Settings, primarySectionAfterSwipe(
            DemoSection.Profile, PrimarySectionSwipeDirection.Previous
        ))
    }

    @Test
    fun nestedSectionsDoNotResolveToPrimarySections() {
        assertNull(primarySectionAfterSwipe(
            DemoSection.VehicleDetails, PrimarySectionSwipeDirection.Next
        ))
    }

    @Test
    fun edgeDetectionUsesGestureStartPosition() {
        assertEquals(true, HorizontalSwipe(20f, 90f, 400f).startedAtEdge(32f))
        assertEquals(true, HorizontalSwipe(380f, -90f, 400f).startedAtEdge(32f))
        assertEquals(false, HorizontalSwipe(100f, 90f, 400f).startedAtEdge(32f))
    }
}
