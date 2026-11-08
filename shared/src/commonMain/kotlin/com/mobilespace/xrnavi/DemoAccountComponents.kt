package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun accountSurfaceColor(): Color =
    if (MaterialTheme.colorScheme.background.red < 0.5f) Color(0xFF070D17)
    else MaterialTheme.colorScheme.surface

@Composable
internal fun accountTonalColor(): Color =
    if (MaterialTheme.colorScheme.background.red < 0.5f) accountSurfaceColor()
    else MaterialTheme.colorScheme.surfaceVariant
