package com.mobilespace.xrnavi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilespace.xrnavi.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface VehicleMessage {
    data object MembershipRequired : VehicleMessage
    data object ManualEntryUnavailable : VehicleMessage
    data class MakeUnavailable(val make: MakeId) : VehicleMessage
    data object Illustration : VehicleMessage
    data object CatalogUnavailable : VehicleMessage
    data object InvalidParameters : VehicleMessage
    data class OperationUnavailable(val operation: VehicleOperation) : VehicleMessage
    data object OperationFailed : VehicleMessage
}

data class GarageState(
    val selectedCategory: Int = 0,
    val selectedVehicle: VehicleId = VehicleId.Mustang,
    val showPersonal: Boolean = true,
    val showTruck: Boolean = true,
    val showSpecial: Boolean = true,
    val message: VehicleMessage? = null,
)

class GarageViewModel(
    private val session: SessionRepository,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow(GarageState(selectedVehicle = session.state.value.selectedVehicle))
    val state = mutableState.asStateFlow()
    init {
        (scope ?: viewModelScope).launch {
            session.state.collect { shared -> mutableState.update { it.copy(selectedVehicle = shared.selectedVehicle) } }
        }
    }
    fun selectCategory(index: Int) {
        if (index !in 0..3) return
        mutableState.update {
            it.copy(selectedCategory = index, showPersonal = index in 0..1,
                showTruck = index == 0 || index == 2, showSpecial = index == 0 || index == 3)
        }
    }
    fun selectVehicle(vehicle: VehicleId): Boolean {
        val selected = SelectGarageVehicle(session)(vehicle)
        mutableState.update {
            it.copy(selectedVehicle = session.state.value.selectedVehicle,
                message = if (selected) null else VehicleMessage.MembershipRequired)
        }
        return selected
    }
}

data class AddVehicleState(
    val query: String = "",
    val vehicleClass: Int = 0,
    val specialClass: Int = -1,
    val helpVisible: Boolean = false,
    val makes: List<VehicleMake> = emptyList(),
    val selectedMake: MakeId? = null,
    val message: VehicleMessage? = null,
)

class AddVehicleViewModel(private val vehicles: VehicleRepository) : ViewModel() {
    private val filter = FilterVehicleMakes(vehicles)
    private val mutableState = MutableStateFlow(AddVehicleState(makes = filter("", VehicleCategory.Passenger)))
    val state = mutableState.asStateFlow()
    private fun update(transform: (AddVehicleState) -> AddVehicleState) {
        mutableState.update {
            val next = transform(it)
            next.copy(makes = filter(next.query, when (next.vehicleClass) {
                0 -> VehicleCategory.Passenger
                1 -> VehicleCategory.Truck
                else -> VehicleCategory.Special
            }))
        }
    }
    fun search(query: String) = update { it.copy(query = query, message = null) }
    fun selectClass(index: Int) {
        if (index !in 0..2) return
        update { it.copy(vehicleClass = index, specialClass = -1, selectedMake = null, message = null) }
    }
    fun selectSpecialClass(index: Int) {
        if (index !in 0..2) return
        update {
            val selected = if (it.specialClass == index) -1 else index
            it.copy(specialClass = selected,
                vehicleClass = if (selected >= 0) 2 else it.vehicleClass, message = null, selectedMake = null)
        }
    }
    fun toggleHelp() = update { it.copy(helpVisible = !it.helpVisible) }
    fun manualEntry() = update { it.copy(message = VehicleMessage.ManualEntryUnavailable) }
    fun chooseMake(id: MakeId): Boolean {
        if (state.value.makes.none { it.id == id }) return false
        update {
            it.copy(selectedMake = id, message = if (id == MakeId.Ford) null else VehicleMessage.MakeUnavailable(id))
        }
        return id == MakeId.Ford
    }
}

data class FordConfigurationState(
    val specification: FordSpecification,
    val message: VehicleMessage? = null,
) {
    val modelIndex get() = specification.model.ordinal
    val yearIndex get() = specification.year - 2022
    val selectedTrim get() = specification.trim.ordinal
    val catalogAvailable get() = specification.model == FordModel.Mustang
}

