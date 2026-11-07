package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class PlatformMapFactory()

@Composable
internal expect fun NativeMap(
    factory: PlatformMapFactory,
    state: MapState,
    dark: Boolean,
    dispatch: (MapEvent) -> Unit,
    modifier: Modifier,
)
