package com.mobilespace.xrnavi

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF335CFF),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF526178),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF172033),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF172033),
    surfaceVariant = Color(0xFFEAEFF7),
    onSurfaceVariant = Color(0xFF526178),
    outline = Color(0xFF7A879C),
    outlineVariant = Color(0xFFD7DFEA),
    primaryContainer = Color(0xFFDAE2FF),
    onPrimaryContainer = Color(0xFF131C33),
    error = Color(0xFFA72A3D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9AAFFF),
    onPrimary = Color(0xFF101A35),
    secondary = Color(0xFFA8B5C7),
    onSecondary = Color(0xFF0B1220),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFF2F5FA),
    surface = Color(0xFF111B2B),
    onSurface = Color(0xFFF2F5FA),
    surfaceVariant = Color(0xFF152033),
    onSurfaceVariant = Color(0xFFA8B5C7),
    outline = Color(0xFF657791),
    outlineVariant = Color(0xFF2D3B50),
    primaryContainer = Color(0xFF24365B),
    onPrimaryContainer = Color(0xFFDDE4FF),
    error = Color(0xFFFF8A9A)
)

@Composable
internal fun XrnaviTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val interFontFamily = rememberInterFontFamily()
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = interFontFamily),
                content = content
            )
        }
    )
}
