package com.mobilespace.xrnavi

import com.mobilespace.xrnavi.data.routestops.LocalRouteStopsRepository
import com.mobilespace.xrnavi.domain.routestops.LoadRouteStops
import com.mobilespace.xrnavi.domain.routestops.PersistRouteStops
import com.mobilespace.xrnavi.presentation.routestops.RouteStopsBloc
import com.mobilespace.xrnavi.presentation.routestops.RouteStopsEffect
import com.mobilespace.xrnavi.presentation.routestops.RouteStopsNotice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RouteStopsBlocTest {
    @Test
    fun corruptStoredRouteIsNotOverwritten() {
        val store = MemoryRouteStopsStore("corrupt")
        val bloc = createBloc(store)

        assertEquals(RouteStopsNotice.DataUnreadable, bloc.state.value.notice)
        assertFalse(bloc.state.value.canSave)
        bloc.dispatch(RouteStopsEvent.BeginEditing)
        bloc.dispatch(RouteStopsEvent.AddStop)
        assertNull(bloc.dispatch(RouteStopsEvent.Save))

        assertEquals(RouteStopsNotice.SaveUnavailable, bloc.state.value.notice)
        assertEquals("corrupt", store.value)
    }

    @Test
    fun failedSaveKeepsDraftAndSuccessfulSavePersistsSnapshot() {
        val store = MemoryRouteStopsStore(null)
        val bloc = createBloc(store)
        bloc.dispatch(RouteStopsEvent.BeginEditing)
        bloc.dispatch(RouteStopsEvent.AddStop)
        val stopId = bloc.state.value.route.searchStopId!!
        bloc.dispatch(
            RouteStopsEvent.SelectSavedPlace(stopId, testPlaces.first().toSavedPlace())
        )
        store.writable = false

        assertNull(bloc.dispatch(RouteStopsEvent.Save))
        assertEquals(RouteStopsNotice.SaveFailed, bloc.state.value.notice)
        assertEquals(1, bloc.state.value.route.draft?.size)
        assertTrue(bloc.state.value.route.applied.isEmpty())

        store.writable = true
        val effect = bloc.dispatch(RouteStopsEvent.Save)
        assertEquals(RouteStopsEffect.Saved(changed = true), effect)
        assertNull(bloc.state.value.route.draft)
        assertEquals(testPlaces.first().title, bloc.state.value.route.applied.single().address)
        assertEquals(
            RouteStopsSnapshot(bloc.state.value.route.applied, nextStopOrdinal = 2),
            decodeRouteStopsSnapshot(store.value)
        )
    }

    @Test
    fun savingUnchangedRouteReturnsToPreviousScreenWithoutInvalidation() {
        val initial = RouteStopsSnapshot(
            listOf(
                RouteStop(
                    stopId = "stop-1",
                    address = testPlaces.first().title,
                    detail = testPlaces.first().detail
                )
            ),
            nextStopOrdinal = 2
        )
        val store = MemoryRouteStopsStore(encodeRouteStopsSnapshot(initial))
        val bloc = createBloc(store)
        bloc.dispatch(RouteStopsEvent.BeginEditing)

        assertEquals(RouteStopsEffect.Saved(changed = false), bloc.dispatch(RouteStopsEvent.Save))
        assertEquals(RouteStopsNotice.MultiStopPreviewUnavailable, bloc.state.value.notice)
    }

    @Test
    fun restoredEditingDraftIsUsedWhenTheViewModelIsRecreated() {
        val persisted = RouteStopsSnapshot(
            listOf(
                RouteStop("stop-1", testPlaces.first().title, testPlaces.first().detail)
            ),
            nextStopOrdinal = 2
        )
        val store = MemoryRouteStopsStore(encodeRouteStopsSnapshot(persisted))
        val restored = RouteStopsState(
            applied = persisted.stops,
            draft = persisted.stops + RouteStop(
                "stop-2",
                testPlaces.last().title,
                testPlaces.last().detail
            ),
            nextStopOrdinal = 3,
            query = "search"
        )
        val bloc = createBloc(store, restored)

        assertEquals(restored, bloc.state.value.route)
        assertTrue(bloc.state.value.canSave)
    }

    private fun createBloc(
        store: MemoryRouteStopsStore,
        restoredRoute: RouteStopsState? = null
    ): RouteStopsBloc {
        val repository = LocalRouteStopsRepository(store)
        return RouteStopsBloc(
            LoadRouteStops(repository),
            PersistRouteStops(repository),
            restoredRoute
        )
    }

    private class MemoryRouteStopsStore(var value: String?) : RouteStopsStore {
        var writable = true

        override fun read(): String? = value

        override fun write(value: String): Boolean {
            if (!writable) return false
            this.value = value
            return true
        }
    }
}
