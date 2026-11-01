package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StopPlacesTest {
    @Test
    fun mapSheetLeavesReadableExpandedScrollViewport() {
        val density = 3.8f
        val height = 182f * density
        val external = calculateMapSheetAnchors(height, density, 1f)
        val inline = calculateMapSheetAnchors(height, density, 1f, inlineAttribution = true)
        assertEquals(0f, inline.attributionBand)
        assertTrue(inline.expanded > external.expanded)
        assertTrue(inline.expanded <= height)
        assertTrue(inline.collapsed < inline.partial && inline.partial < inline.expanded)
        val pinnedHeight = (44f + 7f + 52f + 12f) * density
        assertTrue((inline.expanded - pinnedHeight) / density >= 48f)
    }

    @Test
    fun catalogDoesNotExposeSyntheticPlaces() {
        assertTrue(stopPlaces.isEmpty())
        assertTrue(StopPlacesState().results.isEmpty())
        StopPlaceCategory.entries.forEach { category ->
            val filtered = StopPlacesBloc.reduce(
                StopPlacesState(),
                StopPlacesEvent.Category(category)
            )
            assertEquals(category, filtered.category)
            assertTrue(filtered.results.isEmpty())
        }
    }

    @Test
    fun searchHasNoResultsWithoutAConnectedCatalog() {
        val searched = StopPlacesBloc.reduce(
            StopPlacesState(),
            StopPlacesEvent.Query("Львів")
        )
        assertEquals("Львів", searched.query)
        assertTrue(searched.results.isEmpty())
    }

    @Test
    fun backReturnsToTheScreenThatOpenedCatalog() {
        val mapCatalog = StopPlacesBloc.transition(
            StopPlacesState(),
            StopPlacesEvent.Open(StopPlacesSource.Map)
        )
        assertEquals(StopPlacesDestination.Catalog, mapCatalog.destination)
        assertEquals(
            StopPlacesDestination.Map,
            StopPlacesBloc.transition(mapCatalog.catalog, StopPlacesEvent.Back).destination
        )

        val searchCatalog = StopPlacesBloc.transition(
            StopPlacesState(),
            StopPlacesEvent.Open(StopPlacesSource.StopSearch)
        )
        assertEquals(
            StopPlacesDestination.StopSearch,
            StopPlacesBloc.transition(searchCatalog.catalog, StopPlacesEvent.Back).destination
        )
    }

    @Test
    fun emptyCatalogDoesNotAffectAddressesOrRoutePreviewRestrictions() {
        val addresses = AddressSearchState("Origin", "Destination")
        assertEquals(AddressSearchState("Origin", "Destination"), addresses)
        assertNull(routeStopsPreviewBlockMessage(emptyList()))
        assertEquals(
            "Багатоточковий перегляд ще не підключено",
            routeStopsPreviewBlockMessage(listOf(RouteStop("stop-1", "Львів", "Львівська область")))
        )
    }
}
