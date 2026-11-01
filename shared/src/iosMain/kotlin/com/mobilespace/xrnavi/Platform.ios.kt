package com.mobilespace.xrnavi

import platform.UIKit.UIDevice
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import androidx.compose.ui.Modifier

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

internal actual fun Modifier.exportTestTags(): Modifier = this

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun AuthBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}

private class IosSavedPlacesStore : SavedPlacesStore {
    private val preferences = NSUserDefaults.standardUserDefaults

    override fun read(): String? = preferences.stringForKey(Constants.SAVED_PLACES_PREFERENCE_KEY)

    override fun write(value: String): Boolean {
        preferences.setObject(value, forKey = Constants.SAVED_PLACES_PREFERENCE_KEY)
        return true
    }
}

@Composable
internal actual fun rememberSavedPlacesStore(): SavedPlacesStore =
    remember { IosSavedPlacesStore() }

private class IosRouteStopsStore : RouteStopsStore {
    private val preferences = NSUserDefaults.standardUserDefaults

    override fun read(): String? = preferences.stringForKey(Constants.ROUTE_STOPS_PREFERENCE_KEY)

    override fun write(value: String): Boolean {
        preferences.setObject(value, forKey = Constants.ROUTE_STOPS_PREFERENCE_KEY)
        return true
    }
}

@Composable
internal actual fun rememberRouteStopsStore(): RouteStopsStore =
    remember { IosRouteStopsStore() }

@Composable
internal actual fun rememberAppVersion(): String = remember {
    val bundle = NSBundle.mainBundle
    val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "—"
    val build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "—"
    "$version ($build)"
}