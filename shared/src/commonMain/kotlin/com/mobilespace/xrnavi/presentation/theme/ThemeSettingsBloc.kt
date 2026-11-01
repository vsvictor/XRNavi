package com.mobilespace.xrnavi.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobilespace.xrnavi.domain.theme.ThemePreference
import com.mobilespace.xrnavi.domain.theme.ThemePreferenceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class ThemeSettingsState(
    val preference: ThemePreference,
    val notice: ThemeSettingsNotice? = null
)

internal enum class ThemeSettingsNotice {
    UnknownStoredPreference,
    SaveFailed
}

internal sealed interface ThemeSettingsEvent {
    data class Select(val preference: ThemePreference) : ThemeSettingsEvent
    data class Toggle(val systemDark: Boolean) : ThemeSettingsEvent
}

internal class ThemeSettingsBloc(private val store: ThemePreferenceStore) : ViewModel() {
    private val savedValue = store.read()
    private val mutableState = MutableStateFlow(
        ThemeSettingsState(
            preference = ThemePreference.entries.firstOrNull { it.name == savedValue }
                ?: ThemePreference.System,
            notice = if (
                savedValue != null && ThemePreference.entries.none { it.name == savedValue }
            ) {
                ThemeSettingsNotice.UnknownStoredPreference
            } else {
                null
            }
        )
    )
    val state: StateFlow<ThemeSettingsState> = mutableState.asStateFlow()

    fun dispatch(event: ThemeSettingsEvent) {
        when (event) {
            is ThemeSettingsEvent.Select -> select(event.preference)
            is ThemeSettingsEvent.Toggle -> {
                val current = mutableState.value.preference
                select(if (current.isDark(event.systemDark)) ThemePreference.Light else ThemePreference.Dark)
            }
        }
    }

    private fun select(theme: ThemePreference) {
        if (!store.write(theme)) {
            mutableState.value = mutableState.value.copy(
                notice = ThemeSettingsNotice.SaveFailed
            )
            return
        }
        mutableState.value = ThemeSettingsState(preference = theme)
    }
}

internal fun themeSettingsBlocFactory(store: ThemePreferenceStore) = viewModelFactory {
    initializer { ThemeSettingsBloc(store) }
}
