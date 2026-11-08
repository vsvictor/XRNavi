package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalButton(
    title: String, secondary: Boolean = false, destructive: Boolean = false,
    enabled: Boolean = true, onClick: () -> Unit
) {
    Button(
        onClick, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                destructive -> MaterialTheme.colorScheme.error
                secondary -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primary
            },
            contentColor = when {
                destructive -> MaterialTheme.colorScheme.onError
                secondary -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onPrimary
            }
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) { Text(title, fontSize = 14.sp) }
}
