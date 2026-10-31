package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StopPlacesTest {
    @Test
    fun inlineMapAttributionLeavesReadableExpandedScrollViewport() {
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
    fun categoriesAndSearchUseOnlyLocalFixtures() {
        val initial = StopPlacesState()
        assertEquals(1, initial.results.size)
        val fuel = StopPlacesBloc.reduce(initial, StopPlacesEvent.Category(StopPlaceCategory.Fuel))
        assertEquals(stopPlaces[2], fuel.results.single())
        assertEquals(1, StopPlacesBloc.reduce(fuel, StopPlacesEvent.Query("Хрещатик")).results.size)
        assertTrue(
            StopPlacesBloc.reduce(
                fuel,
                StopPlacesEvent.Query("невідомий запит")
            ).results.isEmpty()
        )
        assertTrue(stopPlaces.all { it.address in demoPlaces })
    }

    @Test
    fun emptyCategoryAndUnknownObjectAreExplicit() {
        assertTrue(StopPlacesState(category = StopPlaceCategory.Border).results.isEmpty())
        val unknown = StopPlacesBloc.reduce(StopPlacesState(), StopPlacesEvent.Details("missing"))
        assertNull(unknown.selected)
        assertTrue(unknown.notice.isNotBlank())
    }

    @Test
    fun browsingAndReturningDoNotMutatePlan() {
        val plan = reduceRouteStops(
            addStopPlaceToDraft(
                reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing),
                stopPlaces[1]
            ),
            RouteStopsEvent.Save
        )
        val opened = StopPlacesBloc.transition(
            StopPlacesState(),
            plan,
            StopPlacesEvent.Open(StopPlacesSource.Map)
        )
        val details = StopPlacesBloc.transition(
            opened.catalog,
            opened.routeStops,
            StopPlacesEvent.Details(stopPlaces[0].id)
        )
        val list =
            StopPlacesBloc.transition(details.catalog, details.routeStops, StopPlacesEvent.Back)
        val returned =
            StopPlacesBloc.transition(list.catalog, list.routeStops, StopPlacesEvent.Back)
        assertEquals(stopPlaces[0], details.catalog.selected)
        assertNull(list.catalog.selected)
        assertEquals(StopPlacesDestination.Map, returned.destination)
        assertEquals(plan, returned.routeStops)
    }

    @Test
    fun addingCreatesOnlyDraftWithDistinctStableIds() {
        val editing = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        val first = addStopPlaceToDraft(editing, stopPlaces[0])
        val second = addStopPlaceToDraft(first, stopPlaces[1])
        assertTrue(second.applied.isEmpty())
        assertEquals(2, second.draft?.size)
        assertNotEquals(second.draft!![0].stopId, second.draft[1].stopId)
        assertEquals(stopPlaces[0].name, first.draft!!.single().placeName)
        assertEquals(stopPlaces[0].address.title, first.draft.single().address)
    }

    @Test
    fun replacementKeepsTargetIdAndDoesNotTouchNeighbour() {
        val editing = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        val first = addStopPlaceToDraft(editing, stopPlaces[0])
        val second = addStopPlaceToDraft(first, stopPlaces[1])
        val id = second.draft!![0].stopId
        val targeted = reduceRouteStops(second, RouteStopsEvent.ReplaceAddress(id))
        val replaced = addStopPlaceToDraft(targeted, stopPlaces[2])
        assertEquals(id, replaced.draft!![0].stopId)
        assertEquals(second.draft[1], replaced.draft[1])
        assertEquals(stopPlaces[2].address.title, replaced.draft[0].address)
    }

    @Test
    fun deletedSearchTargetCannotApplyToAnotherStop() {
        val editing = reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing)
        val first = addStopPlaceToDraft(editing, stopPlaces[0])
        val id = first.draft!!.single().stopId
        val targeted = reduceRouteStops(first, RouteStopsEvent.ReplaceAddress(id))
        val deleted = reduceRouteStops(targeted, RouteStopsEvent.RemoveStop(id))
        val stale = reduceRouteStops(
            deleted, RouteStopsEvent.SelectSavedPlace(
                id,
                SavedPlace(
                    stopPlaces[1].address.title,
                    stopPlaces[1].address.detail,
                    stopPlaces[1].name
                )
            )
        )
        assertEquals(deleted, stale)
    }

    @Test
    fun cancelDiscardsOnlyDraftAndSaveRequiresExplicitEvent() {
        val first = addStopPlaceToDraft(
            reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing), stopPlaces[0]
        )
        val saved = reduceRouteStops(first, RouteStopsEvent.Save)
        val changed = addStopPlaceToDraft(
            reduceRouteStops(saved, RouteStopsEvent.BeginEditing), stopPlaces[1]
        )
        assertEquals(saved.applied, changed.applied)
        assertEquals(
            saved.applied,
            reduceRouteStops(changed, RouteStopsEvent.CancelEditing).applied
        )
        assertEquals(2, reduceRouteStops(changed, RouteStopsEvent.Save).applied.size)
    }

    @Test
    fun globalAddressesAndRoutingRemainIndependent() {
        val addresses = AddressSearchState("Origin", "Destination")
        val draft = addStopPlaceToDraft(
            reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing), stopPlaces[0]
        )
        assertEquals(AddressSearchState("Origin", "Destination"), addresses)
        assertNull(routeStopsPreviewBlockMessage(draft.applied))
        assertEquals(
            "Багатоточковий перегляд ще не підключено",
            routeStopsPreviewBlockMessage(reduceRouteStops(draft, RouteStopsEvent.Save).applied)
        )
    }

    @Test
    fun mapAdditionStartsDraftAndCancelLeavesAppliedPlan() {
        val plan = RouteStopsState()
        val opened = StopPlacesBloc.transition(
            StopPlacesState(),
            plan,
            StopPlacesEvent.Open(StopPlacesSource.Map)
        )
        val details = StopPlacesBloc.transition(
            opened.catalog,
            plan,
            StopPlacesEvent.Details(stopPlaces[0].id)
        )
        val added = StopPlacesBloc.transition(details.catalog, plan, StopPlacesEvent.AddSelected)
        assertTrue(added.beganEditing)
        assertEquals(StopPlacesDestination.Editor, added.destination)
        assertEquals(plan.applied, added.routeStops.applied)
        assertEquals(stopPlaces[0].address.title, added.routeStops.draft!!.single().address)
        assertEquals(
            plan.applied,
            reduceRouteStops(added.routeStops, RouteStopsEvent.CancelEditing).applied
        )
    }

    @Test
    fun catalogReplacementRetainsTargetAndRejectsDeletedOrChangedTarget() {
        val draft = addStopPlaceToDraft(
            addStopPlaceToDraft(
                reduceRouteStops(RouteStopsState(), RouteStopsEvent.BeginEditing),
                stopPlaces[0]
            ),
            stopPlaces[1]
        )
        val targetId = draft.draft!![0].stopId
        val search = reduceRouteStops(draft, RouteStopsEvent.ReplaceAddress(targetId))
        val opened = StopPlacesBloc.transition(
            StopPlacesState(),
            search,
            StopPlacesEvent.Open(StopPlacesSource.StopSearch)
        )
        val selected =
            StopPlacesBloc.reduce(opened.catalog, StopPlacesEvent.Details(stopPlaces[2].id))
        val replaced = StopPlacesBloc.transition(selected, search, StopPlacesEvent.AddSelected)
        assertEquals(targetId, replaced.routeStops.draft!![0].stopId)
        assertEquals(stopPlaces[2].address.title, replaced.routeStops.draft[0].address)
        assertEquals(draft.draft[1], replaced.routeStops.draft[1])
        val deleted = reduceRouteStops(search, RouteStopsEvent.RemoveStop(targetId))
        val rejected = StopPlacesBloc.transition(selected, deleted, StopPlacesEvent.AddSelected)
        assertEquals(deleted, rejected.routeStops)
        assertNull(rejected.destination)
        assertEquals("Ціль зупинки більше недоступна", rejected.catalog.notice)
        val other = reduceRouteStops(search, RouteStopsEvent.ReplaceAddress(draft.draft[1].stopId))
        assertEquals(
            other,
            StopPlacesBloc.transition(selected, other, StopPlacesEvent.AddSelected).routeStops
        )
    }

    @Test
    fun unknownSelectionCannotOpenEditorOrAllocateStop() {
        val state = StopPlacesState(selectedId = "unknown")
        val plan = RouteStopsState()
        val rejected = StopPlacesBloc.transition(state, plan, StopPlacesEvent.AddSelected)
        assertEquals(plan, rejected.routeStops)
        assertNull(rejected.destination)
        assertEquals("Об’єкт відсутній у демокаталозі", rejected.catalog.notice)
    }
}
