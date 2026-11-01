package com.mobilespace.xrnavi.data.vehicleconfigurations

import com.mobilespace.xrnavi.domain.vehicleconfigurations.VehicleConfiguration
import com.mobilespace.xrnavi.domain.vehicleconfigurations.VehicleConfigurationRepository

internal object EmptyVehicleConfigurationRepository : VehicleConfigurationRepository {
    // TODO: Replace with a repository that loads the authenticated user's real vehicle configurations.
    override fun configurations(): List<VehicleConfiguration> = emptyList()
}
