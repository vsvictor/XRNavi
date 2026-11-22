package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MapBlocTest {
    @Test
    fun retryAndStyleChangeIgnoreOldCallbacksAndKeepCamera() {
        val bloc = MapBloc()
        val camera = MapCamera(latitude = 49.0, longitude = 24.0, zoom = 14.0)
        bloc.dispatch(MapEvent.CameraChanged(camera))
        bloc.dispatch(MapEvent.NativeStatus(0, MapLoadStatus.Failed))
        assertEquals(MapLoadStatus.Failed, bloc.state.value.status)
        bloc.dispatch(MapEvent.Retry)
        bloc.dispatch(MapEvent.NativeStatus(0, MapLoadStatus.Ready))
        assertEquals(MapLoadStatus.Loading, bloc.state.value.status)
        bloc.dispatch(MapEvent.NativeStatus(1, MapLoadStatus.Ready))
        assertEquals(MapLoadStatus.Ready, bloc.state.value.status)
        bloc.dispatch(MapEvent.StyleChanged)
        assertEquals(camera, bloc.state.value.camera)
        assertEquals(2, bloc.state.value.revision)
    }

    @Test
    fun repeatedControlsHaveDistinctIdsAndRecenterNeverClaimsLocation() {
        val bloc = MapBloc()
        bloc.dispatch(MapEvent.Control(MapCommand.ZoomIn))
        val first = bloc.state.value.commandId
        bloc.dispatch(MapEvent.Control(MapCommand.ZoomIn))
        assertNotEquals(first, bloc.state.value.commandId)
        bloc.dispatch(MapEvent.Control(MapCommand.Recenter))
        assertTrue(bloc.state.value.notice.contains("не ваша"))
        assertEquals(MapCommand.Recenter, bloc.state.value.command)
    }

    @Test
    fun unavailableSearchDoesNotRemoveOrFailMap() {
        val bloc = MapBloc()
        bloc.dispatch(MapEvent.NativeStatus(0, MapLoadStatus.Ready))
        bloc.dispatch(MapEvent.SearchRequested)
        assertEquals(MapLoadStatus.Ready, bloc.state.value.status)
        assertTrue(bloc.state.value.notice.contains("ще не підключені"))
        bloc.dispatch(MapEvent.DismissNotice)
        assertEquals("", bloc.state.value.notice)
    }
}
