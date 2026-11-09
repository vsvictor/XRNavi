package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun SearchEmptyScreen(actions: AdditionalActions) =
    SearchStatePreviewContent(SearchStatus.Empty, AdditionalScreen.SearchLoading, actions)
