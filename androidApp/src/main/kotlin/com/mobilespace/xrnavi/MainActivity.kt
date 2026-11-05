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
    val dependencies by lazy { AppDependencies() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App(
                dependencies = (application as XrnaviAndroidApplication).dependencies,
                onThemeChanged = ::applySystemBarTheme,
            )
        }
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