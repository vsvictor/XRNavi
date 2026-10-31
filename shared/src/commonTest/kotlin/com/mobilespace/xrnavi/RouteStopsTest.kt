package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RouteStopsTest {
    @Test
    fun draftSupportsAddReplaceReorderSaveAndCancel() {
        var state = RouteStopsState()
        state = reduceRouteStops(state, RouteStopsEvent.BeginEditing)

        state = addDemoStop(state, demoPlaces[0])
        val firstStopId = state.draft!!.single().stopId
        state = addDemoStop(state, demoPlaces[1])
        val secondStopId = state.draft!!.last().stopId

        assertNotEquals(firstStopId, secondStopId)
        state = reduceRouteStops(state, RouteStopsEvent.MoveUp(secondStopId))
        assertEquals(listOf(secondStopId, firstStopId), state.draft!!.map { it.stopId })
        assertEquals("stop-1", state.draft.last().stopId)

        state = reduceRouteStops(state, RouteStopsEvent.ReplaceAddress(firstStopId))
        state = reduceRouteStops(state, RouteStopsEvent.SelectDemoPlace(firstStopId, demoPlaces[3]))
        assertEquals(demoPlaces[3].title, state.draft!!.last().address)

        val cancelled = reduceRouteStops(state, RouteStopsEvent.CancelEditing)
        assertTrue(cancelled.applied.isEmpty())
        assertNull(cancelled.draft)

        var saved = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        saved = addDemoStop(saved, demoPlaces[0])
        saved = reduceRouteStops(saved, RouteStopsEvent.Save)
        assertEquals(demoPlaces[0].title, saved.applied.single().address)
        assertNull(saved.draft)
    }

    @Test
    fun searchRemainsBoundToStableStopIdAfterReordering() {
        var state = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        state = addDemoStop(state, demoPlaces[0])
        val firstId = state.draft!!.single().stopId
        state = addDemoStop(state, demoPlaces[1])
        val secondId = state.draft!!.last().stopId

        state = reduceRouteStops(state, RouteStopsEvent.ReplaceAddress(firstId))
        state = reduceRouteStops(state, RouteStopsEvent.MoveDown(firstId))
        state = reduceRouteStops(state, RouteStopsEvent.SelectDemoPlace(firstId, demoPlaces[3]))

        assertEquals(listOf(secondId, firstId), state.draft.orEmpty().map { it.stopId })
        assertEquals(demoPlaces[3].title, state.draft.orEmpty().last().address)
        assertEquals(demoPlaces[1].title, state.draft.orEmpty().first().address)
    }

    @Test
    fun deletingSearchTargetDiscardsStaleResultAndCancelDoesNotAddEmptyStop() {
        var state = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        state = addDemoStop(state, demoPlaces[0])
        val targetId = state.draft!!.single().stopId
        state = reduceRouteStops(state, RouteStopsEvent.ReplaceAddress(targetId))
        state = reduceRouteStops(state, RouteStopsEvent.RemoveStop(targetId))
        val afterDeletion = reduceRouteStops(
            state,
            RouteStopsEvent.SelectDemoPlace(targetId, demoPlaces[1])
        )
        assertTrue(afterDeletion.draft!!.isEmpty())
        assertNull(afterDeletion.searchStopId)

        var adding = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        adding = reduceRouteStops(adding, RouteStopsEvent.AddStop)
        assertTrue(adding.draft!!.isEmpty())
        adding = reduceRouteStops(adding, RouteStopsEvent.CancelSearch)
        assertTrue(adding.draft!!.isEmpty())
        assertNull(adding.searchStopId)
    }

    @Test
    fun stopAddressIsCopiedAndDoesNotDependOnSavedPlaceChanges() {
        val savedPlace = demoPlaces[0].toSavedPlace().copy(label = "Мій офіс")
        var state = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        state = reduceRouteStops(state, RouteStopsEvent.AddStop)
        val targetId = state.searchStopId!!
        state = reduceRouteStops(state, RouteStopsEvent.SelectSavedPlace(targetId, savedPlace))
        val confirmedStop = state.draft!!.single()

        val renamed = renameSavedPlace(listOf(savedPlace), savedPlace.key, "Нове ім’я")!!.single()
        val deleted = removeSavedPlace(listOf(renamed), renamed.key)

        assertEquals(demoPlaces[0].title, confirmedStop.address)
        assertEquals(demoPlaces[0].detail, confirmedStop.detail)
        assertEquals("Мій офіс", confirmedStop.placeName)
        assertTrue(deleted.isEmpty())
        assertEquals(confirmedStop, state.draft.orEmpty().single())
    }

    @Test
    fun previewInvalidatesOnlyForChangedAppliedStopsAndIsBlockedForAnyStop() {
        val oneStop = RouteStop("stop-1", demoPlaces[0].title, demoPlaces[0].detail)
        assertFalse(routeStopsChanged(listOf(oneStop), listOf(oneStop)))
        assertTrue(routeStopsChanged(listOf(oneStop), emptyList()))
        assertNull(routeStopsPreviewBlockMessage(emptyList()))
        assertEquals(
            "Багатоточковий перегляд ще не підключено",
            routeStopsPreviewBlockMessage(listOf(oneStop))
        )
    }

    @Test
    fun appliedStopPersistenceRoundTripsIdsAddressesAndOrdinal() {
        val stops = listOf(
            RouteStop("stop-1", demoPlaces[0].title, demoPlaces[0].detail, "Офіс"),
            RouteStop("stop-3", demoPlaces[0].title, demoPlaces[0].detail)
        )
        val snapshot = RouteStopsSnapshot(stops, nextStopOrdinal = 4)

        assertEquals(snapshot, decodeRouteStopsSnapshot(encodeRouteStopsSnapshot(snapshot)))
        assertEquals(
            listOf(RouteStop("stop-1", "A", "B")),
            (decodeRouteStops("1:6:stop-11:A1:B") as RouteStopsDecodeResult.Success).stops
        )
        assertNull(decodeRouteStopsSnapshot("broken"))
        assertTrue(reduceRouteStops(RouteStopsState(nextStopOrdinal = 4), RouteStopsEvent.BeginEditing)
            .let { reduceRouteStops(it, RouteStopsEvent.AddStop) }
            .searchStopId == "stop-4")
    }

    @Test
    fun stopPlanningEventsDoNotMutateConfirmedGlobalAddresses() {
        val addresses = AddressSearchState(
            origin = demoPlaces[0].title,
            destination = demoPlaces[1].title,
            target = SearchTarget.Destination,
            query = "global query"
        )
        var state = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        state = addDemoStop(state, demoPlaces[3])
        state = reduceRouteStops(state, RouteStopsEvent.Save)

        assertEquals(demoPlaces[0].title, addresses.origin)
        assertEquals(demoPlaces[1].title, addresses.destination)
        assertEquals("global query", addresses.query)
        assertEquals(demoPlaces[3].title, state.applied.single().address)
    }

    private fun addDemoStop(state: RouteStopsState, place: DemoPlace): RouteStopsState {
        val adding = reduceRouteStops(state, RouteStopsEvent.AddStop)
        val stopId = adding.searchStopId ?: error("AddStop must create a search target")
        return reduceRouteStops(adding, RouteStopsEvent.SelectDemoPlace(stopId, place))
    }
}
