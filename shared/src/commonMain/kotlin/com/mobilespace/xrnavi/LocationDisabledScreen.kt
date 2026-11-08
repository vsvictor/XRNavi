package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun LocationDisabledScreen(actions: AdditionalActions) =
    LocationUnavailableContent(AdditionalScreen.LocationDisabled, actions)
