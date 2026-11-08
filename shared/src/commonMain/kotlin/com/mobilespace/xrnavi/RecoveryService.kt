package com.mobilespace.xrnavi

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal interface RecoveryService {
    suspend fun requestReset(email: String)
    suspend fun confirmReset(resetToken: String, newPassword: String)
}

internal class RpcRecoveryService(
    private val call: suspend (method: String, paramsJson: String) -> String,
) : RecoveryService {
    override suspend fun requestReset(email: String) {
        call("auth.requestPasswordReset", buildJsonObject { put("email", email) }.toString())
    }

    override suspend fun confirmReset(resetToken: String, newPassword: String) {
        val params = buildJsonObject {
            put("reset_token", resetToken)
            put("new_password", newPassword)
        }
        call("auth.confirmPasswordReset", params.toString())
    }
}
