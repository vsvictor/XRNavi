package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal interface RegistrationService {
    suspend fun register(state: RegistrationState)
}

internal fun RegistrationState.registerJson(): JsonObject = buildJsonObject {
    put("email", email.trim())
    put("password", password)
    put("profile", buildJsonObject {
        put("first_name", name.trim())
        put("last_name", lastName.trim())
        if (middleName.isNotBlank()) put("middle_name", middleName.trim())
        put("account_type", membership.accountType)
    })
}

internal class RpcRegistrationService(
    private val call: suspend (method: String, paramsJson: String) -> String,
) : RegistrationService {
    override suspend fun register(state: RegistrationState) {
        call("auth.register", state.registerJson().toString())
    }
}

internal const val registrationNetworkMessage =
    "Не вдалося зв’язатися із сервером. Спробуйте ще раз"
internal const val registrationRejectedMessage =
    "Сервер відхилив дані. Перевірте поля та надійність пароля"
internal const val registrationExistsMessage =
    "Аккаунт із такою поштою вже існує. Увійдіть або відновіть пароль"

internal fun Throwable.registrationFailureMessage(): String {
    val text = message.orEmpty()
    return when {
        text.contains("-32602") -> registrationRejectedMessage
        text.contains("already", ignoreCase = true) || text.contains("exists", ignoreCase = true) ||
            text.contains("duplicate", ignoreCase = true) -> registrationExistsMessage
        else -> registrationNetworkMessage
    }
}
