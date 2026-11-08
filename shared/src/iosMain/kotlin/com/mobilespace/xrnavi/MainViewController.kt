package com.mobilespace.xrnavi

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(mapFactory: IosMapViewFactory, mapboxToken: String) = run {
    val dependencies = AppDependencies(
        mapFactory = PlatformMapFactory(mapFactory),
        mapboxToken = { mapboxToken },
        locationProvider = IosLocationProvider(),
        sessionStorage = IosKeychainSessionStorage(),
    )
    ComposeUIViewController { App(dependencies) }
}
