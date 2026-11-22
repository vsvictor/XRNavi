package com.mobilespace.xrnavi

import com.exkmptracker.bloc.TrackerState

internal enum class TrackerIntegrationProblem { Registration, Runtime }

internal data class TrackerIntegrationState(
    val authenticated: Boolean = false,
    val registering: Boolean = false,
    val trackerId: String? = null,
    val tracking: TrackerState = TrackerState(),
    val problem: TrackerIntegrationProblem? = null,
)
