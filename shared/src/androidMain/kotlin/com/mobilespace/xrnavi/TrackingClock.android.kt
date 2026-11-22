package com.mobilespace.xrnavi

import com.exkmptracker.android.AndroidTrackerClock
import com.exkmptracker.ports.TrackerClock

internal actual fun createTrackingClock(): TrackerClock = AndroidTrackerClock()
