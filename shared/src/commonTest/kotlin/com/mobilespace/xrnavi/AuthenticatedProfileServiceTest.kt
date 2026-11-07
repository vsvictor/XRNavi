package com.mobilespace.xrnavi

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class AuthenticatedProfileServiceTest {
    @Test
    fun sendsOnlyAccessTokenAndReadsActualUserResponseAndTypedProfile() = runTest {
        val service = RpcAuthenticatedProfileService { method, params ->
            assertEquals("auth.getProfile", method)
            assertEquals(buildJsonObject { put("access_token", "test-access") },
                Json.parseToJsonElement(params))
            response
        }
        val profile = service.getProfile(session())
        assertEquals("backend-id", profile.id)
        assertEquals("stored@example.org", profile.email)
        assertEquals("2026-01-01T00:00:00+00:00", profile.createdAt)
        assertEquals(listOf("driver", "admin"), profile.roles)
        assertEquals(JsonPrimitive(false), profile.fields["notifications_enabled"])
        assertEquals("Олена", authenticatedProfileField(profile, "first_name"))
        assertEquals("Вимкнено", authenticatedProfileField(profile, "notifications_enabled"))
        assertEquals("Не вказано", authenticatedProfileField(profile, "middle_name"))
        assertFalse(profile.toString().contains(profile.email))
    }

    @Test
    fun acceptsEmptyRolesAndMissingOptionalProfileFieldsWithoutFixtures() = runTest {
        val profile = RpcAuthenticatedProfileService { _, _ ->
            """{"id":"backend-id","email":"stored@example.org","created_at":"2026-01-01","roles":[],"profile":{}}"""
        }.getProfile(session())
        assertEquals(emptyList(), profile.roles)
        assertEquals(JsonObject(emptyMap()), profile.fields)
        assertEquals("Не вказано", authenticatedProfileField(profile, "first_name"))
    }

    @Test
    fun rejectsMalformedOrIncompleteUserResponseRatherThanUsingLoginEmailOrDemo() = runTest {
        val invalid = listOf(
            "not JSON", "null", "[]", "{}", """{"result":$response}""",
            response.replace(""""backend-id"""", "123"),
            response.replace(""""stored@example.org"""", """""""),
            response.replace("""["driver","admin"]""", """["driver",123]"""),
            response.replace(""""roles":["driver","admin"],""", ""),
            response.replace(""""profile":{""", """"other":{"""),
            response.replace(""""first_name":"Олена"""", """"first_name":null"""),
            response.replace(""""notifications_enabled":false""", """"notifications_enabled":"false""""),
        )
        invalid.forEach { result ->
            assertFailsWith<InvalidProfileResponseException> {
                RpcAuthenticatedProfileService { _, _ -> result }.getProfile(session())
            }
        }
    }

    @Test
    fun rpcFailureDoesNotManufactureProfile() = runTest {
        assertFailsWith<IllegalStateException> {
            RpcAuthenticatedProfileService { _, _ -> throw IllegalStateException("RPC failed") }
                .getProfile(session())
        }
    }

    private fun session() = AuthSession("test-access", "test-refresh", "Bearer", 3600uL)

    private val response = """{"id":"backend-id","email":"stored@example.org","created_at":"2026-01-01T00:00:00+00:00","roles":["driver","admin"],"profile":{"first_name":"Олена","last_name":"Коваль","birth_date":"2000-02-03T00:00:00+00:00","notifications_enabled":false},"extra":true}"""
}
