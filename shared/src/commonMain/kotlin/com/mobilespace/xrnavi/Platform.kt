package com.mobilespace.xrnavi

import androidx.compose.ui.Modifier

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

internal expect fun Modifier.exportTestTags(): Modifier

internal expect val supportsProgrammaticApplicationExit: Boolean

@androidx.compose.runtime.Composable
internal expect fun rememberExitApplication(): () -> Unit