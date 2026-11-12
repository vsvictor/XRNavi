package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AccountAction(title: String, status: String, onClick: () -> Unit) {
    AccountAction(title, status, Modifier, onClick)
}

@Composable
internal fun AccountAction(
    title: String,
    status: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = accountTonalColor()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (status.isNotEmpty()) Text(
                    status, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AccountIcon(AccountIcon.ChevronRight, Modifier.size(18.dp))
        }
    }
}
