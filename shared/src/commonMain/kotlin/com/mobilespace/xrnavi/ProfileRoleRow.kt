package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ProfileRoleRow(role: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = when (role.lowercase()) {
            "driver" -> AccountIcon.Driver
            "admin", "administrator", "super_admin" -> AccountIcon.Admin
            "company_manager", "organization_manager", "fleet_manager" -> AccountIcon.CompanyManager
            "organizer", "event_organizer" -> AccountIcon.Organizer
            else -> AccountIcon.Role
        }
        AccountIcon(
            icon,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "Роль: $role",
        )
        Text(role, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
