package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal const val heroSlogan = "XR - НОВИЙ ФОРМАТ ВЗАЄМОДІЇ"
private val heroTitleSizingSamples = listOf(heroSlogan, "ВІДНОВЛЕННЯ ПАРОЛЯ", "РЕЄСТРАЦІЯ")

/** Спільний розмір шрифта заголовків: найдовший зразок має вміститися в один рядок. */
@Composable
internal fun rememberHeroTitleSize(textWidth: Dp, horizontalLayout: Boolean): TextUnit {
    val baseSize = if (horizontalLayout) 28f else 38f
    val fontFamily = rememberRobotoCondensedFontFamily()
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val availablePx = with(density) { textWidth.toPx() }
    // Вимірюємо лише раз на базовому розмірі, щоб на кожному кадрі повороту не перемірювати текст.
    val baseWidth = remember(fontFamily, baseSize) {
        heroTitleSizingSamples.maxOf { sample ->
            measurer.measure(
                text = sample,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = baseSize.sp,
                    letterSpacing = (-0.45).sp
                ),
                softWrap = false
            ).size.width
        }
    }
    val scale = (availablePx / baseWidth * 0.98f).coerceIn(0.1f, 1f)
    return (baseSize * scale).sp
}
