package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class AdditionalNavigationState(
    val screen: AdditionalScreen? = null,
    val backStack: List<AdditionalScreen> = emptyList(),
    val sectionTarget: DemoSection? = null
)

internal sealed interface AdditionalNavigationEvent {
    data class Open(val screen: AdditionalScreen) : AdditionalNavigationEvent
    data object Back : AdditionalNavigationEvent
    data class Section(val section: DemoSection) : AdditionalNavigationEvent
    data object SectionHandled : AdditionalNavigationEvent
}

internal class AdditionalNavigationBloc {
    private val mutableState = MutableStateFlow(AdditionalNavigationState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: AdditionalNavigationEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(
            state: AdditionalNavigationState,
            event: AdditionalNavigationEvent
        ): AdditionalNavigationState = when (event) {
            is AdditionalNavigationEvent.Open -> if (event.screen == state.screen) state else state.copy(
                screen = event.screen,
                backStack = if (event.screen in state.backStack)
                    state.backStack.take(state.backStack.indexOf(event.screen))
                else state.backStack + listOfNotNull(state.screen)
            )

            AdditionalNavigationEvent.Back -> state.copy(
                screen = state.backStack.lastOrNull(),
                backStack = state.backStack.dropLast(1)
            )

            is AdditionalNavigationEvent.Section -> state.copy(
                screen = null,
                backStack = emptyList(),
                sectionTarget = event.section
            )

            AdditionalNavigationEvent.SectionHandled -> state.copy(sectionTarget = null)
        }
    }
}
