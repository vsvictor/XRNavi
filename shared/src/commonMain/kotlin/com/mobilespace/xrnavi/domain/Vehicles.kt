package com.mobilespace.xrnavi.domain

import kotlinx.coroutines.flow.StateFlow

enum class VehicleCategory { All, Passenger, Truck, Special }
enum class MakeId { Ford, Volkswagen, Toyota, Bmw }
enum class FordModel { Mustang, Focus, Explorer }
enum class FordTrim { GT, EcoBoost }

data class VehicleMake(val id: MakeId, val name: String, val models: List<String>)
data class VehicleDimensions(val height: Double, val width: Double, val length: Double, val mass: Double)
data class FordSpecification(
    val model: FordModel = FordModel.Mustang,
    val year: Int = 2024,
    val trim: FordTrim = FordTrim.GT,
    val dimensions: VehicleDimensions = VehicleDimensions(1.4, 1.9, 4.8, 1.8),
)

enum class ConfigurationId { Empty, Loaded, Recovery }
data class VehicleConfiguration(
    val id: ConfigurationId,
    val dimensions: VehicleDimensions,
    val axleMass: Double,
    val maximumMass: Double,
    val maximumAxleMass: Double,
)

interface VehicleRepository {
    val makes: List<VehicleMake>
    val fordDraft: StateFlow<FordSpecification>
    fun updateFordDraft(specification: FordSpecification)
    fun configurations(vehicle: VehicleId): List<VehicleConfiguration>
}

enum class VehicleOperation { ApplyConfiguration, RequestDispatcherChange, SaveProfile }
data class VehicleOperationRequest(
    val operation: VehicleOperation,
    val vehicle: VehicleId,
    val configuration: ConfigurationId? = null,
    val ford: FordSpecification? = null,
    val actualLoad: Double? = null,
    val avoidTolls: Boolean = false,
)
sealed interface VehicleOperationResult {
    data object NotConfigured : VehicleOperationResult
    data object Invalid : VehicleOperationResult
    data object Failed : VehicleOperationResult
}
interface VehicleOperationsRepository {
    suspend fun perform(request: VehicleOperationRequest): VehicleOperationResult
}

class FilterVehicleMakes(private val repository: VehicleRepository) {
    operator fun invoke(query: String, category: VehicleCategory): List<VehicleMake> {
        if (category != VehicleCategory.Passenger && category != VehicleCategory.All) return emptyList()
        val term = query.trim()
        return repository.makes.filter {
            it.name.contains(term, ignoreCase = true) || it.models.any { model -> model.contains(term, ignoreCase = true) }
        }
    }
}

class SelectGarageVehicle(private val session: SessionRepository) {
    operator fun invoke(vehicle: VehicleId): Boolean {
        if (vehicle == VehicleId.Man && !session.state.value.organizationMember) return false
        session.update {
            it.copy(
                selectedVehicle = vehicle,
                context = if (vehicle == VehicleId.Mustang) DrivingContext.Personal else DrivingContext.Work,
                routingSettings = it.routingSettings.copy(vehicleCategory = if (vehicle == VehicleId.Mustang) 0 else 1),
                selectedTask = if (vehicle == VehicleId.Man && it.selectedVehicle == vehicle &&
                    it.context == DrivingContext.Work) it.selectedTask else null,
            )
        }
        return true
    }
}

class ValidateFordSpecification {
    operator fun invoke(specification: FordSpecification): Boolean =
        specification.model == FordModel.Mustang &&
            specification.year in 2022..2025 &&
            listOf(
                specification.dimensions.height, specification.dimensions.width,
                specification.dimensions.length, specification.dimensions.mass,
            ).all { it.isFinite() && it > 0 }
}

class PrepareFordProfile(
    private val repository: VehicleRepository,
    private val session: SessionRepository,
) {
    operator fun invoke(specification: FordSpecification): Boolean {
        if (!ValidateFordSpecification()(specification)) return false
        // This is an in-memory draft for review, not a saved vehicle or catalog verification.
        repository.updateFordDraft(specification)
        return SelectGarageVehicle(session)(VehicleId.Mustang)
    }
}

class ValidateTruckLoad {
    operator fun invoke(input: String, maximum: Double): Double? =
        input.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.0..maximum }
}

class PerformVehicleOperation(
    private val repository: VehicleRepository,
    private val operations: VehicleOperationsRepository,
) {
    suspend operator fun invoke(request: VehicleOperationRequest): VehicleOperationResult {
        if (request.vehicle == VehicleId.Mustang) {
            if (request.operation == VehicleOperation.RequestDispatcherChange ||
                (request.operation == VehicleOperation.SaveProfile && request.ford == null) ||
                request.ford?.let { !ValidateFordSpecification()(it) } == true
            ) return VehicleOperationResult.Invalid
        } else {
            val configuration = repository.configurations(request.vehicle).find { it.id == request.configuration }
                ?: return VehicleOperationResult.Invalid
            if (request.operation == VehicleOperation.SaveProfile && request.actualLoad == null)
                return VehicleOperationResult.Invalid
            if (request.actualLoad != null &&
                (!request.actualLoad.isFinite() || request.actualLoad !in 0.0..configuration.maximumMass)
            ) return VehicleOperationResult.Invalid
        }
        return operations.perform(request)
    }
}
