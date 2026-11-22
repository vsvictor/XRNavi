package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals

class VehicleDraftBlocsTest {
    @Test
    fun reviewDraftIsRetained() {
        val bloc = VehicleReviewBloc()
        bloc.dispatch(VehicleReviewEvent.Height("4,0"))
        bloc.dispatch(VehicleReviewEvent.Length("16.5"))
        bloc.dispatch(VehicleReviewEvent.Mass("40"))
        assertEquals("4,0", bloc.state.value.height)
        assertEquals("16.5", bloc.state.value.length)
        assertEquals("40", bloc.state.value.mass)
    }

    @Test
    fun modelSelectionIsRetained() {
        val bloc = VehicleModelBloc()
        bloc.dispatch(VehicleModelEvent.Model("Focus"))
        bloc.dispatch(VehicleModelEvent.Year("2023"))
        bloc.dispatch(VehicleModelEvent.Trim("EcoBoost · 2.3"))
        assertEquals("Focus", bloc.state.value.model)
        assertEquals("2023", bloc.state.value.year)
        assertEquals("EcoBoost · 2.3", bloc.state.value.trim)
    }
}
