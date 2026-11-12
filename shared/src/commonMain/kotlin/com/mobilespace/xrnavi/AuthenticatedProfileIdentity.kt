package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AuthenticatedProfileIdentity(profile: AuthenticatedProfile, layout: AccountLayout) {
    val firstName =
        authenticatedProfileField(profile, "first_name").takeUnless { it == "Не вказано" }
    val lastName = authenticatedProfileField(profile, "last_name").takeUnless { it == "Не вказано" }
    val middleName =
        authenticatedProfileField(profile, "middle_name").takeUnless { it == "Не вказано" }
    val displayName = listOfNotNull(firstName, middleName, lastName)
        .joinToString(" ")
        .ifBlank { "Ім’я не вказано" }
    val initials = listOfNotNull(firstName?.firstOrNull(), lastName?.firstOrNull())
        .joinToString("")
        .ifBlank { "?" }

    Surface(
        Modifier.fillMaxWidth().testTag("authenticated-profile-identity"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(if (layout == AccountLayout.TabletLandscape) 80.dp else 64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            initials,
                            fontFamily = rememberRobotoCondensedFontFamily(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        displayName,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = rememberRobotoCondensedFontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (layout.tablet) 32.sp else 28.sp,
                        lineHeight = if (layout.tablet) 38.sp else 34.sp,
                        modifier = Modifier.testTag("authenticated-profile-name")
                            .semantics { heading() },
                    )
                    AccountSingleLineText(
                        profile.email,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        ),
                        modifier = Modifier.testTag("authenticated-profile-email"),
                    )
                }
            }
        }
    }
}
