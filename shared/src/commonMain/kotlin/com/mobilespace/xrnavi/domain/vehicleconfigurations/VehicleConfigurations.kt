package com.mobilespace.xrnavi.domain.vehicleconfigurations

internal enum class VehicleConfigurationAvailability {
    Available,
    Incomplete
}

internal data class VehicleConfiguration(
    val id: String,
    val vehicleId: String,
    val name: String,
    val summary: String,
    val availability: VehicleConfigurationAvailability
)

internal interface VehicleConfigurationRepository {
    fun configurations(): List<VehicleConfiguration>
}

internal data class VehicleConfigurationDraft(
    val vehicleId: String,
    val appliedConfigurationId: String?,
    val selectedConfigurationId: String?
) {
    fun select(configuration: VehicleConfiguration): VehicleConfigurationDraft =
        if (configuration.vehicleId == vehicleId) {
            copy(selectedConfigurationId = configuration.id)
        } else {
            this
        }

    fun cancel(): VehicleConfigurationDraft =
        copy(selectedConfigurationId = appliedConfigurationId)

    val hasPendingChange: Boolean
        get() = selectedConfigurationId != appliedConfigurationId
}

internal data class VehicleConfigurationApplyResult(
    val selections: Map<String, String>,
    val applied: Boolean,
    val parametersChanged: Boolean,
    val notice: VehicleConfigurationNotice
)

internal enum class VehicleConfigurationNotice {
    BelongsToAnotherVehicle,
    NotConnected,
    Incomplete,
    AppliedWithUnconfirmedParameters,
    AlreadyApplied
}

internal fun vehicleConfigurationsFor(
    vehicleId: String,
    configurations: List<VehicleConfiguration>
): List<VehicleConfiguration> = configurations.filter { it.vehicleId == vehicleId }

internal fun defaultVehicleConfigurationId(
    vehicleId: String,
    configurations: List<VehicleConfiguration>
): String? = vehicleConfigurationsFor(vehicleId, configurations)
    .firstOrNull { it.availability == VehicleConfigurationAvailability.Available }
    ?.id

internal fun defaultVehicleConfigurationSelections(
    configurations: List<VehicleConfiguration>
): Map<String, String> = configurations
    .filter { it.availability == VehicleConfigurationAvailability.Available }
    .associate { it.vehicleId to it.id }

internal fun applyVehicleConfiguration(
    activeVehicleId: String,
    currentSelections: Map<String, String>,
    configuration: VehicleConfiguration,
    availableConfigurations: List<VehicleConfiguration>
): VehicleConfigurationApplyResult {
    val currentId = currentSelections[activeVehicleId]
        ?: defaultVehicleConfigurationId(activeVehicleId, availableConfigurations)
    val invalidNotice = when {
        configuration.vehicleId != activeVehicleId ->
            VehicleConfigurationNotice.BelongsToAnotherVehicle
        availableConfigurations.none { it == configuration } ->
            VehicleConfigurationNotice.NotConnected
        configuration.availability != VehicleConfigurationAvailability.Available ->
            VehicleConfigurationNotice.Incomplete
        else -> null
    }
    if (invalidNotice != null) {
        return VehicleConfigurationApplyResult(
            selections = currentSelections,
            applied = false,
            parametersChanged = false,
            notice = invalidNotice
        )
    }

    val changed = currentId != configuration.id
    return VehicleConfigurationApplyResult(
        selections = currentSelections + (activeVehicleId to configuration.id),
        applied = true,
        parametersChanged = changed,
        notice = if (changed) {
            VehicleConfigurationNotice.AppliedWithUnconfirmedParameters
        } else {
            VehicleConfigurationNotice.AlreadyApplied
        }
    )
}

internal fun vehicleConfigurationFor(
    vehicleId: String,
    selections: Map<String, String>,
    configurations: List<VehicleConfiguration>
): VehicleConfiguration? {
    val selectedId = selections[vehicleId] ?: defaultVehicleConfigurationId(vehicleId, configurations)
    return vehicleConfigurationsFor(vehicleId, configurations).firstOrNull { it.id == selectedId }
}
