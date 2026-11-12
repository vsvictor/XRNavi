package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals

class DemoAppBlocTest {
    private fun run(vararg events: DemoAppEvent, from: DemoAppState = DemoAppState()): DemoAppState =
        events.fold(from) { state, event -> DemoAppBloc.transition(state, event) }

    @Test
    fun searchOpensAndClosesToPreviousScreen() {
        val opened = run(DemoAppEvent.SearchOpened(SearchTarget.Destination))
        assertEquals(AppScreen.Search, opened.screen)
        assertEquals(AppScreen.Map, opened.previousScreen)
        assertEquals(AppScreen.Map, run(DemoAppEvent.SearchClosed, from = opened).screen)
    }

    @Test
    fun searchRequestIncrementsCounter() {
        assertEquals(1, run(DemoAppEvent.SearchRequested).searchRequest)
    }

    @Test
    fun routeStopsOpenedAndCancelledRestorePreviousScreen() {
        val opened = run(DemoAppEvent.RouteStopsOpened)
        assertEquals(AppScreen.RouteStops, opened.screen)
        assertEquals(AppScreen.Map, run(DemoAppEvent.RouteStopsEditingCancelled, from = opened).screen)
    }

    @Test
    fun routeStopsSaveFailureKeepsScreenAndShowsNotice() {
        val state = run(
            DemoAppEvent.RouteStopsOpened,
            DemoAppEvent.RouteStopsSaveFailed("fail")
        )
        assertEquals(AppScreen.RouteStops, state.screen)
        assertEquals("fail", state.routeStopsNotice)
    }

    @Test
    fun routeSettingsOpenAndCloseReturnToOriginScreen() {
        val opened = run(DemoAppEvent.RouteSettingsOpened, from = DemoAppState(screen = AppScreen.Preview))
        assertEquals(AppScreen.RouteSettings, opened.screen)
        val closed = run(DemoAppEvent.RouteSettingsClosed, from = opened)
        assertEquals(AppScreen.Preview, closed.screen)
        assertEquals(null, closed.routePreferencesDraft)
    }

    @Test
    fun savedPlacesCloseDependsOnOrigin() {
        val fromSearch = run(DemoAppEvent.SavedPlacesOpened(SavedPlacesOrigin.Search, null),
            DemoAppEvent.SavedPlacesClosed)
        val fromProfile = run(DemoAppEvent.SavedPlacesOpened(SavedPlacesOrigin.Profile, null),
            DemoAppEvent.SavedPlacesClosed)
        assertEquals(AppScreen.Search, fromSearch.screen)
        assertEquals(AppScreen.Map, fromProfile.screen)
    }

    @Test
    fun sectionOpenAndBackNavigate() {
        val opened = run(DemoAppEvent.SectionOpened(DemoSection.Garage))
        assertEquals(DemoSection.Garage, opened.sectionNavigation.section)
        assertEquals(DemoSection.Map, run(DemoAppEvent.SectionBack, from = opened).sectionNavigation.section)
    }

    @Test
    fun exitConfirmationCanBeShownAndDismissed() {
        val shown = run(DemoAppEvent.ExitConfirmationRequested)
        assertEquals(true, shown.exitConfirmationVisible)
        assertEquals(
            false,
            run(DemoAppEvent.ExitConfirmationDismissed, from = shown).exitConfirmationVisible
        )
    }

    @Test
    fun sheetAndNoticeEventsUpdateState() {
        val state = run(
            DemoAppEvent.SheetLevelChanged(SheetLevel.Expanded),
            DemoAppEvent.NoticeShown("hello")
        )
        assertEquals(SheetLevel.Expanded, state.sheet)
        assertEquals("hello", state.notice)
    }

    @Test
    fun blocDispatchPublishesNewState() {
        val bloc = DemoAppBloc()
        bloc.dispatch(DemoAppEvent.ScreenChanged(AppScreen.Recovery))
        assertEquals(AppScreen.Recovery, bloc.state.value.screen)
    }
}
