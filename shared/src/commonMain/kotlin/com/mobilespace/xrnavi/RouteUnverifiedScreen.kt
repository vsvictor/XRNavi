package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun RouteUnverifiedScreen(layout: AccountLayout, actions: AdditionalActions) =
    RouteRestrictionContent(layout, actions, unverified = true)
