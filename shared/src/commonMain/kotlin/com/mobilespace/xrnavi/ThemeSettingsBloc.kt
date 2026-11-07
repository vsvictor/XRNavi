package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class ThemeSettingsState(
    val preference: ThemePreference = ThemePreference.System,
    val notice: String = ""
)

internal sealed interface ThemeSettingsEvent {
    data class Select(val theme: ThemePreference) : ThemeSettingsEvent
    data class Toggle(val systemDark: Boolean) : ThemeSettingsEvent
}

internal class ThemeSettingsBloc(private val store: ThemePreferenceStore) {
    private val mutableState = MutableStateFlow(initialState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: ThemeSettingsEvent) {
        val current = mutableState.value
        val theme = when (event) {
            is ThemeSettingsEvent.Select -> event.theme
            is ThemeSettingsEvent.Toggle ->
                if (current.preference.isDark(event.systemDark)) ThemePreference.Light
                else ThemePreference.Dark
        }
        mutableState.value = if (store.write(theme)) {
            ThemeSettingsState(preference = theme)
        } else {
            current.copy(
                notice = "Не вдалося зберегти тему. Попередній режим залишено; спробуйте ще раз."
            )
        }
    }

    private fun initialState(): ThemeSettingsState {
        val saved = store.read()
        val preference = ThemePreference.entries.firstOrNull { it.name == saved }
        return ThemeSettingsState(
            preference = preference ?: ThemePreference.System,
            notice = if (saved != null && preference == null) {
                "Збережений режим теми не розпізнано. Використано «Як у системі»; виберіть тему повторно."
            } else {
                ""
            }
        )
    }
}
