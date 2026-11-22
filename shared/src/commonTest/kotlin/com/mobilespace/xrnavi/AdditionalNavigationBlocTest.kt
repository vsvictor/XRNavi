package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdditionalNavigationBlocTest {
    @Test
    fun returningToAnExistingDestinationDoesNotAccumulateNavigationCycles() {
        val bloc = AdditionalNavigationBloc()
        bloc.dispatch(AdditionalNavigationEvent.Open(AdditionalScreen.VehicleCatalog))
        repeat(10) {
            bloc.dispatch(AdditionalNavigationEvent.Open(AdditionalScreen.VehicleModel))
            bloc.dispatch(AdditionalNavigationEvent.Open(AdditionalScreen.VehicleReview))
            bloc.dispatch(AdditionalNavigationEvent.Open(AdditionalScreen.VehicleCatalog))
            assertTrue(bloc.state.value.backStack.isEmpty())
        }
        bloc.dispatch(AdditionalNavigationEvent.Back)
        assertNull(bloc.state.value.screen)
    }

    @Test
    fun everyScreenOpensAndBackRestoresThePreviousScreen() {
        AdditionalScreen.entries.forEach { screen ->
            val bloc = AdditionalNavigationBloc()
            bloc.dispatch(AdditionalNavigationEvent.Open(AdditionalScreen.Privacy))
            bloc.dispatch(AdditionalNavigationEvent.Open(screen))
            assertEquals(screen, bloc.state.value.screen)
            bloc.dispatch(AdditionalNavigationEvent.Back)
            assertEquals(
                if (screen == AdditionalScreen.Privacy) null else AdditionalScreen.Privacy,
                bloc.state.value.screen
            )
        }
    }

    @Test
    fun bottomNavigationClosesTheEntireStackAndEmitsOneSectionTransition() {
        val bloc = AdditionalNavigationBloc()
        bloc.dispatch(AdditionalNavigationEvent.Open(AdditionalScreen.Privacy))
        bloc.dispatch(AdditionalNavigationEvent.Open(AdditionalScreen.DeleteAccount))
        bloc.dispatch(AdditionalNavigationEvent.Section(DemoSection.Settings))
        assertNull(bloc.state.value.screen)
        assertTrue(bloc.state.value.backStack.isEmpty())
        assertEquals(DemoSection.Settings, bloc.state.value.sectionTarget)
        bloc.dispatch(AdditionalNavigationEvent.SectionHandled)
        assertNull(bloc.state.value.sectionTarget)
    }
}
