package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

internal fun authenticatedProfileField(profile: AuthenticatedProfile, key: String): String {
    val value = profile.fields[key] as? JsonPrimitive
    return when {
        key == "notifications_enabled" -> when (value?.booleanOrNull) {
            true -> "Увімкнено"
            false -> "Вимкнено"
            null -> "Не вказано"
        }
        value?.isString == true -> value.content.takeIf { it.isNotBlank() } ?: "Не вказано"
        else -> "Не вказано"
    }
}
