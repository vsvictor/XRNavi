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
        assertEquals(AuthenticatedAccountType.Personal, profile.accountType)
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
        assertEquals(AuthenticatedAccountType.Unknown, profile.accountType)
        assertEquals("Не вказано", authenticatedProfileField(profile, "first_name"))
    }

    @Test
    fun readsCorporateAccountTypeAndKeepsUnknownTypesExplicit() = runTest {
        val corporate = RpcAuthenticatedProfileService { _, _ ->
            response.replace(""""account_type":"private"""", """"account_type":"corporate"""")
        }.getProfile(session())
        assertEquals(AuthenticatedAccountType.Corporate, corporate.accountType)

        val unknown = RpcAuthenticatedProfileService { _, _ ->
            response.replace(""""account_type":"private"""", """"account_type":"future"""")
        }.getProfile(session())
        assertEquals(AuthenticatedAccountType.Unknown, unknown.accountType)
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
            response.replace(""""account_type":"private"""", """"account_type":true"""),
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

    @Test
    fun readsStructuredContactsWithoutChangingLoginIdentity() = runTest {
        val types = listOf("phone", "email", "telegram", "viber", "facebook", "x", "instagram", "tiktok",
            "whatsapp", "signal", "linkedin", "website")
        val entries = types.joinToString(",") {
            """{"type":"$it","value":"contact-$it"}"""
        }
        val profile = RpcAuthenticatedProfileService { _, _ ->
            response.replace(""""first_name":"Олена"""", """"contacts":[$entries],"first_name":"Олена"""")
        }.getProfile(session())
        assertEquals(types, profile.contacts.map { it.type })
        assertEquals(types.map { "contact-$it" }, profile.contacts.map { it.value })
        assertEquals("stored@example.org", profile.email)
        assertFalse(profile.contacts.first().toString().contains("contact-phone"))
    }

    @Test
    fun distinguishesLegacyContactsFromExplicitEmptyListAndKeepsFutureTypes() = runTest {
        val legacy = RpcAuthenticatedProfileService { _, _ -> response }.getProfile(session())
        assertEquals(listOf(ProfileContact("email", legacy.email)), legacy.contacts)
        val empty = RpcAuthenticatedProfileService { _, _ ->
            response.replace(""""first_name":"Олена"""", """"contacts":[],"first_name":"Олена"""")
        }.getProfile(session())
        assertEquals(emptyList(), empty.contacts)
        val future = RpcAuthenticatedProfileService { _, _ ->
            response.replace(
                """"first_name":"Олена"""",
                """"contacts":[{"type":"website","value":"https://example.invalid"}],"first_name":"Олена"""",
            )
        }.getProfile(session())
        assertEquals(listOf(ProfileContact("website", "https://example.invalid")), future.contacts)
    }

    @Test
    fun rejectsMalformedContactsRatherThanHidingThem() = runTest {
        listOf(
            "null", "{}", "\"[]\"", "[null]", "[{}]",
            """[{"type":"email","value":123}]""",
            """[{"type":"","value":"address"}]""",
            """[{"type":"phone","value":" "}]""",
        ).forEach { contacts ->
            assertFailsWith<InvalidProfileResponseException> {
                RpcAuthenticatedProfileService { _, _ ->
                    response.replace(""""first_name":"Олена"""", """"contacts":$contacts,"first_name":"Олена"""")
                }.getProfile(session())
            }
        }
    }

    private fun session() = AuthSession("test-access", "test-refresh", "Bearer", 3600uL)

    private val response = """{"id":"backend-id","email":"stored@example.org","created_at":"2026-01-01T00:00:00+00:00","roles":["driver","admin"],"profile":{"first_name":"Олена","last_name":"Коваль","birth_date":"2000-02-03T00:00:00+00:00","account_type":"private","notifications_enabled":false},"extra":true}"""
}
