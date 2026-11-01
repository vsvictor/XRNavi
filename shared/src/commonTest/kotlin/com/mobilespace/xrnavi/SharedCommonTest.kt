package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedCommonTest {

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
    fun openingOriginOverridesTheDefaultDestinationAndSelectionUsesTheActiveTarget() {
        val initial = AddressSearchState()
        val originSearch = initial.open(SearchTarget.Origin)
        assertEquals(SearchTarget.Origin, originSearch.target)
        val selected = originSearch.select(testPlaces.first())
        assertEquals(testPlaces.first().title, selected.origin)
        assertEquals("", selected.destination)

        val destinationSearch = selected.open(SearchTarget.Destination)
        val complete = destinationSearch.select(testPlaces.last())
        assertEquals(selected.origin, complete.origin)
        assertEquals(testPlaces.last().title, complete.destination)
    }

    @Test
    fun switchingAndCancellingSearchPreserveBothConfirmedAddresses() {
        val confirmed = AddressSearchState("Львів", "Київ")
        val editing = confirmed.open(SearchTarget.Origin).copy(query = "незбережений запит")
        val switched = editing.activate(SearchTarget.Destination)
        assertEquals("", switched.query)
        assertEquals(confirmed.origin, switched.origin)
        assertEquals(confirmed.destination, switched.destination)
        val cancelled = switched.copy(query = "інший запит").cancel()
        assertEquals("", cancelled.query)
        assertEquals(confirmed.origin, cancelled.origin)
        assertEquals(confirmed.destination, cancelled.destination)
        val selected = switched.select(testPlaces.last())
        assertEquals(confirmed.origin, selected.origin)
        assertEquals(testPlaces.last().title, selected.destination)
    }

    @Test
    fun restoredSearchRetainsTargetQueryAndConfirmedAddresses() {
        val restored = AddressSearchState.Saver.restore(
            listOf("Львів", "Київ", SearchTarget.Origin.name, "Ринок")
        )!!
        assertEquals(SearchTarget.Origin, restored.target)
        assertEquals("Ринок", restored.query)
        assertEquals("Київ", restored.select(testPlaces.first()).destination)
        assertEquals("Львів", restored.cancel().origin)
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
        assertTrue(!GeoStatus.PermissionDenied.isFreshPosition)
        assertTrue(!GeoStatus.ServiceDisabled.isFreshPosition)
        assertTrue(!GeoStatus.Acquiring.isFreshPosition)
        assertTrue(GeoStatus.FreshFix.isFreshPosition)
        assertTrue(!GeoStatus.StaleFix.isFreshPosition)
    }
}