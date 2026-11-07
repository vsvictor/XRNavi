package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RouteBlocTest {
    private val kyiv = RoutePlace("Київ", "Київ, Україна", GeoPoint(50.45, 30.52))

    @Test
    fun selectingBothPlacesBuildsRoute() = runTest {
        val service = FakeRoutePlanningService()
        val bloc = RouteBloc(service, this)
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Origin))
        bloc.dispatch(RouteEvent.SelectPlace(kyiv))
        assertEquals(RouteStatus.Idle, bloc.state.value.status)
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Destination))
        bloc.dispatch(RouteEvent.QueryChanged("Льв", null))
        advanceUntilIdle()
        val suggestion = bloc.state.value.suggestions.single()
        bloc.dispatch(RouteEvent.SelectPlace(suggestion))
        advanceUntilIdle()
        assertIs<RouteStatus.Ready>(bloc.state.value.status)
        assertEquals(null, bloc.state.value.editing)
    }

    @Test
    fun swapExchangesPlacesAndReplansRoute() = runTest {
        val service = FakeRoutePlanningService()
        val bloc = RouteBloc(service, this)
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Origin))
        bloc.dispatch(RouteEvent.SelectPlace(kyiv))
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Destination))
        bloc.dispatch(RouteEvent.SelectPlace(service.places.first()))
        advanceUntilIdle()
        bloc.dispatch(RouteEvent.Swap)
        advanceUntilIdle()
        assertEquals("Львів", bloc.state.value.origin?.name)
        assertEquals("Київ", bloc.state.value.destination?.name)
        assertEquals(2, service.routeCalls)
    }

    @Test
    fun failuresAreReportedWithoutCrashingAndShortQueriesAreIgnored() = runTest {
        val service = FakeRoutePlanningService()
        service.failure = RoutePlanningException(RoutePlanningException.Kind.Network)
        val bloc = RouteBloc(service, this)
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Origin))
        bloc.dispatch(RouteEvent.QueryChanged("К", null))
        advanceUntilIdle()
        assertTrue(bloc.state.value.searchError.isEmpty())
        bloc.dispatch(RouteEvent.QueryChanged("Київ", null))
        advanceUntilIdle()
        assertTrue(bloc.state.value.searchError.isNotEmpty())
        bloc.dispatch(RouteEvent.SelectPlace(kyiv))
        bloc.dispatch(RouteEvent.OpenSearch(RouteField.Destination))
        bloc.dispatch(RouteEvent.SelectPlace(kyiv))
        advanceUntilIdle()
        assertIs<RouteStatus.Failed>(bloc.state.value.status)
    }

    @Test
    fun startNeverClaimsNavigationIsAvailable() = runTest {
        val bloc = RouteBloc(FakeRoutePlanningService(), this)
        bloc.dispatch(RouteEvent.StartRequested())
        assertTrue(bloc.state.value.notice.contains("Спершу"))
    }
}
