package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddVehicleBlocTest {
    @Test
    fun wizardFlowAddsVehicle() = runTest {
        val bloc = AddVehicleBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectClass("c"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("m"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        assertEquals(AddVehicleStep.Details, bloc.state.value.step)
        assertFalse(bloc.state.value.canSubmit)
        bloc.dispatch(AddVehicleEvent.SelectYear(1999))
        assertNull(bloc.state.value.year)
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        bloc.dispatch(AddVehicleEvent.SelectTrim("tr"))
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        assertNotNull(bloc.state.value.added)
        assertFalse(bloc.state.value.open)
    }

    @Test
    fun backStepsThroughWizardAndCloses() = runTest {
        val bloc = AddVehicleBloc(FakeGarageService(), ::testSession, backgroundScope)
        assertFalse(bloc.handleBack())
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectClass("c"))
        runCurrent()
        assertTrue(bloc.handleBack())
        assertEquals(AddVehicleStep.Class, bloc.state.value.step)
        assertTrue(bloc.handleBack())
        assertFalse(bloc.state.value.open)
    }

    @Test
    fun catalogFailureShowsRetry() = runTest {
        val service = FakeGarageService().apply { fail = true }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        assertTrue(bloc.state.value.error.isNotEmpty())
        service.fail = false
        bloc.dispatch(AddVehicleEvent.Retry)
        runCurrent()
        assertEquals(1, bloc.state.value.classes.size)
    }
}
