package com.mobilespace.xrnavi

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DeviceIdLoginTest {
    private val response =
        """{"access_token":"a","refresh_token":"r","token_type":"Bearer","expires_in":3600}"""

    @Test
    fun loginSendsDeviceIdObject() = runTest {
        var sent: JsonObject? = null
        val service = RpcSignInAuthService(
            deviceIdProvider = { "device-42" },
            osInfoProvider = { OsInfo("TestOS", "1.2") },
        ) { _, params ->
            sent = Json.parseToJsonElement(params) as JsonObject
            response
        }
        service.login("a@b.c", "pw")
        val device = sent!!["device"] as JsonObject
        assertEquals("device-42", device["deviceId"]!!.jsonPrimitive.content)
        val os = device["os"] as JsonObject
        assertEquals("TestOS", os["name"]!!.jsonPrimitive.content)
        assertEquals("1.2", os["version"]!!.jsonPrimitive.content)
    }

    @Test
    fun storedDeviceIdIsGeneratedOnceAndReused() {
        val storage = MemorySecureSessionStorage()
        val first = StoredDeviceIdProvider(storage).deviceId()
        assertEquals(first, StoredDeviceIdProvider(storage).deviceId())
        assertNotEquals(first, StoredDeviceIdProvider(MemorySecureSessionStorage()).deviceId())
    }
}
