package com.mobilespace.xrnavi

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.content.pm.ApplicationInfo
import android.content.Context
import androidx.compose.runtime.remember

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
internal actual fun AuthBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}

@Composable
internal actual fun internalAuthPreviewEnabled(): Boolean =
    LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

internal class AndroidThemePreferenceStore(context: Context) : ThemePreferenceStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        "xrnavi_appearance", Context.MODE_PRIVATE
    )

    override fun read(): String? = preferences.getString(themePreferenceKey, null)

    override fun write(theme: ThemePreference): Boolean =
        preferences.edit().putString(themePreferenceKey, theme.name).commit()
}

@Composable
internal actual fun rememberThemePreferenceStore(): ThemePreferenceStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidThemePreferenceStore(context) }
}

private class AndroidSavedPlacesStore(context: Context) : SavedPlacesStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        "xrnavi_saved_places", Context.MODE_PRIVATE
    )

    override fun read(): String? = preferences.getString(savedPlacesPreferenceKey, null)

    override fun write(value: String): Boolean =
        preferences.edit().putString(savedPlacesPreferenceKey, value).commit()
}

@Composable
internal actual fun rememberSavedPlacesStore(): SavedPlacesStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidSavedPlacesStore(context) }
}

@Composable
internal actual fun rememberAppVersion(): String {
    val context = LocalContext.current
    return remember(context) {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        "${info.versionName ?: "—"} (${info.longVersionCode})"
    }
}