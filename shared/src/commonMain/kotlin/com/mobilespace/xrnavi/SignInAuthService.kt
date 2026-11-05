package com.mobilespace.xrnavi

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Deliberately not a data class: generated toString/copy must not expose tokens.
internal class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: ULong,
)

internal class InvalidAuthResponseException : Exception("Invalid auth.login response")

internal fun interface SignInAuthService {
    suspend fun login(email: String, password: String): AuthSession
}

internal class RpcSignInAuthService(
    private val call: suspend (method: String, paramsJson: String) -> String,
) : SignInAuthService {
    override suspend fun login(email: String, password: String): AuthSession {
        val params = buildJsonObject {
            put("email", email)
            put("password", password)
            put("grant_type", "password")
        }
        val result = call("auth.login", params.toString())
        val fields = try {
            Json.parseToJsonElement(result) as? JsonObject
                ?: throw InvalidAuthResponseException()
        } catch (_: SerializationException) {
            throw InvalidAuthResponseException()
        }
        fun requiredString(name: String): String {
            val value = fields[name] as? JsonPrimitive
            return value?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }
                ?: throw InvalidAuthResponseException()
        }
        val expiry = (fields["expires_in"] as? JsonPrimitive)
            ?.takeUnless { it.isString }?.content?.toULongOrNull()
            ?.takeIf { it > 0uL } ?: throw InvalidAuthResponseException()
        return AuthSession(
            accessToken = requiredString("access_token"),
            refreshToken = requiredString("refresh_token"),
            tokenType = requiredString("token_type"),
            expiresInSeconds = expiry,
        )
    }
}
