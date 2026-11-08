package com.mobilespace.xrnavi

import kotlin.time.Clock

internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
