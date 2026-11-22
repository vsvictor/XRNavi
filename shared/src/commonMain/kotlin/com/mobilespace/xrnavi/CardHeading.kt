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
internal fun CardHeading(text: String) {
    Text(
        text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
        lineHeight = 20.sp, letterSpacing = 0.1.sp,
        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() }
    )
}
