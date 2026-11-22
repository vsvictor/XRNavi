package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun RouteExplanationScreen(layout: AccountLayout, actions: AdditionalActions) =
    RouteRestrictionContent(layout, actions, unverified = false)
