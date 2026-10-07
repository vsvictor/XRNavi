package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.TripPlanActionResult
import com.mobilespace.xrnavi.domain.TripPlanRepository

class UnconfiguredTripPlanService : TripPlanRepository {
    override suspend fun addStop(taskId: String): TripPlanActionResult = TripPlanActionResult.NotConfigured
}
