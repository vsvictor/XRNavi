package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleEquipmentTest {
    @Test
    fun colorSwatchesUseExactRgbAndRejectMissingOrInvalidValues() {
        fun option(rgb: String) = VehicleConfigurationOption(
            "color", "Color", Json.parseToJsonElement("""{"rgb":$rgb}""").jsonObject,
        )
        assertEquals(androidx.compose.ui.graphics.Color.White, option("\"255,255,255\"").swatchColor())
        assertEquals(androidx.compose.ui.graphics.Color.Black, option("\"0, 0, 0\"").swatchColor())
        assertEquals(androidx.compose.ui.graphics.Color.Red, option("\"#FF0000\"").swatchColor())
        assertEquals(
            androidx.compose.ui.graphics.Color(red = 12, green = 34, blue = 56),
            option("\"0c2238\"").swatchColor(),
        )
        for (rgb in listOf("null", "\"\"", "\"256,0,0\"", "\"-1,0,0\"", "\"1,2\"", "\"blue\"")) {
            assertNull(option(rgb).swatchColor())
        }
        assertNull(VehicleConfigurationOption("none", "None", kotlinx.serialization.json.JsonObject(emptyMap())).swatchColor())
    }

    @Test
    fun mileageRpcUsesSelectedV2TrimAndConvertsFuelConsumption() = runTest {
        val pages = mutableListOf<Int>()
        val service = RpcGarageService { method, raw ->
            assertEquals("car.makemodeltrimmileages_index2_get", method)
            val params = Json.parseToJsonElement(raw).jsonObject
            assertEquals(setOf("access_token", "trim_id", "page"), params.keys)
            assertEquals("8873", params["trim_id"]?.jsonPrimitive?.content)
            assertEquals("t", params["access_token"]?.jsonPrimitive?.content)
            pages += params["page"]!!.jsonPrimitive.content.toInt()
            val response = """{"collection":{"pages":2},"data":[{"trim_id":8873,"combined_mpg":"30","epa_city_mpg":"25","epa_highway_mpg":"35","range_city":"400","fuel_tank_capacity":null}]}"""
            if (pages.size == 1) response else """{"result":$response}"""
        }
        val records = service.equipmentMileages(testSession(), "8873")
        assertEquals(listOf(1, 2), pages)
        assertEquals(2, records.size)
        val fields = equipmentFields(records.first()).toMap()
        assertEquals("7.8", fields["Змішана витрата, л/100 км"])
        assertEquals("9.4", fields["Витрата в місті, л/100 км"])
        assertEquals("6.7", fields["Витрата на трасі, л/100 км"])
        assertEquals("643.7", fields["Запас ходу в місті, км"])
        assertFalse("Паливний бак, л" in fields)
        assertEquals(emptyList(), RpcGarageService { _, _ ->
            """{"collection":{"pages":0},"data":[]}"""
        }.equipmentMileages(testSession(), "8873"))
        assertFailsWith<GarageException> {
            RpcGarageService { _, _ ->
                """{"collection":{"pages":1},"data":[{"combined_mpg":"bad"}]}"""
            }.equipmentMileages(testSession(), "8873")
        }
    }

    @Test
    fun mileagesLoadOnlyAfterTrimSelectionAndIgnoreClearedOrChangedTrim() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        assertNull(service.requestedMileageTrim)
        service.failMileages = true
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        assertTrue(bloc.state.value.equipment.mileagesLoading)
        runCurrent()
        assertEquals("10", service.requestedMileageTrim)
        assertTrue(bloc.state.value.equipment.mileagesError.isNotEmpty())
        assertEquals(service.bodiesResult, bloc.state.value.equipment.bodies)
        service.failMileages = false
        bloc.dispatch(AddVehicleEvent.RetryEquipmentMileages)
        runCurrent()
        assertEquals(service.mileagesResult, bloc.state.value.equipment.mileages)
        assertFalse(bloc.state.value.equipment.mileagesLoading)
        assertEquals("", bloc.state.value.equipment.mileagesError)
        val pending = CompletableDeferred<List<kotlinx.serialization.json.JsonObject>>()
        service.mileagesResponse = pending
        bloc.dispatch(AddVehicleEvent.RetryEquipmentMileages)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.EquipmentTrimQueryChanged(""))
        assertNull(bloc.state.value.equipment.selectedTrim)
        assertTrue(bloc.state.value.equipment.mileages.isEmpty())
        pending.complete(service.mileagesResult)
        runCurrent()
        assertTrue(bloc.state.value.equipment.mileages.isEmpty())
        val stale = CompletableDeferred<List<kotlinx.serialization.json.JsonObject>>()
        service.mileagesResponse = stale
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        runCurrent()
        service.mileagesResponse = null
        service.mileagesResult = listOf(Json.parseToJsonElement("""{"combined_mpg":40}""").jsonObject)
        bloc.dispatch(AddVehicleEvent.RetryEquipmentMileages)
        runCurrent()
        stale.complete(listOf(Json.parseToJsonElement("""{"combined_mpg":10}""").jsonObject))
        runCurrent()
        assertEquals(service.mileagesResult, bloc.state.value.equipment.mileages)
        bloc.dispatch(AddVehicleEvent.MakeChanged("Ford"))
        assertEquals(VehicleEquipmentState(), bloc.state.value.equipment)
    }

    @Test
    fun transmissionFieldsStaySeparateFromEngineAndPreserveRecordPairing() {
        val records = listOf(
            Json.parseToJsonElement("""{"size":"2.5","transmission":"Automatic","drive_type":"Front wheel drive"}""").jsonObject,
            Json.parseToJsonElement("""{"size":"3.5","transmission":"Manual","drive_type":"All wheel drive"}""").jsonObject,
        )
        assertEquals(
            listOf(
                listOf("Трансмісія" to "Automatic", "Привід" to "Front wheel drive"),
                listOf("Трансмісія" to "Manual", "Привід" to "All wheel drive"),
            ),
            records.map(::transmissionEquipmentFields),
        )
        assertEquals(
            listOf(listOf("Об'єм двигуна, л" to "2.5"), listOf("Об'єм двигуна, л" to "3.5")),
            records.map(::engineEquipmentFields),
        )
        assertEquals(emptyList(), transmissionEquipmentFields(
            Json.parseToJsonElement("""{"transmission":null,"drive_type":"","size":2.5}""").jsonObject,
        ))
        assertEquals(listOf("Привід" to "Rear wheel drive"), transmissionEquipmentFields(
            Json.parseToJsonElement("""{"drive_type":"Rear wheel drive"}""").jsonObject,
        ))
    }

    @Test
    fun engineRpcUsesV2TrimIdAndParsesAllPages() = runTest {
        val pages = mutableListOf<Int>()
        val service = RpcGarageService { method, raw ->
            assertEquals("car.makemodeltrimengines_index2_get", method)
            val params = Json.parseToJsonElement(raw).jsonObject
            assertEquals(setOf("access_token", "trim_id", "page"), params.keys)
            assertEquals("8873", params["trim_id"]?.jsonPrimitive?.content)
            assertEquals("t", params["access_token"]?.jsonPrimitive?.content)
            pages += params["page"]!!.jsonPrimitive.content.toInt()
            val response = """{"collection":{"pages":2},"data":[{"id":8873,"trim_id":8873,"fuel_type":"Gasoline","size":"2.5","horsepower_hp":200,"torque_ft_lbs":"180","valves":null,"transmission":"Automatic","drive_type":"Front wheel drive"}]}"""
            if (pages.size == 1) response else """{"result":$response}"""
        }
        val engines = service.equipmentEngines(testSession(), "8873")
        assertEquals(listOf(1, 2), pages)
        assertEquals(2, engines.size)
        assertEquals("8873", engines.first()["trim_id"]?.jsonPrimitive?.content)
        val fields = equipmentFields(engines.first()).toMap()
        assertEquals("Gasoline", fields["Пальне"])
        assertEquals("2.5", fields["Об'єм двигуна, л"])
        assertEquals("200", fields["Потужність, hp"])
        assertEquals("244", fields["Крутний момент, Н·м"])
        assertFalse("Клапани" in fields)
        assertEquals(
            listOf("Трансмісія" to "Automatic", "Привід" to "Front wheel drive"),
            transmissionEquipmentFields(engines.first()),
        )
        assertEquals(emptyList(), RpcGarageService { _, _ ->
            """{"collection":{"pages":0},"data":[]}"""
        }.equipmentEngines(testSession(), "8873"))
        assertFailsWith<GarageException> {
            RpcGarageService { _, _ -> """{"collection":{"pages":1},"data":[{"size":"bad"}]}""" }
                .equipmentEngines(testSession(), "8873")
        }
        val failure = IllegalStateException("Unavailable")
        val error = assertFailsWith<GarageException> {
            RpcGarageService { _, _ -> throw failure }.equipmentEngines(testSession(), "8873")
        }
        assertSame(failure, error.cause)
    }

    @Test
    fun selectedTrimLoadsEnginesIndependentlyAndRejectsStaleResponses() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        service.failEngines = true
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        assertTrue(bloc.state.value.equipment.enginesLoading)
        runCurrent()
        assertEquals("10", service.requestedEngineTrim)
        assertTrue(bloc.state.value.equipment.enginesError.isNotEmpty())
        assertEquals(service.bodiesResult, bloc.state.value.equipment.bodies)
        service.failEngines = false
        bloc.dispatch(AddVehicleEvent.RetryEquipmentEngines)
        runCurrent()
        assertEquals(service.enginesResult, bloc.state.value.equipment.engines)
        assertFalse(bloc.state.value.equipment.enginesLoading)
        assertEquals("", bloc.state.value.equipment.enginesError)
        val pending = CompletableDeferred<List<kotlinx.serialization.json.JsonObject>>()
        service.enginesResponse = pending
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.RetryEquipmentEngines)
        runCurrent()
        service.enginesResponse = null
        service.enginesResult = listOf(Json.parseToJsonElement("""{"size":3.0}""").jsonObject)
        bloc.dispatch(AddVehicleEvent.RetryEquipmentEngines)
        runCurrent()
        pending.complete(listOf(Json.parseToJsonElement("""{"size":1.0}""").jsonObject))
        runCurrent()
        assertEquals(service.enginesResult, bloc.state.value.equipment.engines)
        val outdated = CompletableDeferred<List<kotlinx.serialization.json.JsonObject>>()
        service.enginesResponse = outdated
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeChanged("Ford"))
        outdated.complete(service.enginesResult)
        runCurrent()
        assertEquals(VehicleEquipmentState(), bloc.state.value.equipment)
    }

    @Test
    fun bodyRpcUsesSelectedTrimAndParsesEveryPageWithMetricFields() = runTest {
        val pages = mutableListOf<Int>()
        val service = RpcGarageService { method, raw ->
            assertEquals("car.makemodeltrimbodies_index2_get", method)
            val params = Json.parseToJsonElement(raw).jsonObject
            assertEquals(setOf("access_token", "trim_id", "page"), params.keys)
            assertEquals("16166", params["trim_id"]?.jsonPrimitive?.content)
            assertEquals("t", params["access_token"]?.jsonPrimitive?.content)
            pages += params["page"]!!.jsonPrimitive.content.toInt()
            val response = """{"collection":{"pages":2},"data":[{"id":456,"trim_id":16166,"type":"Sedan","length":"191.7","cargo_capacity":"16.0","curb_weight":3460,"gross_weight":null}]}"""
            if (pages.size == 1) """{"jsonrpc":"2.0","id":1,"result":$response}""" else response
        }
        val bodies = service.equipmentBodies(testSession(), "16166")
        assertEquals(listOf(1, 2), pages)
        assertEquals(2, bodies.size)
        assertEquals("16166", bodies.first()["trim_id"]?.jsonPrimitive?.content)
        val fields = equipmentFields(bodies.first()).toMap()
        assertEquals("4869.2", fields["Довжина, мм"])
        assertEquals("453.1", fields["Багажник, л"])
        assertEquals("1569.4", fields["Споряджена маса, кг"])
        assertFalse("Повна маса, кг" in fields)
        assertFailsWith<GarageException> {
            RpcGarageService { _, _ -> """{"collection":{"pages":1},"data":[{"length":"bad"}]}""" }
                .equipmentBodies(testSession(), "16166")
        }
    }

    @Test
    fun bodyRpcPreservesOriginalFailureAndCancellation() = runTest {
        val failure = IllegalStateException("CarAPI returned HTTP 403")
        val error = assertFailsWith<GarageException> {
            RpcGarageService { _, _ -> throw failure }.equipmentBodies(testSession(), "8873")
        }
        assertEquals(GarageException.Kind.Failed, error.kind)
        assertSame(failure, error.cause)
        val cancelled = kotlinx.coroutines.CancellationException("Cancelled")
        assertSame(cancelled, assertFailsWith<kotlinx.coroutines.CancellationException> {
            RpcGarageService { _, _ -> throw cancelled }.equipmentBodies(testSession(), "8873")
        })
    }

    @Test
    fun selectingTrimLoadsBodiesWithRetryAndIgnoresOutdatedResponses() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        service.failBodies = true
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        assertTrue(bloc.state.value.equipment.bodiesLoading)
        runCurrent()
        assertEquals("10", service.requestedBodyTrim)
        assertTrue(bloc.state.value.equipment.bodiesError.isNotEmpty())
        assertTrue(bloc.state.value.equipment.bodies.isEmpty())
        service.failBodies = false
        bloc.dispatch(AddVehicleEvent.RetryEquipmentBodies)
        runCurrent()
        assertEquals(service.bodiesResult, bloc.state.value.equipment.bodies)
        assertEquals("", bloc.state.value.equipment.bodiesError)
        val pending = CompletableDeferred<List<kotlinx.serialization.json.JsonObject>>()
        service.bodiesResponse = pending
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim("10"))
        runCurrent()
        assertTrue(bloc.state.value.equipment.bodies.isEmpty())
        bloc.dispatch(AddVehicleEvent.MakeChanged("Ford"))
        pending.complete(service.bodiesResult)
        runCurrent()
        assertEquals(VehicleEquipmentState(), bloc.state.value.equipment)
    }

    @Test
    fun trimsAreRequestedThroughAuthorizedRpcWithAllPages() = runTest {
        val pages = mutableListOf<Int>()
        val service = RpcGarageService { method, raw ->
            assertEquals("car.oemtrims_index_get", method)
            val params = Json.parseToJsonElement(raw).jsonObject
            assertEquals("Toyota", params["make"]?.jsonPrimitive?.content)
            assertEquals("Camry", params["model"]?.jsonPrimitive?.content)
            assertEquals("2020", params["year"]?.jsonPrimitive?.content)
            assertEquals("100", params["limit"]?.jsonPrimitive?.content)
            assertEquals("t", params["access_token"]?.jsonPrimitive?.content)
            val page = params["page"]!!.jsonPrimitive.content.toInt()
            pages += page
            """{"jsonrpc":"2.0","id":1,"result":{"data":[{"id":$page,"trim":"Ignored trim","submodel":"Other","description":"SE $page","extra":{"value":true}}],"collection":{"pages":2}}}"""
        }
        val trims = service.equipmentTrims(testSession(), "Toyota", "Camry", 2020)
        assertEquals(listOf(1, 2), pages)
        assertEquals(listOf("1", "2"), trims.map { it.id })
        assertEquals(listOf("SE 1", "SE 2"), trims.map { it.name })
        assertTrue("extra" in trims.first().rawData)
    }

    @Test
    fun detailsPreserveSectionsAndRejectInvalidData() = runTest {
        val response =
            """{"bodies":[{"doors":4}],"engines":[{"transmission":"automatic"}],"mileages":[{"range_electric":200}],"exterior_colors":[{"name":"White","rgb":"255,255,255"}],"interior_colors":[],"extra":null}"""
        val service = RpcGarageService { method, raw ->
            assertEquals("car.oemtrims_view_get", method)
            val params = Json.parseToJsonElement(raw).jsonObject
            assertEquals("42", params["id"]?.jsonPrimitive?.content)
            assertEquals("t", params["access_token"]?.jsonPrimitive?.content)
            response
        }
        val details = service.equipmentDetails(testSession(), "42")
        assertEquals(Json.parseToJsonElement(response).jsonObject, details.rawData)
        assertEquals(1, details.bodies.size)
        assertEquals(1, details.engines.size)
        assertEquals(1, details.mileages.size)
        assertEquals(1, details.exteriorColors.size)
        assertTrue(details.interiorColors.isEmpty())
        for (invalid in listOf("{}", response.replace("200", "\"bad\""))) {
            assertFailsWith<GarageException> {
                RpcGarageService { _, _ -> invalid }.equipmentDetails(testSession(), "42")
            }
        }
        assertFailsWith<GarageException> {
            RpcGarageService { _, _ -> """{"data":[],"collection":{"pages":"bad"}}""" }
                .equipmentTrims(testSession(), "Toyota", "Camry", 2020)
        }
    }

    @Test
    fun fieldsConvertUnitsAndDoNotInventMissingValues() {
        val fields = equipmentFields(
            Json.parseToJsonElement(
                """{"combined_mpg":30,"range_electric":200,"length":100,"fuel_tank_capacity":10,"horsepower_hp":null,"id":1}""",
            ).jsonObject
        ).toMap()
        assertEquals("7.8", fields["Змішана витрата, л/100 км"])
        assertEquals("321.9", fields["Електричний запас ходу, км"])
        assertEquals("2540", fields["Довжина, мм"])
        assertEquals("37.9", fields["Паливний бак, л"])
        assertEquals(4, fields.size)
        assertEquals(
            "Немає даних", equipmentFields(
                Json.parseToJsonElement("""{"combined_mpg":0}""").jsonObject,
            ).single().second
        )
    }

    @Test
    fun yearLoadsAllConfigurationDetailsWithRetry() = runTest {
        val service = FakeGarageService().apply { failEquipment = true }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        assertEquals(AddVehicleTab.Equipment, bloc.state.value.selectedTab)
        assertTrue(bloc.state.value.equipment.loading)
        runCurrent()
        assertEquals(Triple("Toyota", "Focus", 2021), service.requestedEquipment)
        assertTrue(bloc.state.value.equipment.error.isNotEmpty())
        service.failEquipment = false
        bloc.dispatch(AddVehicleEvent.RetryEquipment)
        runCurrent()
        assertEquals(service.equipmentTrimsResult, bloc.state.value.equipment.trims)
        assertEquals("10", service.requestedEquipmentTrim)
        val trim = service.equipmentTrimsResult.single()
        bloc.dispatch(AddVehicleEvent.EquipmentTrimQueryChanged("sE"))
        assertEquals(listOf(trim), bloc.state.value.equipment.filteredTrims)
        bloc.dispatch(AddVehicleEvent.SelectEquipmentTrim(trim.id))
        assertEquals(trim, bloc.state.value.equipment.selectedTrim)
        assertEquals(trim.name, bloc.state.value.equipment.trimQuery)
        assertFalse(bloc.state.value.equipment.trimExpanded)
        assertEquals(
            listOf(service.equipmentDetailsResult),
            bloc.state.value.equipment.configurations
        )
        bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.General))
        bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.Equipment))
        assertEquals(trim, bloc.state.value.equipment.selectedTrim)
        assertEquals(
            listOf(service.equipmentDetailsResult),
            bloc.state.value.equipment.configurations
        )
    }

    @Test
    fun failedDetailsDoNotExposePartialOptionsAndCanBeRetried() = runTest {
        val response = CompletableDeferred<VehicleEquipmentDetails>()
        val service = FakeGarageService().apply { equipmentDetailsResponse = response }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        assertTrue(bloc.state.value.equipment.loading)
        response.completeExceptionally(GarageException(GarageException.Kind.Failed))
        runCurrent()
        assertFalse(bloc.state.value.equipment.loading)
        assertTrue(bloc.state.value.equipment.error.isNotEmpty())
        assertTrue(bloc.state.value.equipment.configurations.isEmpty())
        service.equipmentDetailsResponse = null
        bloc.dispatch(AddVehicleEvent.RetryEquipment)
        runCurrent()
        assertEquals("", bloc.state.value.equipment.error)
        assertEquals(listOf(service.equipmentDetailsResult), bloc.state.value.equipment.configurations)
    }

    @Test
    fun outdatedListAndDetailsCannotOverwriteChangedVehicle() = runTest {
        for (pendingDetails in listOf(false, true)) {
            val service = FakeGarageService()
            val listResponse = CompletableDeferred<List<VehicleEquipmentTrim>>()
            val detailResponse = CompletableDeferred<VehicleEquipmentDetails>()
            if (!pendingDetails) service.equipmentResponse = listResponse
            else service.equipmentDetailsResponse = detailResponse
            val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
            bloc.dispatch(AddVehicleEvent.Open)
            runCurrent()
            bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
            runCurrent()
            bloc.dispatch(AddVehicleEvent.SelectMake("1"))
            runCurrent()
            bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
            runCurrent()
            bloc.dispatch(AddVehicleEvent.SelectYear(2021))
            runCurrent()
            bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.General))
            bloc.dispatch(AddVehicleEvent.MakeChanged("Ford"))
            listResponse.complete(service.equipmentTrimsResult)
            detailResponse.complete(service.equipmentDetailsResult)
            runCurrent()
            assertEquals(VehicleEquipmentState(), bloc.state.value.equipment)
            assertFalse(bloc.state.value.equipment.loading)
        }
    }

    @Test
    fun engineDetailsIncludeAllProvidedCharacteristicsWithoutRecordIdentifiers() {
        val fields = engineDetailFields(Json.parseToJsonElement(
            """{"id":1,"trim_id":42,"fuel_type":"Gasoline","size":2.5,"horsepower_hp":200,"torque_ft_lbs":100,"compression_ratio":"10:1","valves":null}""",
        ).jsonObject)
        assertEquals(listOf(
            "Пальне" to "Gasoline",
            "Об'єм двигуна, л" to "2.5",
            "Потужність, hp" to "200",
            "Крутний момент, Н·м" to "135.6",
            "compression_ratio" to "10:1",
        ), fields)
    }

    @Test
    fun engineLabelsShowOnlyFuelAndDisplacementWhilePreservingFullRecords() {
        val engine = Json.parseToJsonElement(
            """{"id":1,"size":2.5,"fuel_type":"Gasoline","engine_type":"gas","cylinders":4,"horsepower_hp":200,"transmission":"Automatic"}""",
        ).jsonObject
        val details = VehicleEquipmentDetails(
            emptyList(), listOf(engine), emptyList(), emptyList(), emptyList(),
            Json.parseToJsonElement("{}").jsonObject,
        )
        val state = VehicleEquipmentState(configurations = listOf(details))
        val option = state.options(VehicleConfigurationField.Engine).single()
        assertEquals("Пальне: Gasoline · Об'єм двигуна, л: 2.5", option.name)
        assertEquals("Пальне: Gasoline\nОб'єм двигуна, л: 2.5", option.popupName)
        assertEquals(engine, option.rawData)
        assertTrue(state.copy(
            configurations = listOf(details.copy(
                engines = listOf(Json.parseToJsonElement("""{"horsepower_hp":200}""").jsonObject),
            )),
        ).options(VehicleConfigurationField.Engine).isEmpty())
    }

    @Test
    fun dropdownsOfferOnlyCompatibleOptionsIncludingEngineTransmissionPairs() {
        fun obj(text: String) = Json.parseToJsonElement(text).jsonObject
        val sedan = VehicleEquipmentDetails(
            listOf(obj("""{"id":1,"type":"Sedan","doors":4}""")),
            listOf(
                obj("""{"id":1,"size":2.0,"transmission":"Manual"}"""),
                obj("""{"id":2,"size":3.0,"transmission":"Automatic"}"""),
            ),
            emptyList(),
            listOf(obj("""{"name":"White","rgb":"255,255,255"}""")),
            listOf(obj("""{"name":"Black","rgb":"0,0,0"}""")), obj("{}"),
        )
        val suv = sedan.copy(
            bodies = listOf(obj("""{"type":"SUV","doors":5}""")),
            engines = listOf(obj("""{"size":4.0,"transmission":"Automatic"}""")),
            exteriorColors = listOf(obj("""{"name":"Red","rgb":"255,0,0"}""")),
        )
        val state = VehicleEquipmentState(configurations = listOf(sedan, sedan, suv))
        val body = VehicleConfigurationField.Body
        val engine = VehicleConfigurationField.Engine
        val transmission = VehicleConfigurationField.Transmission
        val exterior = VehicleConfigurationField.ExteriorColor
        assertEquals(2, state.options(body).size)
        assertEquals(3, state.options(engine).size)
        var selected =
            state.copy(selections = mapOf(body to state.options(body).first { "Sedan" in it.name }))
        assertEquals(2, selected.options(engine).size)
        assertEquals(listOf("White · 255,255,255"), selected.options(exterior).map { it.name })
        val smallEngine =
            selected.options(engine).first { it.rawData["size"]?.jsonPrimitive?.content == "2.0" }
        selected = selected.copy(selections = selected.selections + (engine to smallEngine))
        assertEquals(listOf("Manual"), selected.options(transmission).map { it.name })
        selected = selected.copy(
            selections = selected.selections + (transmission to selected.options(transmission)
                .single())
        )
        assertEquals(listOf(smallEngine.id), selected.options(engine).map { it.id })
        assertEquals(2, selected.compatibleDetails.size)
        val red = state.options(exterior).first { "Red" in it.name }
        val reverse = state.copy(selections = mapOf(exterior to red))
        assertEquals(listOf("SUV · 5 дв."), reverse.options(body).map { it.name })
        assertEquals(1, reverse.options(engine).size)
        assertEquals(emptyList(), VehicleEquipmentState().options(body))
    }

    @Test
    fun fieldQueriesFilterLocallyAndSelectionsSurviveTabChanges() = runTest {
        val service = FakeGarageService().apply {
            equipmentDetailsResult = equipmentDetailsResult.copy(
                bodies = listOf(
                    Json.parseToJsonElement("""{"type":"Sedan"}""").jsonObject,
                    Json.parseToJsonElement("""{"type":"SUV"}""").jsonObject,
                ),
                engines = listOf(
                    Json.parseToJsonElement("""{"fuel_type":"Gasoline","size":2.5,"horsepower_hp":200}""").jsonObject,
                ),
            )
        }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        val engine = bloc.state.value.equipment.options(VehicleConfigurationField.Engine).single()
        bloc.dispatch(AddVehicleEvent.ShowEngineDetails(engine.id))
        assertEquals(engine, bloc.state.value.equipment.engineDetails)
        assertTrue(bloc.state.value.equipment.selections.isEmpty())
        bloc.dispatch(AddVehicleEvent.DismissEngineDetails)
        assertNull(bloc.state.value.equipment.engineDetails)
        bloc.dispatch(AddVehicleEvent.ShowEngineDetails("invalid"))
        assertNull(bloc.state.value.equipment.engineDetails)
        bloc.dispatch(AddVehicleEvent.ShowEngineDetails(engine.id))
        bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.General))
        assertNull(bloc.state.value.equipment.engineDetails)
        bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.Equipment))
        val field = VehicleConfigurationField.Body
        val option = bloc.state.value.equipment.options(field).first { it.name == "Sedan" }
        bloc.dispatch(AddVehicleEvent.ConfigurationExpanded(field, true))
        assertEquals(field, bloc.state.value.equipment.expanded)
        bloc.dispatch(AddVehicleEvent.SelectConfigurationOption(field, option.id))
        assertEquals(option, bloc.state.value.equipment.selections[field])
        assertEquals("Sedan", bloc.state.value.equipment.queries[field])
        assertNull(bloc.state.value.equipment.expanded)
        bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.General))
        bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.Equipment))
        assertEquals(option, bloc.state.value.equipment.selections[field])
        bloc.dispatch(AddVehicleEvent.ConfigurationQueryChanged(field, "sU"))
        assertNull(bloc.state.value.equipment.selections[field])
        assertEquals("sU", bloc.state.value.equipment.queries[field])
        assertEquals(field, bloc.state.value.equipment.expanded)
        assertEquals(
            listOf("SUV"),
            bloc.state.value.equipment.filteredOptions(field).map { it.name })
        bloc.dispatch(AddVehicleEvent.SelectConfigurationOption(field, "invalid"))
        assertNull(bloc.state.value.equipment.selections[field])
        bloc.dispatch(AddVehicleEvent.SelectConfigurationOption(field, option.id))
        assertEquals(option, bloc.state.value.equipment.selections[field])
        bloc.dispatch(AddVehicleEvent.MakeChanged("Ford"))
        assertEquals(VehicleEquipmentState(), bloc.state.value.equipment)
    }
}
