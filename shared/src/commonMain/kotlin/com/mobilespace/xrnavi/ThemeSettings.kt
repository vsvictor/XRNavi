package com.mobilespace.xrnavi

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

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

internal class ThemeSettingsViewModel(private val store: ThemePreferenceStore) : ViewModel() {
    private val savedValue = store.read()
    var preference by mutableStateOf(
        ThemePreference.entries.firstOrNull { it.name == savedValue } ?: ThemePreference.System
    )
        private set
    var notice by mutableStateOf(
        if (savedValue != null && ThemePreference.entries.none { it.name == savedValue })
            "Збережений режим теми не розпізнано. Використано «Як у системі»; виберіть тему повторно."
        else ""
    )
        private set

    fun select(theme: ThemePreference) {
        if (!store.write(theme)) {
            notice = "Не вдалося зберегти тему. Попередній режим залишено; спробуйте ще раз."
            return
        }
        preference = theme
        notice = ""
    }

    fun toggle(systemDark: Boolean) {
        select(if (preference.isDark(systemDark)) ThemePreference.Light else ThemePreference.Dark)
    }
}

internal fun themeSettingsFactory(store: ThemePreferenceStore) = viewModelFactory {
    initializer { ThemeSettingsViewModel(store) }
}

@Composable
internal expect fun rememberThemePreferenceStore(): ThemePreferenceStore

@Composable
internal expect fun rememberAppVersion(): String
