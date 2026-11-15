package com.mobilespace.xrnavi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RpcGarageServiceTest {
    @Test
    fun yearsUsesMakeAndModelAndParsesResultArray() = kotlinx.coroutines.test.runTest {
        for (response in listOf(
            "[2015,2016,2017,2018,2019,2020]",
            """{"jsonrpc":"2.0","id":1,"result":[2015,2016,2017,2018,2019,2020]}""",
        )) {
            val service = RpcGarageService { method, paramsJson ->
                assertEquals("car.years_index2_get", method)
                val params = Json.parseToJsonElement(paramsJson).jsonObject
                assertEquals(setOf("access_token", "make", "model"), params.keys)
                assertEquals("t", params["access_token"]?.jsonPrimitive?.content)
                assertEquals("Toyota", params["make"]?.jsonPrimitive?.content)
                assertEquals("Camry", params["model"]?.jsonPrimitive?.content)
                response
            }
            assertEquals((2015..2020).toList(), service.years(testSession(), "Toyota", "Camry"))
        }
        assertEquals(emptyList(), RpcGarageService { _, _ -> "[]" }.years(testSession(), "Toyota", "Camry"))
    }

    @Test
    fun yearsRejectsMalformedResults() = kotlinx.coroutines.test.runTest {
        for (response in listOf("{}", """{"result":{}}""", "[null]", """["2015"]""", "[2015.5]", "[true]")) {
            val error = assertFailsWith<GarageException> {
                RpcGarageService { _, _ -> response }.years(testSession(), "Toyota", "Camry")
            }
            assertEquals(GarageException.Kind.InvalidResponse, error.kind)
        }
    }

    @Test
    fun modelLookupRejectsMalformedDataAndSupportsEmptyResults() = kotlinx.coroutines.test.runTest {
        for (response in listOf(
            "{}", """{"data":[{"id":1}]}""", """{"data":[{"id":"bad","name":"Camry"}]}""",
        )) {
            val service = RpcGarageService { _, _ -> response }
            val error = assertFailsWith<GarageException> {
                service.searchModels(testSession(), "Toyota")
            }
            assertEquals(GarageException.Kind.InvalidResponse, error.kind)
        }
        val service = RpcGarageService { _, _ -> """{"data":[]}""" }
        assertEquals(emptyList(), service.searchModels(testSession(), "Toyota"))
    }

    @Test
    fun modelsAreRequestedBySelectedMakeName() = kotlinx.coroutines.test.runTest {
        var method: String? = null
        var params: JsonObject? = null
        val service = RpcGarageService { calledMethod, paramsJson ->
            method = calledMethod
            params = Json.parseToJsonElement(paramsJson) as JsonObject
            """{"data":[{"id":10,"name":"Corolla"},{"id":11,"name":"Camry"}]}"""
        }
        val models = service.searchModels(testSession(), "Toyota")
        assertEquals("car.oemmakemodels_index_get", method)
        assertEquals("Toyota", params?.get("make")?.jsonPrimitive?.content)
        assertEquals("t", params?.get("access_token")?.jsonPrimitive?.content)
        assertEquals(setOf("make", "access_token"), params?.keys)
        assertEquals(listOf("Corolla", "Camry"), models.map { it.name })
        assertEquals(listOf("10", "11"), models.map { it.id })
    }

    @Test
    fun makeSearchHasNoBusinessParametersAndParsesResponse() = kotlinx.coroutines.test.runTest {
        var method: String? = null
        var params: JsonObject? = null
        val service = RpcGarageService { calledMethod, paramsJson ->
            method = calledMethod
            params = Json.parseToJsonElement(paramsJson) as JsonObject
            """{"data":[{"id":42,"name":"Toyota"},{"id":7,"name":"Ford"}]}"""
        }

        val makes = service.searchMakes(testSession())

        assertEquals("car.makes_index2_get", method)
        assertEquals("t", params?.get("access_token")?.jsonPrimitive?.content)
        assertEquals(setOf("access_token"), params?.keys)
        assertEquals(listOf("42", "7"), makes.map { it.id })
        assertEquals(listOf("Toyota", "Ford"), makes.map { it.name })
        assertEquals(
            Json.parseToJsonElement("""{"id":42,"name":"Toyota"}""").jsonObject,
            makes.first().rawData,
        )
    }

    @Test
    fun makeSearchRejectsMalformedResponsesAndAcceptsEmptyList() = kotlinx.coroutines.test.runTest {
        for (response in listOf(
            "{}", """{"data":{}}""", """{"data":[{"name":"Toyota"}]}""",
            """{"data":[{"id":1,"name":""}]}""", """{"data":[{"id":"invalid","name":"Toyota"}]}""",
        )) {
            val service = RpcGarageService { _, _ -> response }
            val error = assertFailsWith<GarageException> { service.searchMakes(testSession()) }
            assertEquals(GarageException.Kind.InvalidResponse, error.kind)
        }
        val service = RpcGarageService { _, _ -> """{"data":[]}""" }
        assertEquals(emptyList(), service.searchMakes(testSession()))
    }

    @Test
    fun vinDecoderRejectsMissingOrInvalidVehicleIdentity() = kotlinx.coroutines.test.runTest {
        for (response in listOf(
            "{}",
            """{"make":"Toyota","model":"","year":2020}""",
            """{"make":"Toyota","model":"Camry","year":"2020"}""",
            """{"make":"Toyota","model":"Camry","year":0}""",
        )) {
            assertFailsWith<GarageException> {
                RpcGarageService { _, _ -> response }.decodeVin(testSession(), "1GTG6CEN0L1139305")
            }
        }
    }

    @Test
    fun vinDecoderUsesRequestedMethodAndParameters() = kotlinx.coroutines.test.runTest {
        var method: String? = null
        var params: JsonObject? = null
        val service = RpcGarageService { calledMethod, paramsJson ->
            method = calledMethod
            params = Json.parseToJsonElement(paramsJson) as JsonObject
            """{"make":"Toyota","model":"Camry","year":2020,"specs":{},"trims":[]}"""
        }

        val decoded = service.decodeVin(testSession(), "1GTG6CEN0L1139305")
        assertEquals("Toyota", decoded.make)
        assertEquals("Camry", decoded.model)
        assertEquals(2020, decoded.year)

        assertEquals("car.vindecoder_index_get", method)
        assertEquals("1GTG6CEN0L1139305", params?.get("vin")?.jsonPrimitive?.content)
        assertEquals("yes", params?.get("verbose")?.jsonPrimitive?.content)
        assertEquals("yes", params?.get("all_trims")?.jsonPrimitive?.content)
        assertEquals("t", params?.get("access_token")?.jsonPrimitive?.content)
    }
}
