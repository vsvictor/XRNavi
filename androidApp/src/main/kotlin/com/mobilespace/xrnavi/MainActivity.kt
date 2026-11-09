package com.mobilespace.xrnavi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import android.app.Application
import com.mobilespace.xrnavi.AppDependencies

class XrnaviAndroidApplication : Application() {
    val locationProvider by lazy { AndroidLocationProvider(this) }
    val dependencies by lazy {
        AppDependencies(
            mapboxToken = { getString(R.string.mapbox_access_token) },
            locationProvider = locationProvider,
            sessionStorage = AndroidKeystoreSessionStorage(this),
            deviceIdStorage = AndroidKeystoreSessionStorage(this, entry = "device_id"),
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.attributes = window.attributes.apply {
            rotationAnimation = android.view.WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE
        }
        val provider = (application as XrnaviAndroidApplication).locationProvider
        val launcher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
        ) { provider.onPermissionResult() }
        provider.attach(this) { launcher.launch(it) }

        setContent {
            App(
                dependencies = (application as XrnaviAndroidApplication).dependencies,
                onThemeChanged = ::applySystemBarTheme,
            )
        }
    }

    override fun onDestroy() {
        (application as XrnaviAndroidApplication).locationProvider.detach(this)
        super.onDestroy()
    }

    private fun applySystemBarTheme(darkTheme: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val dependencies = remember { AppDependencies() }
    App(dependencies)
}