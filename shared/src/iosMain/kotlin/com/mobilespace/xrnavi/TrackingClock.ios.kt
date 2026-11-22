package com.mobilespace.xrnavi

import com.exkmptracker.ios.IosTrackerClock
import com.exkmptracker.ports.TrackerClock

internal actual fun createTrackingClock(): TrackerClock = IosTrackerClock()
