package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

internal enum class ThemePreference(val label: String) {
    System("Як у системі"), Light("Світла"), Dark("Темна");

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }
}

internal const val themePreferenceKey = "appearance.theme"

internal interface ThemePreferenceStore {
    fun read(): String?
    fun write(theme: ThemePreference): Boolean
}

@Composable
internal expect fun rememberThemePreferenceStore(): ThemePreferenceStore

@Composable
internal expect fun rememberAppVersion(): String
