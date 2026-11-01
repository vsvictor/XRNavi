package com.mobilespace.xrnavi.data.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mobilespace.xrnavi.Constants
import com.mobilespace.xrnavi.domain.theme.ThemePreference
import com.mobilespace.xrnavi.domain.theme.ThemePreferenceStore
import platform.Foundation.NSUserDefaults

private class IosThemePreferenceStore : ThemePreferenceStore {
    private val preferences = NSUserDefaults.standardUserDefaults

    override fun read(): String? = preferences.stringForKey(Constants.THEME_PREFERENCE_KEY)

    override fun write(theme: ThemePreference): Boolean {
        preferences.setObject(theme.name, forKey = Constants.THEME_PREFERENCE_KEY)
        return true
    }
}

@Composable
internal actual fun rememberThemePreferenceStore(): ThemePreferenceStore =
    remember { IosThemePreferenceStore() }
