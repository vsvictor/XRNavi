package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun SecurityPersonalCard(profile: AuthenticatedProfile) {
    ProfileSectionCard {
        CardHeading("Особисті дані")
        val fullName = listOf("last_name", "first_name", "middle_name")
            .map { authenticatedProfileField(profile, it) }
            .filter { it != "Не вказано" }
            .joinToString(" ")
            .ifBlank { "Не вказано" }
        AccountDetailField("ПІБ", fullName, divider = false)
        profile.contacts.forEach { ProfileContactRow(it) }
        if (profile.contacts.isEmpty()) {
            Text(
                "Контакти не додано",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
