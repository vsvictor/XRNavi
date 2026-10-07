package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Illustrative local fixtures, not verified catalog entries or a persistence service. */
class DemoVehicleRepository : VehicleRepository {
    override val makes = listOf(
        VehicleMake(MakeId.Ford, "Ford", listOf("Mustang", "Focus", "Explorer")),
        VehicleMake(MakeId.Volkswagen, "Volkswagen", listOf("Golf", "Passat", "Tiguan")),
        VehicleMake(MakeId.Toyota, "Toyota", listOf("Corolla", "Camry", "RAV4")),
        VehicleMake(MakeId.Bmw, "BMW", listOf("3 Series", "5 Series", "X5")),
    )
    private val draft = MutableStateFlow(FordSpecification())
    override val fordDraft = draft.asStateFlow()
    override fun updateFordDraft(specification: FordSpecification) { draft.value = specification }

    override fun configurations(vehicle: VehicleId): List<VehicleConfiguration> =
        if (vehicle == VehicleId.Mustang) emptyList() else listOf(
            VehicleConfiguration(ConfigurationId.Empty, VehicleDimensions(3.8, 2.55, 16.5, 18.2), 7.1, 40.0, 11.5),
            VehicleConfiguration(ConfigurationId.Loaded, VehicleDimensions(4.0, 2.55, 16.5, 36.8), 10.8, 40.0, 11.5),
            VehicleConfiguration(ConfigurationId.Recovery, VehicleDimensions(3.1, 2.3, 8.2, 6.8), 3.6, 7.2, 3.6),
        )
}
