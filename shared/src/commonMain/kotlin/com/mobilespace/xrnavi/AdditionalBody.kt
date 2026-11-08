package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalBody(text: String) {
    Text(
        text, fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
