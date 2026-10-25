package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun startupUsesMapOnlyForConfiguredSession() {
        assertEquals(AppScreen.Map, initialScreenForSession(hasConfiguredSession = true))
        assertEquals(AppScreen.SignIn, initialScreenForSession(hasConfiguredSession = false))
    }

    @Test
    fun changingSearchTargetClearsThePreviousTargetQuery() {
        assertEquals(
            "",
            searchQueryAfterTargetChange(
                currentTarget = SearchTarget.Origin,
                nextTarget = SearchTarget.Destination,
                query = "Хрещатик"
            )
        )
        assertEquals(
            "Хрещатик",
            searchQueryAfterTargetChange(
                currentTarget = SearchTarget.Destination,
                nextTarget = SearchTarget.Destination,
                query = "Хрещатик"
            )
        )
    }

    @Test
    fun sheetLevelControlsMoveBetweenAdjacentAnchors() {
        assertEquals(SheetLevel.Partial, nextSheetLevel(SheetLevel.Collapsed))
        assertEquals(SheetLevel.Expanded, nextSheetLevel(SheetLevel.Partial))
        assertEquals(SheetLevel.Collapsed, nextSheetLevel(SheetLevel.Expanded))

        assertEquals(SheetLevel.Collapsed, previousSheetLevel(SheetLevel.Collapsed))
        assertEquals(SheetLevel.Collapsed, previousSheetLevel(SheetLevel.Partial))
        assertEquals(SheetLevel.Partial, previousSheetLevel(SheetLevel.Expanded))
    }

    @Test
    fun sheetAnchorsReserveAttributionAndFitExpandedTextScale() {
        val landscape = calculateMapSheetAnchors(
            mapHeightPx = 960f,
            density = 2f,
            fontScale = 1f
        )
        val largeText = calculateMapSheetAnchors(
            mapHeightPx = 960f,
            density = 2f,
            fontScale = 2f
        )

        assertTrue(landscape.collapsed < landscape.partial)
        assertTrue(landscape.partial < landscape.expanded)
        assertTrue(landscape.collapsed >= 173f * 2f)
        assertTrue(landscape.attributionBand >= 44f * 2f)
        assertTrue(landscape.expanded + landscape.attributionBand <= 960f)
        assertTrue(largeText.collapsed > landscape.collapsed)
        assertTrue(largeText.collapsed >= 239f * 2f)
        assertTrue(largeText.attributionBand >= 80f * 2f)
        assertTrue(largeText.expanded + largeText.attributionBand <= 960f)

        assertEquals(landscape.collapsed, landscape.heightFor(SheetLevel.Collapsed))
        assertEquals(landscape.partial, landscape.heightFor(SheetLevel.Partial))
        assertEquals(landscape.expanded, landscape.heightFor(SheetLevel.Expanded))
    }

    @Test
    fun locationStatesNeverTreatPermissionAsFreshPosition() {
        assertTrue(geoStatusMessage(GeoStatus.PermissionDenied).contains("ввести початкову адресу вручну"))
        assertTrue(geoStatusMessage(GeoStatus.ServiceDisabled).contains("ввести адресу вручну"))
        assertTrue(geoStatusMessage(GeoStatus.Acquiring).contains("не вважається поточною"))
        assertTrue(geoStatusMessage(GeoStatus.StaleFix).contains("підтвердьте початкову адресу вручну"))
        assertTrue(geoStatusMessage(GeoStatus.FreshFix).contains("позицію отримано"))
    }
}