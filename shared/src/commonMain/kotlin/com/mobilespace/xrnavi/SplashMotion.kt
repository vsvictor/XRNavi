package com.mobilespace.xrnavi

import androidx.compose.animation.core.CubicBezierEasing

internal data class SplashMotion(
    val scale: Float,
    val alpha: Float,
    val translationX: Float,
    val translationY: Float,
    val orbitDegrees: Float,
)

internal fun splashMotion(elapsedMillis: Long, tablet: Boolean): SplashMotion {
    val elapsed = elapsedMillis.coerceIn(0, SplashDurationMillis).toFloat()
    val enter = CubicBezierEasing(0f, 0f, 0.58f, 1f)
        .transform((elapsed / 480f).coerceIn(0f, 1f))
    val settle = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
        .transform(((elapsed - 480f) / 240f).coerceIn(0f, 1f))
    val appearing = CubicBezierEasing(0f, 0f, 0.58f, 1f)
        .transform((elapsed / 450f).coerceIn(0f, 1f))
    val width = if (tablet) 280f else 210f
    val height = if (tablet) 196.19258f else 160.64444f
    return SplashMotion(
        scale = if (elapsed < 480f) 0.9f + 0.12f * enter else 1.02f - 0.02f * settle,
        alpha = 0.25f + 0.75f * appearing,
        translationX = if (elapsed < 480f) width * (0.05f - 0.06f * enter)
            else -width * 0.01f * (1f - settle),
        translationY = if (elapsed < 480f) {
            (height * 0.05f + 12f) * (1f - enter) - (height * 0.01f + 1f) * enter
        } else {
            -(height * 0.01f + 1f) * (1f - settle)
        },
        orbitDegrees = 360f * ((elapsed - 200f) / 2_000f).coerceIn(0f, 1f),
    )
}
