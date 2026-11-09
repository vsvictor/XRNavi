package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DemoAccountBlocsTest {
    @Test
    fun accountDialogsOpenDismissAndReset() {
        val bloc = DemoAccountBloc()
        bloc.dispatch(DemoAccountEvent.ViewVehicle("v1"))
        bloc.dispatch(DemoAccountEvent.Unavailable("Недоступно"))
        assertEquals("v1", bloc.state.value.viewedVehicleId)
        assertEquals("Недоступно", bloc.state.value.notice)
        bloc.dispatch(DemoAccountEvent.ShowThemePicker)
        assertTrue(bloc.state.value.themePickerVisible)
        bloc.dispatch(DemoAccountEvent.DismissDialog)
        assertNull(bloc.state.value.notice)
        assertFalse(bloc.state.value.themePickerVisible)
        assertEquals("v1", bloc.state.value.viewedVehicleId)
        bloc.dispatch(DemoAccountEvent.Left)
        assertNull(bloc.state.value.viewedVehicleId)
    }

    @Test
    fun garageFilterChanges() {
        val bloc = DemoGarageBloc()
        assertEquals(GarageFilter.All, bloc.state.value.filter)
        bloc.dispatch(DemoGarageEvent.Filter(GarageFilter.Cargo))
        assertEquals(GarageFilter.Cargo, bloc.state.value.filter)
    }

    @Test
    fun configurationDraftSyncsAndCancels() {
        val bloc = DemoVehicleConfigurationsBloc()
        bloc.dispatch(DemoVehicleConfigurationsEvent.Sync("v1", "a"))
        assertEquals("a", bloc.state.value.draft.selectedConfigurationId)
        val other = DemoVehicleConfiguration(
            "b", "v1", "B", "", VehicleConfigurationAvailability.Available, true
        )
        bloc.dispatch(DemoVehicleConfigurationsEvent.Select(other))
        assertEquals("b", bloc.state.value.draft.selectedConfigurationId)
        bloc.dispatch(DemoVehicleConfigurationsEvent.Sync("v1", "a"))
        assertEquals("b", bloc.state.value.draft.selectedConfigurationId)
        bloc.dispatch(DemoVehicleConfigurationsEvent.Cancel)
        assertEquals("a", bloc.state.value.draft.selectedConfigurationId)
        bloc.dispatch(DemoVehicleConfigurationsEvent.Sync("v2", "c"))
        assertEquals("c", bloc.state.value.draft.selectedConfigurationId)
    }
}
