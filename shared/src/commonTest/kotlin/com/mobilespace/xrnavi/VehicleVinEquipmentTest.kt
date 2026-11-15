package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleVinEquipmentTest {
    private fun raw(value: String) = Json.parseToJsonElement(value).jsonObject
    private val trim =
        VehicleEquipmentTrim("8873", "LE Sedan", raw("""{"trim":"LE","description":"LE Sedan"}"""))

    private fun start(
        service: FakeGarageService,
        scope: kotlinx.coroutines.CoroutineScope
    ): AddVehicleBloc {
        val bloc = AddVehicleBloc(service, ::testSession, scope)
        bloc.dispatch(AddVehicleEvent.Open)
        return bloc
    }

    @Test
    fun completeVinPopulatesEquipmentWithoutRedundantTechnicalRequestsOrLegacyIds() = runTest {
        val service = FakeGarageService().apply {
            equipmentTrimsResult = listOf(trim)
            decodedVin = DecodedVehicleVin(
                "Toyota", "Focus", 2021, raw(
                    """{"trim":"LE","trims":[{"id":123,"name":"LE","description":"Sedan","make_model_trim_body":{"id":123,"type":"Sedan","height":58.2,"width":72.9,"length":191.7,"curb_weight":3460},"make_model_trim_engine":{"engine_type":"gas","fuel_type":"Gasoline","size":2.5,"horsepower_hp":200,"transmission":"Automatic","drive_type":"Front wheel drive"},"make_model_trim_mileage":{"combined_mpg":30,"epa_city_mpg":25,"epa_highway_mpg":35,"fuel_tank_capacity":15}}]}""",
                )
            )
        }
        val bloc = start(service, backgroundScope)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        val state = bloc.state.value
        assertEquals("8873", state.equipment.selectedTrim?.id)
        assertEquals(AddVehicleTab.General, state.selectedTab)
        assertEquals("Sedan", state.equipment.bodies.single()["type"]?.jsonPrimitive?.content)
        assertEquals(
            "Automatic",
            state.equipment.engines.single()["transmission"]?.jsonPrimitive?.content
        )
        assertEquals(
            "30",
            state.equipment.mileages.single()["combined_mpg"]?.jsonPrimitive?.content
        )
        assertNull(service.requestedBodyTrim)
        assertNull(service.requestedEngineTrim)
        assertNull(service.requestedMileageTrim)
        assertTrue(state.canSubmit)
        assertFalse(state.equipment.bodies.single().containsKey("id"))
    }

    @Test
    fun partialVinFetchesMissingFieldsAndPreservesKnownVinData() = runTest {
        val service = FakeGarageService().apply {
            equipmentTrimsResult = listOf(trim)
            bodiesResult =
                raw("""{"type":"Sedan","length":191.7,"curb_weight":3000}""").let(::listOf)
            enginesResult =
                raw("""{"size":2.5,"horsepower_hp":200,"transmission":"Automatic"}""").let(::listOf)
            decodedVin = DecodedVehicleVin(
                "Toyota", "Focus", 2021, raw(
                    """{"trim":"LE","specs":{"curb_weight_pounds":"3460","displacement_l":"2.5"},"trims":[]}""",
                )
            )
        }
        val bloc = start(service, backgroundScope)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        assertEquals("8873", service.requestedBodyTrim)
        assertEquals("8873", service.requestedEngineTrim)
        assertEquals("8873", service.requestedMileageTrim)
        assertEquals(
            "3460",
            bloc.state.value.equipment.bodies.single()["curb_weight"]?.jsonPrimitive?.content
        )
        assertEquals(
            "191.7",
            bloc.state.value.equipment.bodies.single()["length"]?.jsonPrimitive?.content
        )
        assertTrue(bloc.state.value.canSubmit)
    }

    @Test
    fun ambiguousVinNeverChoosesArbitraryTrimAndExplainsManualSelection() = runTest {
        val service = FakeGarageService().apply {
            equipmentTrimsResult = listOf(trim, trim.copy(id = "another"))
            decodedVin =
                DecodedVehicleVin("Toyota", "Focus", 2021, raw("""{"trim":"LE","trims":[]}"""))
        }
        val bloc = start(service, backgroundScope)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        assertNull(bloc.state.value.equipment.selectedTrim)
        assertTrue(bloc.state.value.equipment.vinNotice.isNotEmpty())
        assertFalse(bloc.state.value.canSubmit)
        assertNull(service.requestedBodyTrim)
    }

    @Test
    fun failedVinTechnicalFetchKeepsRetryAndCannotSave() = runTest {
        val service = FakeGarageService().apply {
            equipmentTrimsResult = listOf(trim)
            failBodies = true
            decodedVin = DecodedVehicleVin("Toyota", "Focus", 2021, raw("""{"trim":"LE"}"""))
        }
        val bloc = start(service, backgroundScope)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        assertFalse(bloc.state.value.canSubmit)
        assertTrue(bloc.state.value.equipment.bodiesError.isNotEmpty())
        service.failBodies = false
        bloc.dispatch(AddVehicleEvent.RetryEquipmentBodies)
        runCurrent()
        assertTrue(bloc.state.value.canSubmit)
    }
}
