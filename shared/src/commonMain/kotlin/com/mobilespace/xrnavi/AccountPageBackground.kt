package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
internal fun Modifier.accountPageBackground(): Modifier {
    val background = MaterialTheme.colorScheme.background
    return background(
        Brush.verticalGradient(
            if (isDarkXrnaviTheme()) listOf(background, background)
            else listOf(Color.White, background),
        ),
    )
}
