package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Methods that obtain or recover credentials and therefore are sent without a token.
 * Every other server call is authorized: the token is validated (and refreshed) first.
 */
internal val publicRpcMethods = setOf(
    "auth.login",
    "auth.register",
    "auth.refresh",
    "auth.requestPasswordReset",
    "auth.confirmPasswordReset",
    "auth.recoverPassword",
)

internal class AuthorizedRpc(
    private val sessions: SessionManager,
    private val rawCall: suspend (method: String, paramsJson: String) -> String,
) {
    suspend fun call(method: String, paramsJson: String): String {
        if (method in publicRpcMethods) return rawCall(method, paramsJson)
        val session = sessions.validSession()
        return try {
            rawCall(method, withToken(paramsJson, session.accessToken))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (rejected: Exception) {
            if (!rejected.isAuthRejection() || rejected is SessionExpiredException) throw rejected
            val renewed = sessions.refreshAfterRejection(session.accessToken)
            rawCall(method, withToken(paramsJson, renewed.accessToken))
        }
    }

    private fun withToken(paramsJson: String, token: String): String {
        val params = Json.parseToJsonElement(paramsJson) as? JsonObject ?: JsonObject(emptyMap())
        return buildJsonObject {
            params.forEach { (key, value) -> if (key != "access_token") put(key, value) }
            put("access_token", token)
        }.toString()
    }
}
