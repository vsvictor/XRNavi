package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun MapPlaceLabel(
    text: String,
    x: Dp,
    y: Dp,
    size: androidx.compose.ui.unit.TextUnit = 12.sp,
    strong: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier.offset(x = x, y = y),
        fontSize = size,
        fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal,
        color = if (strong) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
    )
}
