package com.mobilespace.xrnavi

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(mapFactory: IosMapViewFactory, mapboxToken: String) = run {
    val dependencies = AppDependencies(PlatformMapFactory(mapFactory)) { mapboxToken }
    ComposeUIViewController { App(dependencies) }
}