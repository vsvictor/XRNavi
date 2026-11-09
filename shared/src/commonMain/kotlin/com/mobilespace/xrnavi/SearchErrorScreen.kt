package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun SearchErrorScreen(actions: AdditionalActions) =
    SearchStatePreviewContent(SearchStatus.Error, AdditionalScreen.SearchLoading, actions)
