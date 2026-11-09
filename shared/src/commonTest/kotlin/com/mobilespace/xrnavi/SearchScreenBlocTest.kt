package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchScreenBlocTest {
    @Test
    fun savingAndDuplicateAddressActionsUpdateSearchFeedback() {
        val bloc = SearchScreenBloc()
        val place = demoPlaces.first()

        bloc.dispatch(SearchScreenEvent.PlaceSaved)
        assertEquals("Адресу додано до локального обраного.", bloc.state.value.saveNotice)
        assertNull(bloc.state.value.duplicatePlace)

        bloc.dispatch(SearchScreenEvent.DuplicatePlace(place))
        assertEquals(place.toSavedPlace(), bloc.state.value.duplicatePlace)
        assertEquals("", bloc.state.value.saveNotice)

        bloc.dispatch(SearchScreenEvent.DismissDuplicate)
        assertNull(bloc.state.value.duplicatePlace)
    }

    @Test
    fun leavingSearchClearsTransientFeedback() {
        val bloc = SearchScreenBloc()
        bloc.dispatch(SearchScreenEvent.InvalidPlace)
        bloc.dispatch(SearchScreenEvent.Reset)
        assertEquals(SearchScreenState(), bloc.state.value)
    }
}
