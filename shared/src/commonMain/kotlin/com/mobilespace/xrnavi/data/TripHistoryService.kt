package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.TripHistoryAction
import com.mobilespace.xrnavi.domain.TripHistoryActionResult
import com.mobilespace.xrnavi.domain.TripHistoryRepository

class UnconfiguredTripHistoryService : TripHistoryRepository {
    override suspend fun perform(action: TripHistoryAction): TripHistoryActionResult =
        TripHistoryActionResult.NotConfigured
}
