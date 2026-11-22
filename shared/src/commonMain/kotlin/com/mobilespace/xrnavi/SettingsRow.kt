package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SettingsRow(
    title: String,
    description: String,
    value: String? = null,
    grouped: Boolean = true,
    locked: Boolean = false,
    icon: AccountIcon? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().testTag("settings-row-$title")
            .heightIn(min = if (grouped) 65.dp else 48.dp)
            .clip(RoundedCornerShape(if (grouped) 0.dp else 12.dp))
            .background(accountSurfaceColor())
            .then(
                if (onClick != null) Modifier.clickable(
                    role = Role.Button,
                    onClick = onClick
                ) else Modifier
            )
            .padding(horizontal = if (grouped) 14.dp else 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) AccountIcon(
            icon, Modifier.size(20.dp),
            tint = if (locked) accountPolicyColor() else null
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title, fontSize = 13.sp, lineHeight = 16.sp,
                letterSpacing = 0.sp, fontWeight = FontWeight.Medium
            )
            Text(
                description,
                fontSize = 11.sp,
                lineHeight = if (grouped) 14.sp else 16.sp,
                letterSpacing = 0.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (value != null) Text(
            value,
            fontSize = if (grouped) 11.sp else 12.sp,
            lineHeight = if (grouped) 13.sp else 16.sp,
            letterSpacing = 0.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (onClick != null) AccountIcon(
            AccountIcon.ChevronRight,
            Modifier.size(16.dp)
        )
        if (locked) AccountIcon(
            AccountIcon.Lock,
            Modifier.size(15.dp),
            contentDescription = "Корпоративний параметр недоступний"
        )
    }
}
