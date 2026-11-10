package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SplashBlocTest {
    @Test
    fun completesAtExactly2400MillisecondsFromFirstRenderedFrame() {
        val bloc = SplashBloc()
        val start = 10_000_000_000L
        bloc.dispatch(SplashEvent.Frame(start))
        bloc.dispatch(SplashEvent.Frame(start + 2_399_000_000L))
        assertFalse(bloc.state.value.finished)
        assertEquals(2_399L, bloc.state.value.elapsedMillis)
        bloc.dispatch(SplashEvent.Frame(start + 2_400_000_000L))
        assertTrue(bloc.state.value.finished)
        assertEquals(SplashDurationMillis, bloc.state.value.elapsedMillis)
    }

    @Test
    fun resumedFramesFinishInsteadOfReplayingAfterAnInterruption() {
        val bloc = SplashBloc()
        bloc.dispatch(SplashEvent.Frame(0))
        bloc.dispatch(SplashEvent.Frame(500_000_000))
        bloc.dispatch(SplashEvent.Frame(30_000_000_000))
        assertTrue(bloc.state.value.finished)
        bloc.dispatch(SplashEvent.Frame(0))
        assertTrue(bloc.state.value.finished)
    }

    @Test
    fun earlierFrameCannotMoveTheAnimationBackward() {
        val bloc = SplashBloc()
        bloc.dispatch(SplashEvent.Frame(0))
        bloc.dispatch(SplashEvent.Frame(500_000_000))
        bloc.dispatch(SplashEvent.Frame(400_000_000))
        assertEquals(500L, bloc.state.value.elapsedMillis)
    }
}
