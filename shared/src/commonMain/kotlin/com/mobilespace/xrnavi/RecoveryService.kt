package com.mobilespace.xrnavi

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal interface RecoveryService {
    suspend fun requestReset(email: String)
    suspend fun confirmReset(email: String, resetToken: String, newPassword: String)
}

private fun String.isResetCode() = length == 6 && all { it in '0'..'9' }

internal class RpcRecoveryService(
    private val call: suspend (method: String, paramsJson: String) -> String,
) : RecoveryService {
    override suspend fun requestReset(email: String) {
        call("auth.requestPasswordReset", buildJsonObject { put("email", email) }.toString())
    }

    override suspend fun confirmReset(email: String, resetToken: String, newPassword: String) {
        val params = buildJsonObject {
            if (resetToken.isResetCode()) {
                put("email", email.trim())
                put("code", resetToken)
            } else {
                put("reset_token", resetToken)
            }
            put("new_password", newPassword)
        }
        call("auth.confirmPasswordReset", params.toString())
    }
}
