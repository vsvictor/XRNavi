package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthenticatedProfileActions(
    layout: AccountLayout,
    onOpenSecurity: () -> Unit,
    onOpenFavorites: () -> Unit,
) {
    val security: @Composable () -> Unit = {
        Surface(
            color = accountTonalColor(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
                .clickable(role = Role.Button, onClick = onOpenSecurity)
                .testTag("authenticated-profile-security"),
        ) {
            Column(
                Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Безпека та особисті дані", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Переглянути особисті дані й завершити сеанс.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    val favorites: @Composable () -> Unit = {
        Surface(
            color = accountTonalColor(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
                .clickable(role = Role.Button, onClick = onOpenFavorites)
                .testTag("authenticated-profile-favorites"),
        ) {
            Column(
                Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Обране", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Збережені місця на цьому пристрої.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (layout == AccountLayout.PhonePortrait || layout == AccountLayout.PhoneLandscape) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1.3f)) { security() }
            Column(Modifier.weight(1f)) { favorites() }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            security()
            favorites()
        }
    }
}
