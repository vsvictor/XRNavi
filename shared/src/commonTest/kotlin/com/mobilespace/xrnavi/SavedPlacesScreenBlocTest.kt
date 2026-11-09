package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SavedPlacesScreenBlocTest {
    @Test
    fun editingAndDeletingAreIndependentTransientActions() {
        val bloc = SavedPlacesScreenBloc()
        val place = demoPlaces.first().toSavedPlace()

        bloc.dispatch(SavedPlacesScreenEvent.Edit(place))
        bloc.dispatch(SavedPlacesScreenEvent.EditLabel("Home"))
        assertEquals(place, bloc.state.value.editingPlace)
        assertEquals("Home", bloc.state.value.editedLabel)

        bloc.dispatch(SavedPlacesScreenEvent.CancelEdit)
        assertNull(bloc.state.value.editingPlace)
        assertEquals("", bloc.state.value.editedLabel)

        bloc.dispatch(SavedPlacesScreenEvent.RequestDelete(place))
        assertEquals(place, bloc.state.value.deletingPlace)
        bloc.dispatch(SavedPlacesScreenEvent.CancelDelete)
        assertNull(bloc.state.value.deletingPlace)
    }

    @Test
    fun initialEditSelectionLoadsOnlyWhenNoMatchingEditIsActive() {
        val bloc = SavedPlacesScreenBloc()
        val place = demoPlaces.first().toSavedPlace()

        bloc.dispatch(SavedPlacesScreenEvent.SelectInitialEdit(place))
        assertEquals(place.label, bloc.state.value.editedLabel)
        bloc.dispatch(SavedPlacesScreenEvent.EditLabel("Custom"))
        bloc.dispatch(SavedPlacesScreenEvent.SelectInitialEdit(place))
        assertEquals("Custom", bloc.state.value.editedLabel)
    }
}
