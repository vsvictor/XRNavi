package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class ThemeSettingsViewModel(private val store: ThemePreferenceStore) : ViewModel() {
    private val savedValue = store.read()
    var preference by mutableStateOf(
        ThemePreference.entries.firstOrNull { it.name == savedValue } ?: ThemePreference.System
    )
        private set
    var notice by mutableStateOf(
        if (savedValue != null && ThemePreference.entries.none { it.name == savedValue }) {
            "Збережений режим теми не розпізнано. Використано «Як у системі»; виберіть тему повторно."
        } else {
            ""
        }
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
