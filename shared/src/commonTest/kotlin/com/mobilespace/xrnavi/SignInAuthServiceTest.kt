package com.mobilespace.xrnavi

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SignInAuthServiceTest {
    @Test
    fun sendsExactLoginContractAndSafelyEncodesInput() = runTest {
        val email = "test+\"quoted\"@example.org"
        val password = " spaces \" \\ \n українська "
        val service = RpcSignInAuthService { method, params ->
            assertEquals("auth.login", method)
            assertEquals(buildJsonObject {
                put("email", email)
                put("password", password)
                put("grant_type", "password")
            }, Json.parseToJsonElement(params))
            """{"access_token":"access","refresh_token":"refresh","token_type":"Bearer","expires_in":3600,"extra":true}"""
        }
        val session = service.login(email, password)
        assertEquals("access", session.accessToken)
        assertEquals("refresh", session.refreshToken)
        assertEquals("Bearer", session.tokenType)
        assertEquals(3600uL, session.expiresInSeconds)
    }

    @Test
    fun rejectsMalformedMissingAndWronglyTypedTokenFields() = runTest {
        val valid = """{"access_token":"a","refresh_token":"r","token_type":"Bearer","expires_in":3600}"""
        val invalid = listOf(
            "not JSON", "null", "[]", "{}",
            valid.replace(""""a"""", "12"),
            valid.replace(""""r"""", """""""),
            valid.replace(""""Bearer"""", "true"),
            valid.replace("3600", "\"3600\""),
            valid.replace("3600", "0"),
            valid.replace("3600", "-1"),
            valid.replace("3600", "1.5"),
            """{"result":$valid}""",
        )
        for (response in invalid) {
            val service = RpcSignInAuthService { _, _ -> response }
            assertFailsWith<InvalidAuthResponseException> {
                service.login("test@example.org", "password")
            }
        }
    }

    @Test
    fun propagatesRpcFailureInsteadOfManufacturingSession() = runTest {
        val service = RpcSignInAuthService { _, _ -> throw IllegalStateException("Unauthorized") }
        assertFailsWith<IllegalStateException> {
            service.login("test@example.org", "password")
        }
    }
}
