package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
    fun openingOriginOverridesTheDefaultDestinationAndSelectionUsesTheActiveTarget() {
        val initial = AddressSearchState()
        val originSearch = initial.open(SearchTarget.Origin)
        assertEquals(SearchTarget.Origin, originSearch.target)
        val selected = originSearch.select(demoPlaces.first())
        assertEquals(demoPlaces.first().title, selected.origin)
        assertEquals("", selected.destination)

        val destinationSearch = selected.open(SearchTarget.Destination)
        val complete = destinationSearch.select(demoPlaces.last())
        assertEquals(selected.origin, complete.origin)
        assertEquals(demoPlaces.last().title, complete.destination)
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
        val selected = switched.select(demoPlaces.last())
        assertEquals(confirmed.origin, selected.origin)
        assertEquals(demoPlaces.last().title, selected.destination)
    }

    @Test
    fun restoredSearchRetainsTargetQueryAndConfirmedAddresses() {
        val restored = AddressSearchState.Saver.restore(
            listOf("Львів", "Київ", SearchTarget.Origin.name, "Ринок")
        )!!
        assertEquals(SearchTarget.Origin, restored.target)
        assertEquals("Ринок", restored.query)
        assertEquals("Київ", restored.select(demoPlaces.first()).destination)
        assertEquals("Львів", restored.cancel().origin)
    }

    @Test
    fun categoriesDoNotSilentlySelectAnotherVehicle() {
        assertEquals(demoVehicle, demoVehicleForClass("Легкове"))
        assertEquals("Вантажне", demoVehicleForClass("Вантажне")?.category)
        assertEquals("Спецтехніка", demoVehicleForClass("Спецтехніка")?.category)
        assertNull(demoVehicleForClass("Невідома категорія"))
        assertEquals(demoVehicle, demoVehicleForClass("Легкове"))
        assertEquals("Легковий · 1,4 м · 1,8 т", demoVehicle.specification)
    }

    @Test
    fun routePreviewRequiresBothAddressesAndAnAvailableVehicleProfile() {
        assertTrue(
            routePreviewBlockMessage("", "площа Ринок, 1", "Легкове")
                ?.contains("початкову та кінцеву точки") == true
        )
        assertTrue(
            routePreviewBlockMessage("Хрещатик, 22", "", "Легкове")
                ?.contains("початкову та кінцеву точки") == true
        )
        assertTrue(
            routePreviewBlockMessage("Хрещатик, 22", "площа Ринок, 1", "Невідома категорія")
                ?.contains("не налаштовано") == true
        )
        assertEquals(
            null,
            routePreviewBlockMessage("Хрещатик, 22", "площа Ринок, 1", "Легкове")
        )
    }

    @Test
    fun availableVehicleClassesHaveProfilesAndUnknownClassesRemainUnavailable() {
        assertEquals(null, vehicleClassUnavailableMessage("Легкове"))
        assertEquals(null, vehicleClassUnavailableMessage("Вантажне"))
        assertEquals(null, vehicleClassUnavailableMessage("Спецтехніка"))
        assertTrue(vehicleClassUnavailableMessage("Невідома категорія")!!.contains("не налаштовано"))
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