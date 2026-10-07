package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.VehicleOperationRequest
import com.mobilespace.xrnavi.domain.VehicleOperationResult
import com.mobilespace.xrnavi.domain.VehicleOperationsRepository

/** No backend is configured; no request is sent and nothing is persisted. */
object UnconfiguredVehicleConfigurationsService : VehicleOperationsRepository {
    override suspend fun perform(request: VehicleOperationRequest): VehicleOperationResult =
        VehicleOperationResult.NotConfigured
}
