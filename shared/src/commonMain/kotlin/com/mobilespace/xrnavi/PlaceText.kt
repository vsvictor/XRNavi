package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PlaceText(name: String?, placeholder: String, modifier: Modifier) {
    Text(
        name ?: placeholder,
        modifier.padding(start = 10.dp, end = 8.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 13.sp,
        fontWeight = if (name == null) FontWeight.Normal else FontWeight.SemiBold,
        color = if (name == null) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
    )
}
