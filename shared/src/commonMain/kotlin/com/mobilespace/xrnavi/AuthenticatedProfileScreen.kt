package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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

@Composable
internal fun AuthenticatedProfileScreen(profile: AuthenticatedProfile) {
    Text("Дані акаунта з сервера", style = MaterialTheme.typography.titleLarge)
    Text("ID: ${profile.id}", Modifier.testTag("authenticated-profile-id"))
    Text("Пошта: ${profile.email}", Modifier.testTag("authenticated-profile-email"))
    Text("Створено: ${profile.createdAt}")
    Text(
        "Ролі: ${profile.roles.joinToString().ifEmpty { "Немає призначених ролей" }}",
        Modifier.testTag("authenticated-profile-roles"),
    )
    listOf(
        "first_name" to "Ім’я",
        "last_name" to "Прізвище",
        "middle_name" to "По батькові",
        "birth_date" to "Дата народження",
        "notifications_enabled" to "Сповіщення акаунта",
    ).forEach { (key, label) ->
        Text("$label: ${authenticatedProfileField(profile, key)}")
    }
    Text("Профіль лише для перегляду. Редагування профілю ще не підключено.")
}