class FordConfigurationViewModel(
    private val session: SessionRepository,
    private val vehicles: VehicleRepository,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow(FordConfigurationState(vehicles.fordDraft.value))
    val state = mutableState.asStateFlow()
    init {
        (scope ?: viewModelScope).launch {
            vehicles.fordDraft.collect { draft -> mutableState.update { it.copy(specification = draft) } }
        }
    }
    fun selectModel(index: Int) {
        val model = FordModel.entries.getOrNull(index) ?: return
        mutableState.update { it.copy(specification = it.specification.copy(model = model, trim = FordTrim.GT), message = null) }
    }
    fun selectYear(index: Int) {
        if (index !in 0..3) return
        mutableState.update { it.copy(specification = it.specification.copy(year = index + 2022), message = null) }
    }
    fun nextTrim() {
        if (!state.value.catalogAvailable) return
        mutableState.update {
            it.copy(specification = it.specification.copy(
                trim = if (it.specification.trim == FordTrim.GT) FordTrim.EcoBoost else FordTrim.GT), message = null)
        }
    }
    fun showHelp() = mutableState.update { it.copy(message = VehicleMessage.Illustration) }
    fun validateParameters(): Boolean {
        val prepared = PrepareFordProfile(vehicles, session)(state.value.specification)
        mutableState.update { it.copy(message = if (prepared) null else VehicleMessage.CatalogUnavailable) }
        return prepared
    }
}

data class FordParameterFields(val height: String, val width: String, val length: String, val mass: String) {
    fun dimensions(): VehicleDimensions? {
        fun parse(value: String) = value.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
        return VehicleDimensions(parse(height) ?: return null, parse(width) ?: return null,
            parse(length) ?: return null, parse(mass) ?: return null)
    }
    companion object {
        fun from(value: VehicleDimensions) = FordParameterFields(
            value.height.toString(), value.width.toString(), value.length.toString(), value.mass.toString())
    }
}
enum class FordParameter { Height, Width, Length, Mass }

data class VehicleProfileState(
    val vehicle: VehicleId,
    val ford: FordSpecification,
    val fields: FordParameterFields,
    val truckConfiguration: VehicleConfiguration?,
    val actualLoad: String = "36.8",
    val actualLoadValid: Boolean = true,
    val personalParametersValid: Boolean = true,
    val avoidTolls: Boolean = false,
    val helpVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val message: VehicleMessage? = null,
) {
    val isPersonal get() = vehicle == VehicleId.Mustang
    val saveEnabled get() = !isSubmitting && if (isPersonal) personalParametersValid else actualLoadValid
}

