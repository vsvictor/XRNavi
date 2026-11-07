package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun App(
    dependencies: AppDependencies,
    onThemeChanged: (Boolean) -> Unit = {},
) {
    val themeStore = rememberThemePreferenceStore()
    val themeSettings: ThemeSettingsViewModel = viewModel(
        factory = remember(themeStore) { themeSettingsFactory(themeStore) }
    )
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = themeSettings.preference.isDark(systemDarkTheme)
    val connectionStatus by dependencies.connectionBloc.state.collectAsStateWithLifecycle()
    val connectionError by dependencies.connectionBloc.error.collectAsStateWithLifecycle()
    val connectionSnackbar = remember { SnackbarHostState() }
    LaunchedEffect(connectionError) {
        connectionError?.let {
            connectionSnackbar.showSnackbar(it, duration = SnackbarDuration.Indefinite)
        }
    }
    LaunchedEffect(dependencies) {
        dependencies.mobileSpaceRpcClient.start()
    }
    LaunchedEffect(darkTheme) {
        onThemeChanged(darkTheme)
    }
    XrnaviTheme(darkTheme = darkTheme) {
        Box(Modifier.fillMaxSize()) {
            XrnaviApplication(
                dependencies = dependencies,
                themeSettings = themeSettings,
                onToggleTheme = { themeSettings.toggle(systemDarkTheme) }
            )
            Box(
                Modifier.fillMaxSize().safeDrawingPadding()
                    .testTag("connection_indicator_safe_area")
            ) {
                SnackbarHost(connectionSnackbar, Modifier.align(Alignment.BottomCenter))
                ConnectionIndicator(
                    status = connectionStatus,
                    modifier = Modifier
                        .align(AbsoluteAlignment.TopRight)
                        .absolutePadding(
                            top = ConnectionIndicatorEdgeOffset,
                            right = ConnectionIndicatorEdgeOffset
                        )
                )
            }
        }
    }
}
