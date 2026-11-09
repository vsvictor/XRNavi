package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalTitle(text: String, large: Boolean = false) {
    Text(
        text,
        fontFamily = if (large) rememberRobotoCondensedFontFamily() else rememberInterFontFamily(),
        fontSize = if (large) 24.sp else 14.sp,
        lineHeight = if (large) 28.sp else 20.sp,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.semantics { heading() })
}
