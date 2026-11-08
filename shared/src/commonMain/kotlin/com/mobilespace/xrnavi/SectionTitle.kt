package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
internal fun SectionTitle(text: String, fontSize: Int = 24, lineHeight: Int = 30) {
    Text(
        text, fontFamily = rememberRobotoCondensedFontFamily(), fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp, lineHeight = lineHeight.sp,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() }
    )
}
