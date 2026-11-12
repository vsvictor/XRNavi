package com.mobilespace.xrnavi

import platform.UIKit.UIDevice
import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import androidx.compose.ui.Modifier

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

internal actual fun Modifier.exportTestTags(): Modifier = this

internal actual val supportsProgrammaticApplicationExit: Boolean = false

@Composable
internal actual fun rememberExitApplication(): () -> Unit = {
    error("iOS does not support programmatic application termination.")
}

@Composable
internal actual fun AuthBackHandler(onBack: () -> Unit) {
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = onBack
    )
}

@Composable
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
internal actual fun internalAuthPreviewEnabled(): Boolean = kotlin.native.Platform.isDebugBinary

private class IosThemePreferenceStore : ThemePreferenceStore {
    private val preferences = NSUserDefaults.standardUserDefaults
    override fun read(): String? = preferences.stringForKey(themePreferenceKey)
    override fun write(theme: ThemePreference): Boolean {
        preferences.setObject(theme.name, forKey = themePreferenceKey)
        return true
    }
}

@Composable
internal actual fun rememberThemePreferenceStore(): ThemePreferenceStore =
    remember { IosThemePreferenceStore() }

private class IosSavedPlacesStore : SavedPlacesStore {
    private val preferences = NSUserDefaults.standardUserDefaults

    override fun read(): String? = preferences.stringForKey(savedPlacesPreferenceKey)

    override fun write(value: String): Boolean {
        preferences.setObject(value, forKey = savedPlacesPreferenceKey)
        return true
    }
}

@Composable
internal actual fun rememberSavedPlacesStore(): SavedPlacesStore =
    remember { IosSavedPlacesStore() }

private class IosRouteStopsStore : RouteStopsStore {
    private val preferences = NSUserDefaults.standardUserDefaults

    override fun read(): String? = preferences.stringForKey(routeStopsPreferenceKey)

    override fun write(value: String): Boolean {
        preferences.setObject(value, forKey = routeStopsPreferenceKey)
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
private class IosRecoveryProgressStore : RecoveryProgressStore {
    private val preferences = NSUserDefaults.standardUserDefaults

    override fun read(): String? = preferences.stringForKey(recoveryProgressPreferenceKey)

    override fun write(email: String?) {
        if (email == null) preferences.removeObjectForKey(recoveryProgressPreferenceKey)
        else preferences.setObject(email, forKey = recoveryProgressPreferenceKey)
    }
}

@Composable
internal actual fun rememberRecoveryProgressStore(): RecoveryProgressStore =
    remember { IosRecoveryProgressStore() }
