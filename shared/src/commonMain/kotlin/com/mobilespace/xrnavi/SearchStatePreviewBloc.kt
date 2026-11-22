package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class SearchStatePreviewState(val query: String = "")

internal sealed interface SearchStatePreviewEvent {
    data class Query(val value: String) : SearchStatePreviewEvent
}

internal class SearchStatePreviewBloc {
    private val mutableState = MutableStateFlow(SearchStatePreviewState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: SearchStatePreviewEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(
            state: SearchStatePreviewState,
            event: SearchStatePreviewEvent
        ): SearchStatePreviewState = when (event) {
            is SearchStatePreviewEvent.Query -> state.copy(query = event.value)
        }
    }
}
