package com.mobilespace.xrnavi

import android.os.Bundle
import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import android.app.Application
import com.mobilespace.xrnavi.AppDependencies
import com.exkmptracker.android.AndroidTrackerProvider
import com.exkmptracker.ports.TrackingTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import android.content.Context

class XrnaviAndroidApplication : Application(), AndroidTrackerProvider {
    private val trackingScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val trackingRuntime by lazy { AndroidAppTrackingRuntime(this, trackingScope) }
    val locationProvider by lazy { AndroidLocationProvider(this) }
    val dependencies by lazy {
        AppDependencies(
            mapboxToken = { getString(R.string.mapbox_access_token) },
            locationProvider = locationProvider,
            sessionStorage = AndroidKeystoreSessionStorage(this),
            deviceIdStorage = AndroidKeystoreSessionStorage(this, entry = "device_id"),
            vehiclePhotoStorage = AndroidKeystoreSessionStorage(this, entry = "vehicle_photos"),
            trackingRuntimeFactory = { trackingRuntime },
        )
    }
    override fun createTrackingTransport(context: Context): TrackingTransport = dependencies.trackingTransport
    override fun allowsStickyRestore(context: Context): Boolean = false
}

class MainActivity : ComponentActivity() {
    private var trackingPermissions: ((Array<String>) -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        configureTransparentSystemBars()
        hideSystemBars()
        window.attributes = window.attributes.apply {
            layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            rotationAnimation = android.view.WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE
        }
        val provider = (application as XrnaviAndroidApplication).locationProvider
        val launcher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
        ) { provider.onPermissionResult() }
        provider.attach(this) { launcher.launch(it) }
        val tracker = (application as XrnaviAndroidApplication).trackingRuntime
        val trackingLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            provider.refresh()
            tracker.permissionsReturned()
        }
        trackingPermissions = { trackingLauncher.launch(it) }

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

    override fun onResume() {
        super.onResume()
        (application as XrnaviAndroidApplication).locationProvider.refresh()
        trackingPermissions?.let {
            (application as XrnaviAndroidApplication).trackingRuntime.attach(this, it)
        }
    }

    override fun onPause() {
        (application as XrnaviAndroidApplication).trackingRuntime.detach(this)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    @Suppress("DEPRECATION")
    private fun configureTransparentSystemBars() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun applySystemBarTheme(darkTheme: Boolean) {
        window.decorView.setBackgroundColor(if (darkTheme) 0xFF0B1220.toInt() else 0xFFF6F8FC.toInt())
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