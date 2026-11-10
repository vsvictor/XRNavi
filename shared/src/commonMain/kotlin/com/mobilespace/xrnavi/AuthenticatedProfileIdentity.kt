package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AuthenticatedProfileIdentity(profile: AuthenticatedProfile, layout: AccountLayout) {
    val firstName = authenticatedProfileField(profile, "first_name").takeUnless { it == "Не вказано" }
    val lastName = authenticatedProfileField(profile, "last_name").takeUnless { it == "Не вказано" }
    val middleName = authenticatedProfileField(profile, "middle_name").takeUnless { it == "Не вказано" }
    val displayName = listOfNotNull(lastName, firstName, middleName)
        .joinToString(" ")
        .ifBlank { "Ім’я не вказано" }
    val initials = listOfNotNull(firstName?.firstOrNull(), lastName?.firstOrNull())
        .joinToString("")
        .ifBlank { "?" }
    val accountType = profile.accountType

    Column(
        Modifier.fillMaxWidth().testTag("authenticated-profile-identity"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(
                    when (layout) {
                        AccountLayout.PhoneLandscape -> 44.dp
                        AccountLayout.TabletLandscape -> 80.dp
                        else -> 64.dp
                    },
                ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(initials, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    displayName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag("authenticated-profile-name"),
                )
                Text(
                    profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("authenticated-profile-email"),
                )
                Text(
                    "e-mail не підтверджено",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("authenticated-profile-email-status"),
                )
            }
            if (layout.tablet) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.widthIn(min = 176.dp).heightIn(min = 44.dp)
                        .testTag("authenticated-profile-edit"),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text("Редагувати профіль")
                }
            }
        }
        if (!layout.tablet) {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                    .testTag("authenticated-profile-edit"),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text("Редагувати профіль")
            }
        }
        Row(
            Modifier.fillMaxWidth()
                .background(accountTonalColor(), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf(
                AuthenticatedAccountType.Personal,
                AuthenticatedAccountType.Corporate,
            ).forEach { type ->
                val isSelected = type == accountType
                Surface(
                    Modifier.weight(1f).semantics {
                        selected = isSelected
                        contentDescription = "Тип акаунта: ${type.label}"
                    }.testTag(
                        if (type == AuthenticatedAccountType.Personal) {
                            "profile-account-type-private"
                        } else {
                            "profile-account-type-corporate"
                        },
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else accountSurfaceColor(),
                ) {
                    Text(
                        type.label,
                        Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
