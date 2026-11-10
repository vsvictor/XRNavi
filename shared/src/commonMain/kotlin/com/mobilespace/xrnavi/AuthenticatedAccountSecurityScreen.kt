package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthenticatedAccountSecurityScreen(
    profile: AuthenticatedProfile,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().testTag("authenticated-account-security"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("account-security-back")) {
                AccountIcon(AccountIcon.Back, contentDescription = "Назад до профілю")
            }
            SectionTitle("Безпека та особисті дані")
        }
        AuthenticatedProfileDetails(profile)
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().testTag("account-security-logout"),
        ) {
            Text("Вийти з акаунта")
        }
    }
}
