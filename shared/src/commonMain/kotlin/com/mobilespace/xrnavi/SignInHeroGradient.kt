package com.mobilespace.xrnavi

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal fun signInHeroGradient(dark: Boolean, horizontal: Boolean, reduced: Boolean = false): Brush =
    if (horizontal) {
        Brush.horizontalGradient(
            colorStops = if (dark) {
                arrayOf(
                    0f to Color(0x660B1220),
                    0.5f to Color.Transparent,
                    0.88f to Color(0xCC0B1220),
                    1f to Color(0xFF0B1220),
                )
            } else {
                arrayOf(
                    0f to Color(0x66F6F8FC),
                    0.45f to Color.Transparent,
                    0.88f to Color(0xF7FFFFFF),
                    1f to Color.White,
                )
            },
        )
    } else {
        Brush.verticalGradient(
            colorStops = if (dark) {
                if (reduced) {
                    arrayOf(
                        0f to Color(0x660B1220),
                        0.62f to Color.Transparent,
                        0.92f to Color(0xCC0B1220),
                        1f to Color(0xFF0B1220),
                    )
                } else {
                    arrayOf(
                        0f to Color(0x660B1220),
                        0.5f to Color.Transparent,
                        0.82f to Color(0xCC0B1220),
                        1f to Color(0xFF0B1220),
                    )
                }
            } else {
                if (reduced) {
                    arrayOf(
                        0f to Color(0xDDF6F8FC),
                        0.23f to Color.Transparent,
                        0.76f to Color.Transparent,
                        0.94f to Color(0xF7FFFFFF),
                        1f to Color.White,
                    )
                } else {
                    arrayOf(
                        0f to Color(0xDDF6F8FC),
                        0.23f to Color.Transparent,
                        0.56f to Color.Transparent,
                        0.78f to Color(0xF7FFFFFF),
                        1f to Color.White,
                    )
                }
            },
        )
    }
