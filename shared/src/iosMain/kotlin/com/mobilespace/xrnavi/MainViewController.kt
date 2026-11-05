package com.mobilespace.xrnavi

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = run {
    val dependencies = AppDependencies()
    ComposeUIViewController { App(dependencies) }
}