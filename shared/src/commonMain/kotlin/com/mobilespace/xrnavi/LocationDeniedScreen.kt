package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun LocationDeniedScreen(actions: AdditionalActions) =
    LocationUnavailableContent(AdditionalScreen.LocationDenied, actions)
