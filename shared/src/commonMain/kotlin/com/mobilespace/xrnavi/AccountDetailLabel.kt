package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier

@Composable
internal fun AccountDetailLabel(
    label: String,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Text(
        label,
        modifier = modifier,
        fontSize = if (emphasized) 14.sp else 12.sp,
        lineHeight = if (emphasized) 18.sp else 16.sp,
        fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
        color = if (emphasized) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
