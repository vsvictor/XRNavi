package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RouteBlocStatesTest {
    private val here = RoutePlace("Моє місцезнаходження", "Надійна позиція", GeoPoint(50.45, 30.52))

    @Test
    fun searchPhasesDistinguishEmptyFromError() = runTest {
        val service = FakeRoutePlanningService()
        val bloc = RouteBloc(service, this)
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Destination))
        assertEquals(SearchPhase.Idle, bloc.state.value.searchPhase)
        service.places = emptyList()
        bloc.dispatch(RouteEvent.QueryChanged("Нікуди", null))
        assertEquals(SearchPhase.Loading, bloc.state.value.searchPhase)
        advanceUntilIdle()
        assertEquals(SearchPhase.Empty, bloc.state.value.searchPhase)
        assertTrue(bloc.state.value.searchError.isEmpty())
        service.failure = RoutePlanningException(RoutePlanningException.Kind.Network)
        bloc.dispatch(RouteEvent.RetrySearch)
        advanceUntilIdle()
        assertEquals(SearchPhase.Error, bloc.state.value.searchPhase)
        assertEquals("Нікуди", bloc.state.value.query)
        assertEquals(RouteField.Destination, bloc.state.value.editing)
        service.failure = null
        service.places = listOf(here)
        bloc.dispatch(RouteEvent.RetrySearch)
        advanceUntilIdle()
        assertEquals(SearchPhase.Results, bloc.state.value.searchPhase)
    }

    @Test
    fun backClosesSearchThenStepsSheetDownThenYields() = runTest {
        val bloc = RouteBloc(FakeRoutePlanningService(), this)
        bloc.dispatch(RouteEvent.SetSheet(SheetLevel.Expanded))
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Origin))
        assertTrue(bloc.handleBack())
        assertEquals(null, bloc.state.value.editing)
        assertEquals(SheetLevel.Expanded, bloc.state.value.sheet)
        assertTrue(bloc.handleBack())
        assertEquals(SheetLevel.Partial, bloc.state.value.sheet)
        assertTrue(bloc.handleBack())
        assertEquals(SheetLevel.Collapsed, bloc.state.value.sheet)
        assertFalse(bloc.handleBack())
    }

    @Test
    fun oldRouteIsStaleWhileReplanningAndAfterFailure() = runTest {
        val service = FakeRoutePlanningService()
        val bloc = RouteBloc(service, this)
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Origin))
        bloc.dispatch(RouteEvent.SelectPlace(here))
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Destination))
        bloc.dispatch(RouteEvent.SelectPlace(service.places.first()))
        advanceUntilIdle()
        assertFalse(bloc.state.value.routeStale)
        service.failure = RoutePlanningException(RoutePlanningException.Kind.Network)
        bloc.dispatch(RouteEvent.Swap)
        advanceUntilIdle()
        assertTrue(bloc.state.value.routeStale)
        service.failure = null
        bloc.dispatch(RouteEvent.Retry)
        advanceUntilIdle()
        assertFalse(bloc.state.value.routeStale)
    }

    @Test
    fun startNeedsReliableFixWhenOriginIsCurrentLocation() = runTest {
        val service = FakeRoutePlanningService()
        val bloc = RouteBloc(service, this)
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Origin))
        bloc.dispatch(RouteEvent.UseCurrentLocation(here))
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Destination))
        bloc.dispatch(RouteEvent.SelectPlace(service.places.first()))
        advanceUntilIdle()
        bloc.dispatch(RouteEvent.StartRequested(reliableOrigin = false))
        assertTrue(bloc.state.value.notice.contains("надійної позиції"))
        bloc.dispatch(RouteEvent.DismissNotice)
        bloc.dispatch(RouteEvent.StartRequested(reliableOrigin = true))
        assertFalse(bloc.state.value.notice.contains("надійної позиції"))
    }
}