class VehicleProfileViewModel(
    private val session: SessionRepository,
    private val vehicles: VehicleRepository,
    operations: VehicleOperationsRepository,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val actionScope = scope ?: viewModelScope
    private val perform = PerformVehicleOperation(vehicles, operations)
    private val mutableState = MutableStateFlow(VehicleProfileState(
        vehicle = session.state.value.selectedVehicle, ford = vehicles.fordDraft.value,
        fields = FordParameterFields.from(vehicles.fordDraft.value.dimensions),
        truckConfiguration = vehicles.configurations(VehicleId.Man).find { it.id == ConfigurationId.Loaded },
        personalParametersValid = ValidateFordSpecification()(vehicles.fordDraft.value),
        avoidTolls = session.state.value.routingSettings.avoidTolls))
    val state = mutableState.asStateFlow()
    init {
        actionScope.launch {
            combine(session.state, vehicles.fordDraft) { shared, ford -> shared to ford }.collect { (shared, ford) ->
                mutableState.update {
                    it.copy(vehicle = shared.selectedVehicle, ford = ford,
                        fields = if (it.ford != ford) FordParameterFields.from(ford.dimensions) else it.fields,
                        personalParametersValid = if (it.ford != ford) ValidateFordSpecification()(ford) else it.personalParametersValid,
                        avoidTolls = shared.routingSettings.avoidTolls,
                        message = if (it.vehicle != shared.selectedVehicle || it.ford != ford) null else it.message)
                }
            }
        }
    }
    fun toggleHelp() = mutableState.update { it.copy(helpVisible = !it.helpVisible) }
    fun changeActualLoad(value: String) {
        if (session.state.value.selectedVehicle != VehicleId.Man) return
        mutableState.update { it.copy(actualLoad = value,
            actualLoadValid = ValidateTruckLoad()(value, it.truckConfiguration?.maximumMass ?: 0.0) != null, message = null) }
    }
    fun changeFordParameter(parameter: FordParameter, value: String) {
        if (session.state.value.selectedVehicle != VehicleId.Mustang) return
        mutableState.update {
            val fields = when (parameter) {
                FordParameter.Height -> it.fields.copy(height = value)
                FordParameter.Width -> it.fields.copy(width = value)
                FordParameter.Length -> it.fields.copy(length = value)
                FordParameter.Mass -> it.fields.copy(mass = value)
            }
            val dimensions = fields.dimensions()
            it.copy(fields = fields, ford = if (dimensions != null) it.ford.copy(dimensions = dimensions) else it.ford,
                personalParametersValid =
                dimensions != null && ValidateFordSpecification()(it.ford.copy(dimensions = dimensions)), message = null)
        }
        if (state.value.personalParametersValid) vehicles.updateFordDraft(state.value.ford)
    }
    fun toggleAvoidTolls() {
        session.update { it.copy(routingSettings = it.routingSettings.copy(avoidTolls = !it.routingSettings.avoidTolls)) }
        mutableState.update { it.copy(avoidTolls = session.state.value.routingSettings.avoidTolls, message = null) }
    }
    fun save() = submit(VehicleOperation.SaveProfile)
    fun requestDispatcherChange() = submit(VehicleOperation.RequestDispatcherChange)
    private fun submit(operation: VehicleOperation) {
        val current = state.value
        if (current.isSubmitting || session.state.value.selectedVehicle != current.vehicle) return
        if ((operation == VehicleOperation.SaveProfile && !current.saveEnabled) ||
            (operation == VehicleOperation.RequestDispatcherChange && current.isPersonal)) {
            mutableState.update { it.copy(message = VehicleMessage.InvalidParameters) }
            return
        }
        val ford = current.fields.dimensions()?.let { current.ford.copy(dimensions = it) }
        val request = VehicleOperationRequest(operation, current.vehicle,
            configuration = if (current.isPersonal) null else ConfigurationId.Loaded,
            ford = if (current.isPersonal) ford else null,
            actualLoad = if (current.isPersonal) null else ValidateTruckLoad()(current.actualLoad, current.truckConfiguration?.maximumMass ?: 0.0),
            avoidTolls = session.state.value.routingSettings.avoidTolls)
        mutableState.update { it.copy(isSubmitting = true, message = null) }
        actionScope.launch {
            try {
                val result = perform(request)
                mutableState.update { it.copy(message = if (it.vehicle == request.vehicle) result.message(operation) else null) }
            } finally {
                mutableState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}

data class VehicleConfigurationsState(
    val vehicle: VehicleId,
    val ford: FordSpecification,
    val configurations: List<VehicleConfiguration>,
    val selected: ConfigurationId = ConfigurationId.Loaded,
    val helpVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val message: VehicleMessage? = null,
) {
    val isPersonal get() = vehicle == VehicleId.Mustang
    val selectedIndex get() = selected.ordinal
    val selectedConfiguration get() = configurations.find { it.id == selected }
}

class VehicleConfigurationsViewModel(
    private val session: SessionRepository,
    private val vehicles: VehicleRepository,
    operations: VehicleOperationsRepository,
    scope: CoroutineScope? = null,
) : ViewModel() {
    private val actionScope = scope ?: viewModelScope
    private val perform = PerformVehicleOperation(vehicles, operations)
    private val mutableState = MutableStateFlow(VehicleConfigurationsState(
        session.state.value.selectedVehicle, vehicles.fordDraft.value,
        vehicles.configurations(session.state.value.selectedVehicle)))
    val state = mutableState.asStateFlow()
    init {
        actionScope.launch {
            combine(session.state, vehicles.fordDraft) { shared, ford -> shared to ford }.collect { (shared, ford) ->
                mutableState.update { it.copy(vehicle = shared.selectedVehicle, ford = ford,
                    configurations = vehicles.configurations(shared.selectedVehicle),
                    message = if (it.vehicle != shared.selectedVehicle) null else it.message) }
            }
        }
    }
    fun selectConfiguration(index: Int) {
        val configuration = state.value.configurations.getOrNull(index) ?: return
        mutableState.update { it.copy(selected = configuration.id, message = null) }
    }
    fun toggleHelp(): Boolean {
        mutableState.update { it.copy(helpVisible = !it.helpVisible) }
        return state.value.helpVisible
    }
    fun applyConfiguration() = submit(VehicleOperation.ApplyConfiguration)
    fun requestDispatcherChange() = submit(VehicleOperation.RequestDispatcherChange)
    private fun submit(operation: VehicleOperation) {
        val current = state.value
        if (current.isSubmitting || session.state.value.selectedVehicle != current.vehicle) return
        if (current.isPersonal) {
            mutableState.update { it.copy(message = VehicleMessage.OperationUnavailable(operation)) }
            return
        }
        val request = VehicleOperationRequest(operation, current.vehicle, current.selected,
            avoidTolls = session.state.value.routingSettings.avoidTolls)
        mutableState.update { it.copy(isSubmitting = true, message = null) }
        actionScope.launch {
            try {
                val result = perform(request)
                mutableState.update { it.copy(message = if (it.vehicle == request.vehicle) result.message(operation) else null) }
            } finally {
                mutableState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}

private fun VehicleOperationResult.message(operation: VehicleOperation): VehicleMessage = when (this) {
    VehicleOperationResult.NotConfigured -> VehicleMessage.OperationUnavailable(operation)
    VehicleOperationResult.Invalid -> VehicleMessage.InvalidParameters
    VehicleOperationResult.Failed -> VehicleMessage.OperationFailed
}
