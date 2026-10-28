package com.mobilespace.xrnavi

import platform.UIKit.UIDevice
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun AuthBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
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

@Composable
internal actual fun rememberAppVersion(): String = remember {
    val bundle = NSBundle.mainBundle
    val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "—"
    val build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "—"
    "$version ($build)"
}