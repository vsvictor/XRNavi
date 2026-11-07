package com.mobilespace.xrnavi

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(mapFactory: IosMapViewFactory) = run {
    val dependencies = AppDependencies(PlatformMapFactory(mapFactory))
    ComposeUIViewController { App(dependencies) }
}