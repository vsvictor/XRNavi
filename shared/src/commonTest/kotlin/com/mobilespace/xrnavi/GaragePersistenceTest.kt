package com.mobilespace.xrnavi

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GaragePersistenceTest {
    private fun raw(value: String) = Json.parseToJsonElement(value).jsonObject

    private fun prepare(bloc: AddVehicleBloc) {
        bloc.dispatch(AddVehicleEvent.Open)
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
    }

    @Test
    fun snapshotPreservesRawSelectedVehicleAndVinWithoutOtherTrimsOrCredentials() {
        val body = raw("""{"trim_id":8873,"type":"Sedan","length":"192.1"}""")
        val engine = raw("""{"trim_id":8873,"size":"2.5","transmission":"Automatic"}""")
        val mileage = raw("""{"trim_id":8873,"combined_mpg":"30"}""")
        val exterior = raw("""{"name":"White","rgb":"255,255,255","custom":true}""")
        val interior = raw("""{"name":"Black","rgb":"0,0,0"}""")
        val trim = VehicleEquipmentTrim("8873", "LE", raw("""{"id":8873,"description":"LE"}"""))
        val details = VehicleEquipmentDetails(
            listOf(body), listOf(engine), listOf(mileage), listOf(exterior), listOf(interior),
            raw("""{"id":8873,"extra":{"preserve":true}}"""),
        )
        val state = AddVehicleState(
            selectedMake = VehicleMake("22", "Toyota", raw("""{"id":22,"name":"Toyota","extra":1}""")),
            selectedModel = VehicleModelItem("4779", "Camry", listOf(2020), emptyList(), raw("""{"id":4779,"name":"Camry"}""")),
            year = 2020, vinQuery = "1GTG6CEN0L1139305", vinSearched = true,
            decodedVin = DecodedVehicleVin("Toyota", "Camry", 2020, raw("""{"make":"Toyota","extra":[1,2]}""")),
            equipment = VehicleEquipmentState(
                trims = listOf(trim, VehicleEquipmentTrim("other", "Other", raw("{}"))),
                selectedTrim = trim, bodies = listOf(body), engines = listOf(engine), mileages = listOf(mileage),
                configurations = listOf(details, details.copy(exteriorColors = listOf(raw("""{"name":"Red"}""")))),
                selections = mapOf(
                    VehicleConfigurationField.ExteriorColor to VehicleEquipmentState(configurations = listOf(details))
                        .options(VehicleConfigurationField.ExteriorColor).single(),
                    VehicleConfigurationField.InteriorColor to VehicleEquipmentState(configurations = listOf(details))
                        .options(VehicleConfigurationField.InteriorColor).single(),
                ),
            ),
        )
        val snapshot = state.vehicleSnapshot()
        assertEquals(state.selectedMake?.rawData, snapshot["make"]?.jsonObject?.get("raw_data"))
        assertEquals(trim.rawData, snapshot["trim"]?.jsonObject?.get("raw_data"))
        assertEquals(details.rawData, snapshot["equipment_data"])
        assertEquals(192.1 * 0.0254, snapshot["dimensions"]?.jsonObject?.get("length_m")?.jsonPrimitive?.content?.toDouble())
        assertEquals(exterior, snapshot["selected_exterior_color"])
        assertEquals(interior, snapshot["selected_interior_color"])
        assertEquals(state.decodedVin?.rawData, snapshot["vin_data"])
        assertEquals(state.vinQuery, snapshot["vin"]?.jsonPrimitive?.content)
        assertFalse(snapshot.toString().contains("access_token"))
        assertFalse(snapshot.toString().contains("Red"))
        assertEquals(listOf(exterior), state.equipment.options(VehicleConfigurationField.ExteriorColor).map { it.rawData })
        val changed = state.copy(year = 2021).vehicleSnapshot()
        assertEquals(kotlinx.serialization.json.JsonNull, changed["vin"])
        assertEquals(kotlinx.serialization.json.JsonNull, changed["vin_data"])
    }

    @Test
    fun rpcSendsFullSnapshotAndListsThePersistedServerRecord() = runTest {
        val snapshot = raw("""{"make":{"id":"22","name":"Toyota"},"model":{"id":"4779","name":"Camry"},"year":2020,"trim":{"id":"8873","name":"LE"},"bodies":[{"type":"Sedan"}],"extra":{"retained":true}}""")
        var saved: JsonObject? = null
        val service = RpcGarageService { method, paramsJson ->
            val params = raw(paramsJson)
            assertEquals("t", params["access_token"]?.jsonPrimitive?.content)
            assertFalse("user_id" in params)
            when (method) {
                "garage.vehicles.add" -> {
                    assertEquals(snapshot, params["vehicle"])
                    assertEquals("8873", params["trim_id"]?.jsonPrimitive?.content)
                    saved = raw("""{"id":"server-id","name":"Toyota Camry · 2020","category":"passenger","verification":"none","dimensions":{"length_m":4.88},"vehicle":$snapshot}""")
                    """{"vehicle":$saved}"""
                }
                "garage.vehicles.list" -> """{"vehicles":[$saved],"active_vehicle_id":"server-id"}"""
                "garage.vehicles.set_active" -> {
                    assertEquals("server-id", params["vehicle_id"]?.jsonPrimitive?.content)
                    """{"active_vehicle_id":"server-id"}"""
                }
                else -> error("Unexpected method: $method")
            }
        }
        val added = service.addVehicle(testSession(), NewVehicle("passenger", "22", "4779", 2020, "8873", snapshot))
        assertEquals(snapshot, added.vehicleData)
        assertEquals(4.88, added.lengthMeters)
        val list = service.vehicles(testSession())
        assertEquals(listOf(added), list.vehicles)
        assertEquals(added.id, list.activeVehicleId)
        assertEquals(added.id, service.setActive(testSession(), added.id))
    }

    @Test
    fun savedBodyMeasuresAreDisplayedAfterAddAndReloadWithLegacyOverridesPreserved() = runTest {
        val snapshot = raw("""{"bodies":[{"height":"58.2","width":72.9,"length":191.7,"curb_weight":"3460","gross_weight":4500}],"engines":[{"size":"2.5","transmission":"Automatic","drive_type":"Front wheel drive"}],"mileages":[{"combined_mpg":"30"}]}""")
        val record = """{"id":"saved","name":"Toyota Camry","category":"passenger","vehicle":$snapshot}"""
        val service = RpcGarageService { method, _ ->
            when (method) {
                "garage.vehicles.add" -> """{"vehicle":$record}"""
                "garage.vehicles.list" -> """{"vehicles":[$record],"active_vehicle_id":"saved"}"""
                else -> error("Unexpected method: $method")
            }
        }
        val added = service.addVehicle(testSession(), NewVehicle("passenger", "22", "4779", 2020, "8873", snapshot))
        assertEquals(58.2 * 0.0254, added.heightMeters)
        assertEquals(72.9 * 0.0254, added.widthMeters)
        assertEquals(191.7 * 0.0254, added.lengthMeters)
        assertEquals(1569, added.weightKg)
        assertEquals(added, service.vehicles(testSession()).vehicles.single())
        assertEquals(snapshot, added.vehicleData)
        val legacy = RpcGarageService { _, _ ->
            """{"vehicles":[{"id":"legacy","name":"Legacy","dimensions":{"length_m":5.0},"weight_kg":2000,"vehicle":$snapshot}],"active_vehicle_id":null}"""
        }.vehicles(testSession()).vehicles.single()
        assertEquals(5.0, legacy.lengthMeters)
        assertEquals(2000, legacy.weightKg)
    }

    @Test
    fun missingBodyMeasuresRemainUnknownAndGrossWeightIsNotUsedAsCurbWeight() {
        val measures = raw("""{"bodies":[{"height":null,"width":0,"gross_weight":4500}]}""").bodyMeasures()
        assertNull(measures.heightMeters)
        assertNull(measures.widthMeters)
        assertNull(measures.lengthMeters)
        assertNull(measures.weightKg)
        kotlin.test.assertFailsWith<GarageException> {
            raw("""{"bodies":[{"height":"invalid"}]}""").bodyMeasures()
        }
        kotlin.test.assertFailsWith<GarageException> {
            raw("""{"bodies":[{"curb_weight":-1}]}""").bodyMeasures()
        }
    }

    @Test
    fun modernFormSavesWithoutLegacyClassOrTrimAndRefreshesGarage() = runTest {
        val service = FakeGarageService().apply { catalogClasses = emptyList() }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        prepare(bloc)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        assertFalse(bloc.state.value.canSubmit)
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        assertFalse(bloc.state.value.canSubmit)
        runCurrent()
        assertTrue(bloc.state.value.canSubmit)
        service.failAddVehicle = true
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        assertNull(bloc.state.value.added)
        assertTrue(bloc.state.value.error.isNotEmpty())
        service.failAddVehicle = false
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        val request = assertNotNull(service.submittedVehicle)
        assertEquals("passenger", request.classId)
        assertEquals("10", request.trimId)
        assertEquals(2021, request.vehicleData["year"]?.jsonPrimitive?.content?.toInt())
        assertEquals(AddVehicleStep.Review, bloc.state.value.step)
        assertNotNull(bloc.state.value.added)
        val garage = GarageBloc(service, ::testSession, backgroundScope)
        garage.dispatch(GarageEvent.Load)
        runCurrent()
        assertTrue(garage.state.value.vehicles.any { it.vehicleData == request.vehicleData })
        garage.dispatch(GarageEvent.Clear)
        assertEquals(GarageState(), garage.state.value)
    }

    @Test
    fun closingFormIgnoresLateSaveAndBlocksDuplicateSubmission() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        prepare(bloc)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        runCurrent()
        val response = CompletableDeferred<GarageVehicle>()
        service.addVehicleResponse = response
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        assertFalse(bloc.state.value.canSubmit)
        bloc.dispatch(AddVehicleEvent.Submit)
        bloc.dispatch(AddVehicleEvent.Close)
        bloc.dispatch(AddVehicleEvent.Open)
        response.complete(GarageVehicle("late", "Late", "passenger", VerificationStatus.None))
        runCurrent()
        assertNull(bloc.state.value.added)
        assertEquals(AddVehicleStep.Class, bloc.state.value.step)
    }
}
