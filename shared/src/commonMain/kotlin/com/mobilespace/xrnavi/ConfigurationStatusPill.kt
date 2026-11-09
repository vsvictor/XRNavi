package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ConfigurationStatusPill(
    text: String,
    selected: Boolean = false,
    applied: Boolean = false,
    unavailable: Boolean = false
) {
    val background = when {
        unavailable -> MaterialTheme.colorScheme.surfaceVariant
        applied -> MaterialTheme.colorScheme.secondaryContainer
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when {
        unavailable -> MaterialTheme.colorScheme.onSurfaceVariant
        applied -> MaterialTheme.colorScheme.onSecondaryContainer
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = background, shape = RoundedCornerShape(16.dp)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = content,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
