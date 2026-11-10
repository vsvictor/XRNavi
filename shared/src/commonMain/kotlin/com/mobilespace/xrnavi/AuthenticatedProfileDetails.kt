package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun AuthenticatedProfileDetails(profile: AuthenticatedProfile) {
    AccountCard(tonal = true) {
        CardHeading("Особисті дані")
        val fullName = listOf("last_name", "first_name", "middle_name")
            .map { authenticatedProfileField(profile, it) }
            .filter { it != "Не вказано" }
            .joinToString(" ")
            .ifBlank { "Не вказано" }
        AccountBody("Прізвище, ім’я та по батькові: $fullName")
        AccountBody(
            "Дата народження: ${
                formatAuthenticatedProfileDate(authenticatedProfileField(profile, "birth_date"))
            }",
        )
        AccountBody("Сповіщення: ${authenticatedProfileField(profile, "notifications_enabled")}")
        CardHeading("Дані акаунта")
        AccountBody("ID: ${profile.id}")
        AccountBody("Створено: ${formatAuthenticatedProfileDate(profile.createdAt)}")
        AccountBody("Ролі: ${profile.roles.joinToString().ifEmpty { "немає призначених ролей" }}")
    }
}
