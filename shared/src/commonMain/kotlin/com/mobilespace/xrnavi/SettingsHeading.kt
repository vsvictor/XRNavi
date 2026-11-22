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
internal fun SettingsHeading(text: String, layout: AccountLayout) {
    if (layout == AccountLayout.PhonePortrait) {
        Text(
            text, fontWeight = FontWeight.Medium, fontSize = 11.sp,
            lineHeight = 13.sp, letterSpacing = 0.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() }
        )
    } else {
        CardHeading(text)
    }
}
