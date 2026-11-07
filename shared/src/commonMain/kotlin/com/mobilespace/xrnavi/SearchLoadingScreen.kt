package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun SearchLoadingScreen(actions: AdditionalActions) =
    SearchStatePreviewContent(SearchStatus.Loading, AdditionalScreen.SearchError, actions)
