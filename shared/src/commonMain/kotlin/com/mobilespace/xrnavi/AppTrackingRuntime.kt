package com.mobilespace.xrnavi

import com.exkmptracker.bloc.TrackerState
import com.exkmptracker.model.TrackingSession
import kotlinx.coroutines.flow.StateFlow

/** Native owner of location/sensor lifecycle; never tied to a screen's composition. */
interface AppTrackingRuntime {
    val state: StateFlow<TrackerState>
    suspend fun start(session: TrackingSession)
    suspend fun stop()
}
