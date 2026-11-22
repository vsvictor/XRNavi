package com.mobilespace.xrnavi

import com.exkmptracker.bloc.TrackerState
import com.exkmptracker.model.TrackingSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class UnavailableTrackingRuntime : AppTrackingRuntime {
    private val mutableState = MutableStateFlow(TrackerState())
    override val state = mutableState.asStateFlow()
    override suspend fun start(session: TrackingSession) {
        mutableState.value = TrackerState(error = "Native tracker runtime was not configured")
    }
    override suspend fun stop() { mutableState.value = TrackerState() }
}
