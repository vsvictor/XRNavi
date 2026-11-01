package com.mobilespace.xrnavi.domain.theme

internal enum class ThemePreference {
    System, Light, Dark;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }
}

internal interface ThemePreferenceStore {
    fun read(): String?
    fun write(theme: ThemePreference): Boolean
}
