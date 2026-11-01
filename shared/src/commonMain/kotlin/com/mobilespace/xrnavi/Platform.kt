package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

internal expect fun Modifier.exportTestTags(): Modifier

@Composable
internal expect fun rememberAppVersion(): String