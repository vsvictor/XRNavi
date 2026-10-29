package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal enum class VehicleConfigurationAvailability {
    Available,
    Incomplete
}

internal data class DemoVehicleConfiguration(
    val id: String,
    val vehicleId: String,
    val name: String,
    val summary: String,
    val availability: VehicleConfigurationAvailability,
    val synthetic: Boolean
)

internal data class VehicleConfigurationDraft(
    val vehicleId: String,
    val appliedConfigurationId: String?,
    val selectedConfigurationId: String?
) {
    fun select(configuration: DemoVehicleConfiguration): VehicleConfigurationDraft =
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

internal fun vehicleConfigurationsFor(vehicleId: String): List<DemoVehicleConfiguration> {
    val vehicle = demoVehicleById(vehicleId) ?: return emptyList()
    val baseSummary = if (vehicle.id == demoVehicle.id) {
        vehicle.specification
    } else {
        "Габарити: Не задано · маса: Не задано"
    }
    return listOf(
        DemoVehicleConfiguration(
            id = "$vehicleId-base",
            vehicleId = vehicleId,
            name = "Базова демоконфігурація",
            summary = baseSummary,
            availability = VehicleConfigurationAvailability.Available,
            synthetic = false
        ),
        DemoVehicleConfiguration(
            id = "$vehicleId-synthetic-02",
            vehicleId = vehicleId,
            name = "Синтетичний демосценарій 02",
            summary = "Габарити: Не задано · маса: Не задано",
            availability = VehicleConfigurationAvailability.Available,
            synthetic = true
        ),
        DemoVehicleConfiguration(
            id = "$vehicleId-incomplete",
            vehicleId = vehicleId,
            name = "Демоконфігурація з неповними даними",
            summary = "Дані конфігурації не задано; застосування недоступне.",
            availability = VehicleConfigurationAvailability.Incomplete,
            synthetic = true
        )
    )
}

internal fun defaultVehicleConfigurationId(vehicleId: String): String? =
    vehicleConfigurationsFor(vehicleId)
        .firstOrNull { it.availability == VehicleConfigurationAvailability.Available }
        ?.id

internal fun defaultVehicleConfigurationSelections(): Map<String, String> =
    demoVehicles.mapNotNull { vehicle ->
        defaultVehicleConfigurationId(vehicle.id)?.let { vehicle.id to it }
    }.toMap()

internal data class VehicleConfigurationApplyResult(
    val selections: Map<String, String>,
    val applied: Boolean,
    val parametersChanged: Boolean,
    val message: String
)

internal fun applyVehicleConfiguration(
    activeVehicleId: String,
    currentSelections: Map<String, String>,
    configuration: DemoVehicleConfiguration
): VehicleConfigurationApplyResult {
    val currentId = currentSelections[activeVehicleId]
        ?: defaultVehicleConfigurationId(activeVehicleId)
    val invalidMessage = when {
        configuration.vehicleId != activeVehicleId ->
            "Конфігурація належить іншому автомобілю. Спочатку явно оберіть цей транспорт у гаражі."
        vehicleConfigurationsFor(activeVehicleId).none { it == configuration } ->
            "Конфігурація недоступна для цього демопрофілю."
        configuration.availability != VehicleConfigurationAvailability.Available ->
            "Конфігурація має неповні дані й не може бути застосована."
        else -> null
    }
    if (invalidMessage != null) {
        return VehicleConfigurationApplyResult(
            selections = currentSelections,
            applied = false,
            parametersChanged = false,
            message = invalidMessage
        )
    }

    val changed = currentId != configuration.id
    return VehicleConfigurationApplyResult(
        selections = currentSelections + (activeVehicleId to configuration.id),
        applied = true,
        parametersChanged = changed,
        message = if (changed) {
            "Демонстраційну конфігурацію застосовано. Її параметри не підтверджені."
        } else {
            "Ця конфігурація вже застосована; параметри не змінено."
        }
    )
}

internal fun vehicleConfigurationFor(
    vehicleId: String,
    selections: Map<String, String>
): DemoVehicleConfiguration? {
    val selectedId = selections[vehicleId] ?: defaultVehicleConfigurationId(vehicleId)
    return vehicleConfigurationsFor(vehicleId).firstOrNull { it.id == selectedId }
}

internal fun DemoVehicleProfile.withConfiguration(
    configuration: DemoVehicleConfiguration?
): DemoVehicleProfile = if (configuration == null) this else copy(
    configurationName = configuration.name,
    specification = configuration.summary
)

internal fun encodeVehicleConfigurationSelections(
    selections: Map<String, String>
): List<String> = selections.flatMap { listOf(it.key, it.value) }

internal fun decodeVehicleConfigurationSelections(values: List<String>): Map<String, String> =
    values.chunked(2).filter { it.size == 2 }.associate { it[0] to it[1] }

internal val vehicleConfigurationSelectionsSaver = listSaver<Map<String, String>, String>(
    save = { selections -> encodeVehicleConfigurationSelections(selections) },
    restore = { values -> decodeVehicleConfigurationSelections(values) }
)
