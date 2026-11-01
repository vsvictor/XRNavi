package com.mobilespace.xrnavi.data.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mobilespace.xrnavi.Constants
import com.mobilespace.xrnavi.domain.theme.ThemePreference
import com.mobilespace.xrnavi.domain.theme.ThemePreferenceStore

private class AndroidThemePreferenceStore(context: Context) : ThemePreferenceStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        "xrnavi_appearance", Context.MODE_PRIVATE
    )

    override fun read(): String? = preferences.getString(Constants.THEME_PREFERENCE_KEY, null)

    override fun write(theme: ThemePreference): Boolean =
        preferences.edit().putString(Constants.THEME_PREFERENCE_KEY, theme.name).commit()
}

@Composable
internal actual fun rememberThemePreferenceStore(): ThemePreferenceStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidThemePreferenceStore(context) }
}
