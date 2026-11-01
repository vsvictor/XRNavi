package com.mobilespace.xrnavi

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

internal actual fun Modifier.exportTestTags(): Modifier =
    semantics { testTagsAsResourceId = true }

@Composable
internal actual fun AuthBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}

private class AndroidSavedPlacesStore(context: Context) : SavedPlacesStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        "xrnavi_saved_places", Context.MODE_PRIVATE
    )

    override fun read(): String? =
        preferences.getString(Constants.SAVED_PLACES_PREFERENCE_KEY, null)

    override fun write(value: String): Boolean =
        preferences.edit().putString(Constants.SAVED_PLACES_PREFERENCE_KEY, value).commit()
}

@Composable
internal actual fun rememberSavedPlacesStore(): SavedPlacesStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidSavedPlacesStore(context) }
}

private class AndroidRouteStopsStore(context: Context) : RouteStopsStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        "xrnavi_route_stops", Context.MODE_PRIVATE
    )

    override fun read(): String? =
        preferences.getString(Constants.ROUTE_STOPS_PREFERENCE_KEY, null)

    override fun write(value: String): Boolean =
        preferences.edit().putString(Constants.ROUTE_STOPS_PREFERENCE_KEY, value).commit()
}

@Composable
internal actual fun rememberRouteStopsStore(): RouteStopsStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidRouteStopsStore(context) }
}

@Composable
internal actual fun rememberAppVersion(): String {
    val context = LocalContext.current
    return remember(context) {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        "${info.versionName ?: "—"} (${info.longVersionCode})"
    }
}