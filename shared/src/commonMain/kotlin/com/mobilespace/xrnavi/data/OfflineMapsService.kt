package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.OfflineMapsAction
import com.mobilespace.xrnavi.domain.OfflineMapsActionResult
import com.mobilespace.xrnavi.domain.OfflineMapsRepository

class UnconfiguredOfflineMapsService : OfflineMapsRepository {
    override suspend fun perform(action: OfflineMapsAction): OfflineMapsActionResult =
        OfflineMapsActionResult.NotConfigured
}
