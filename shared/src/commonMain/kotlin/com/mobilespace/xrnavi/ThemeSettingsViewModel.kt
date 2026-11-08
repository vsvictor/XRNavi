package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/** Compose-facing host of [ThemeSettingsBloc]: mirrors its state and forwards intents as events. */
internal class ThemeSettingsViewModel(store: ThemePreferenceStore) : ViewModel() {
    private val bloc = ThemeSettingsBloc(store)
    private var current by mutableStateOf(bloc.state.value)

    private fun dispatch(event: ThemeSettingsEvent) {
        bloc.dispatch(event)
        current = bloc.state.value
    }

    val preference get() = current.preference
    val notice get() = current.notice

    fun select(theme: ThemePreference) = dispatch(ThemeSettingsEvent.Select(theme))
    fun toggle(systemDark: Boolean) = dispatch(ThemeSettingsEvent.Toggle(systemDark))
}

internal fun themeSettingsFactory(store: ThemePreferenceStore) = viewModelFactory {
    initializer { ThemeSettingsViewModel(store) }
}
