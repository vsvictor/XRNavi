package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import xrnavi.shared.generated.resources.*

@Composable
internal fun isDarkXrnaviTheme(): Boolean =
    MaterialTheme.colorScheme.background == Color(0xFF0B1220)
