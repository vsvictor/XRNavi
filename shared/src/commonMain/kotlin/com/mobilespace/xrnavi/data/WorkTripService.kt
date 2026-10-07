package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.WorkTripAction
import com.mobilespace.xrnavi.domain.WorkTripActionResult
import com.mobilespace.xrnavi.domain.WorkTripRepository

class UnconfiguredWorkTripService : WorkTripRepository {
    override suspend fun perform(action: WorkTripAction, taskId: String): WorkTripActionResult =
        WorkTripActionResult.NotConfigured
}
