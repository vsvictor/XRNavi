package com.mobilespace.xrnavi

import kotlinx.coroutines.CompletableDeferred
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
class GarageMutationsTest {
    private fun savedVehicle(): GarageVehicle {
        val snapshot = Json.parseToJsonElement(
            """{"make":{"id":"1","name":"Toyota","raw_data":{"extra":1}},"model":{"id":"mo","name":"Focus","raw_data":{}},"year":2021,"trim":{"id":"10","name":"SE","raw_data":{}},"bodies":[{"height":58.2}],"engines":[{"transmission":"Automatic"}],"mileages":[],"exterior_colors":[{"name":"White","rgb":"255,255,255"},{"name":"Black","rgb":"0,0,0"}],"interior_colors":[],"selected_exterior_color":{"name":"White","rgb":"255,255,255"},"equipment_data":{},"vin":null,"vin_data":null,"extra":{"preserved":true}}""",
        ).jsonObject
        return GarageVehicle("existing", "Toyota Focus 2021", "passenger", VerificationStatus.None, vehicleData = snapshot)
    }

    @Test
    fun editRestoresVehicleColorsAndUpdatesSameIdWithoutAddingAnotherCar() = runTest {
        val saved = savedVehicle()
        val service = FakeGarageService().apply { vehicles = listOf(saved); active = saved.id }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Edit(saved))
        assertFalse(bloc.state.value.canSubmit)
        runCurrent()
        assertTrue(bloc.state.value.canSubmit)
        assertEquals("Toyota", bloc.state.value.selectedMake?.name)
        assertEquals("Focus", bloc.state.value.selectedModel?.name)
        assertEquals(2021, bloc.state.value.year)
        assertEquals("10", bloc.state.value.equipment.selectedTrim?.id)
        assertEquals("White", bloc.state.value.equipment.selections[VehicleConfigurationField.ExteriorColor]?.rawData?.get("name")?.jsonPrimitive?.content)
        val black = bloc.state.value.equipment.options(VehicleConfigurationField.ExteriorColor).first { it.name.startsWith("Black") }
        bloc.dispatch(AddVehicleEvent.SelectConfigurationOption(VehicleConfigurationField.ExteriorColor, black.id))
        service.failAddVehicle = true
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        assertTrue(bloc.state.value.error.isNotEmpty())
        assertEquals(saved, service.vehicles.single())
        service.failAddVehicle = false
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        assertEquals(saved.id, service.updatedVehicleId)
        assertEquals(saved.id, bloc.state.value.added?.id)
        assertEquals(1, service.vehicles.size)
        assertEquals(saved.id, service.active)
        assertEquals(black.rawData, service.submittedVehicle?.vehicleData?.get("selected_exterior_color"))
        assertEquals(saved.vehicleData?.get("extra"), service.submittedVehicle?.vehicleData?.get("extra"))
    }

    @Test
    fun editingYearReloadsEquipmentAndRetainsUpdateTarget() = runTest {
        val saved = savedVehicle()
        val service = FakeGarageService().apply { vehicles = listOf(saved) }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Edit(saved))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2020))
        runCurrent()
        assertNull(bloc.state.value.equipment.selectedTrim)
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        assertEquals(saved.id, service.updatedVehicleId)
        assertEquals(2020, service.submittedVehicle?.year)
        assertEquals(1, service.vehicles.size)
    }

    @Test
    fun editCatalogFailureSupportsRetryAndIgnoresLateResponseAfterClose() = runTest {
        val service = FakeGarageService().apply { failModels = true }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Edit(savedVehicle()))
        runCurrent()
        assertFalse(bloc.state.value.canSubmit)
        assertTrue(bloc.state.value.editCatalogError.isNotEmpty())
        service.failModels = false
        val deferred = CompletableDeferred<List<VehicleEquipmentTrim>>()
        service.equipmentResponse = deferred
        bloc.dispatch(AddVehicleEvent.RetryEditCatalog)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.Close)
        deferred.complete(service.equipmentTrimsResult)
        runCurrent()
        assertEquals(AddVehicleState(), bloc.state.value)
    }

    @Test
    fun deleteNeedsConfirmationAndPreservesListOnFailureThenSelectsRemainingVehicle() = runTest {
        val service = FakeGarageService()
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        bloc.dispatch(GarageEvent.RequestDelete("a"))
        assertNull(service.deletedVehicleId)
        bloc.dispatch(GarageEvent.CancelDelete)
        assertNull(bloc.state.value.deleteVehicleId)
        bloc.dispatch(GarageEvent.RequestDelete("a"))
        service.fail = true
        bloc.dispatch(GarageEvent.ConfirmDelete)
        runCurrent()
        assertEquals(2, bloc.state.value.vehicles.size)
        assertEquals("a", bloc.state.value.activeVehicleId)
        assertEquals("a", bloc.state.value.deleteVehicleId)
        assertTrue(bloc.state.value.error.isNotEmpty())
        service.fail = false
        bloc.dispatch(GarageEvent.ConfirmDelete)
        runCurrent()
        assertEquals(listOf("t"), bloc.state.value.vehicles.map { it.id })
        assertEquals("t", bloc.state.value.activeVehicleId)
        bloc.dispatch(GarageEvent.RequestDelete("t"))
        bloc.dispatch(GarageEvent.ConfirmDelete)
        runCurrent()
        assertTrue(bloc.state.value.vehicles.isEmpty())
        assertNull(bloc.state.value.activeVehicleId)
    }

    @Test
    fun clearIgnoresLateDeleteAndBlocksDuplicateRequests() = runTest {
        val service = FakeGarageService()
        val bloc = GarageBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(GarageEvent.Load)
        runCurrent()
        service.deleteResponse = CompletableDeferred()
        bloc.dispatch(GarageEvent.RequestDelete("a"))
        bloc.dispatch(GarageEvent.ConfirmDelete)
        runCurrent()
        assertEquals(2, bloc.state.value.vehicles.size)
        bloc.dispatch(GarageEvent.ConfirmDelete)
        bloc.dispatch(GarageEvent.Clear)
        service.deleteResponse?.complete("t")
        runCurrent()
        assertEquals(GarageState(), bloc.state.value)
    }

    @Test
    fun mutationRpcUsesExactWireAndRequiresDeletionResponse() = runTest {
        val vehicle = savedVehicle()
        val snapshot = requireNotNull(vehicle.vehicleData)
        val service = RpcGarageService { method, raw ->
            val params = Json.parseToJsonElement(raw).jsonObject
            assertEquals("t", params["access_token"]?.jsonPrimitive?.content)
            assertEquals(vehicle.id, params["vehicle_id"]?.jsonPrimitive?.content)
            when (method) {
                "garage.vehicles.update" -> {
                    assertEquals(snapshot, params["vehicle"])
                    assertEquals("10", params["trim_id"]?.jsonPrimitive?.content)
                    """{"vehicle":{"id":"existing","name":"Toyota Focus","category":"passenger","vehicle":$snapshot}}"""
                }
                "garage.vehicles.delete" -> {
                    assertEquals(setOf("access_token", "vehicle_id"), params.keys)
                    """{"active_vehicle_id":null}"""
                }
                else -> error("Unexpected RPC: $method")
            }
        }
        assertEquals(vehicle.id, service.updateVehicle(testSession(), vehicle.id, NewVehicle("passenger", "1", "mo", 2021, "10", snapshot)).id)
        assertNull(service.deleteVehicle(testSession(), vehicle.id))
        kotlin.test.assertFailsWith<GarageException> {
            RpcGarageService { _, _ -> "{}" }.deleteVehicle(testSession(), vehicle.id)
        }
    }
}
