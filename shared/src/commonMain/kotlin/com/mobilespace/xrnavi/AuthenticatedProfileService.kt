package com.mobilespace.xrnavi

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.put

internal data class AuthenticatedProfile(
    val id: String,
    val email: String,
    val createdAt: String,
    val roles: List<String>,
    val fields: JsonObject,
    val contacts: List<ProfileContact> = listOf(ProfileContact("email", email)),
) {
    override fun toString(): String = "AuthenticatedProfile"
}

internal class InvalidProfileResponseException : Exception("Invalid auth.getProfile response")

internal fun interface AuthenticatedProfileService {
    suspend fun getProfile(session: AuthSession): AuthenticatedProfile
}

internal class RpcAuthenticatedProfileService(
    private val call: suspend (method: String, paramsJson: String) -> String,
) : AuthenticatedProfileService {
    override suspend fun getProfile(session: AuthSession): AuthenticatedProfile {
        val result = call("auth.getProfile", buildJsonObject {
            put("access_token", session.accessToken)
        }.toString())
        val user = try {
            Json.parseToJsonElement(result) as? JsonObject
                ?: throw InvalidProfileResponseException()
        } catch (_: SerializationException) {
            throw InvalidProfileResponseException()
        }
        fun requiredString(name: String): String =
            (user[name] as? JsonPrimitive)?.takeIf { it.isString }
                ?.content?.takeIf { it.isNotBlank() } ?: throw InvalidProfileResponseException()

        val roles = (user["roles"] as? JsonArray)?.map {
            (it as? JsonPrimitive)?.takeIf { role -> role.isString }?.content
                ?: throw InvalidProfileResponseException()
        } ?: throw InvalidProfileResponseException()
        val fields = user["profile"] as? JsonObject ?: throw InvalidProfileResponseException()
        listOf("first_name", "last_name", "middle_name", "birth_date", "account_type").forEach { key ->
            if (key in fields && (fields[key] as? JsonPrimitive)?.isString != true) {
                throw InvalidProfileResponseException()
            }
        }
        if ("notifications_enabled" in fields) {
            val notifications = fields["notifications_enabled"] as? JsonPrimitive
            if (notifications == null || notifications.isString || notifications.booleanOrNull == null) {
                throw InvalidProfileResponseException()
            }
        }
        val email = requiredString("email")
        val contacts = if ("contacts" !in fields) {
            listOf(ProfileContact("email", email))
        } else {
            val entries = fields["contacts"] as? JsonArray ?: throw InvalidProfileResponseException()
            entries.map { entry ->
                val contact = entry as? JsonObject ?: throw InvalidProfileResponseException()
                fun contactString(key: String): String =
                    (contact[key] as? JsonPrimitive)?.takeIf { it.isString }
                        ?.content?.takeIf { it.isNotBlank() } ?: throw InvalidProfileResponseException()
                ProfileContact(contactString("type"), contactString("value"))
            }
        }
        return AuthenticatedProfile(
            requiredString("id"), email, requiredString("created_at"), roles, fields, contacts,
        )
    }
}
