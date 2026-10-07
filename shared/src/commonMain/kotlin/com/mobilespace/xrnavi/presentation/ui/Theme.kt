package com.mobilespace.xrnavi.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

internal val ApexBackground = Color(0xFF090C10)
internal val ApexSurface = Color(0xFF14191F)
internal val ApexBorder = Color(0xFF303741)
internal val ApexText = Color(0xFFF5F6F8)
internal val ApexMuted = Color(0xFFA2ACB9)
internal val ApexSubtle = Color(0xFF6C7888)
internal val ApexRed = Color(0xFFE64242)
internal val ApexLinkRed = Color(0xFFF45151)

@Composable
internal fun ApexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = ApexBackground,
            surface = ApexSurface,
            primary = ApexRed,
            onBackground = ApexText,
            onSurface = ApexText,
        ),
        content = content,
    )
}

