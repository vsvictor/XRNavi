package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import androidx.compose.ui.text.TextRange

internal data class VehicleConfigurationOption(
    val id: String,
    val name: String,
    val rawData: JsonObject,
    val popupName: String = name,
)

internal data class VehicleEquipmentTrim(val id: String, val name: String, val rawData: JsonObject)

internal data class VehicleEquipmentDetails(
    val bodies: List<JsonObject>,
    val engines: List<JsonObject>,
    val mileages: List<JsonObject>,
    val exteriorColors: List<JsonObject>,
    val interiorColors: List<JsonObject>,
    val rawData: JsonObject,
)

internal data class VehicleEquipmentState(
    val trims: List<VehicleEquipmentTrim> = emptyList(),
    val selectedTrim: VehicleEquipmentTrim? = null,
    val trimQuery: String = "",
    val trimExpanded: Boolean = false,
    val trimCursor: TextRange = TextRange.Zero,
    val bodies: List<JsonObject> = emptyList(),
    val bodiesLoading: Boolean = false,
    val bodiesError: String = "",
    val engines: List<JsonObject> = emptyList(),
    val enginesLoading: Boolean = false,
    val enginesError: String = "",
    val mileages: List<JsonObject> = emptyList(),
    val mileagesLoading: Boolean = false,
    val mileagesError: String = "",
    val loading: Boolean = false,
    val error: String = "",
    val vinNotice: String = "",
    val configurations: List<VehicleEquipmentDetails> = emptyList(),
    val selections: Map<VehicleConfigurationField, VehicleConfigurationOption> = emptyMap(),
    val queries: Map<VehicleConfigurationField, String> = emptyMap(),
    val expanded: VehicleConfigurationField? = null,
    val engineDetails: VehicleConfigurationOption? = null,
    val cursors: Map<VehicleConfigurationField, TextRange> = emptyMap(),
) {
    val filteredTrims: List<VehicleEquipmentTrim>
        get() = trims.filter { selectedTrim != null || it.name.contains(trimQuery.trim(), ignoreCase = true) }

    private val selectedConfigurations: List<VehicleEquipmentDetails>
        get() = if (selectedTrim == null) configurations else
            listOfNotNull(configurations.getOrNull(trims.indexOfFirst { it.id == selectedTrim.id }))

    fun options(field: VehicleConfigurationField): List<VehicleConfigurationOption> =
        selectedConfigurations.flatMap { details ->
            val filters = selections - field
            if (!details.matches(filters)) emptyList()
            else details.records(field)
                .filter { record ->
                    if (field == VehicleConfigurationField.Engine || field == VehicleConfigurationField.Transmission)
                        details.matchesEngine(record, filters)
                    else true
                }
                .mapNotNull { it.option(field) }
        }.distinctBy { it.id }.sortedBy { it.name }

    fun filteredOptions(field: VehicleConfigurationField): List<VehicleConfigurationOption> =
        options(field).filter {
            selections[field] != null || it.name.contains(
                queries[field].orEmpty().trim(),
                ignoreCase = true
            )
        }

    val compatibleDetails: List<VehicleEquipmentDetails>
        get() = selectedConfigurations.filter { it.matches(selections) }
}

private val engineKeys = listOf(
    "engine_type", "fuel_type", "cylinders", "size", "horsepower_hp", "horsepower_rpm",
    "torque_ft_lbs", "torque_rpm", "valves", "valve_timing", "cam_type",
)

private fun VehicleEquipmentDetails.records(field: VehicleConfigurationField): List<JsonObject> =
    when (field) {
        VehicleConfigurationField.Body -> bodies
        VehicleConfigurationField.Engine, VehicleConfigurationField.Transmission -> engines
        VehicleConfigurationField.ExteriorColor -> exteriorColors
        VehicleConfigurationField.InteriorColor -> interiorColors
    }

private fun JsonObject.option(field: VehicleConfigurationField): VehicleConfigurationOption? {
    fun text(key: String) = (this[key] as? JsonPrimitive)
        ?.takeUnless { it == JsonNull }?.content?.takeIf { it.isNotBlank() }

    val keys = when (field) {
        VehicleConfigurationField.Body -> listOf("type", "doors", "seats")
        VehicleConfigurationField.Engine -> engineKeys
        VehicleConfigurationField.Transmission -> listOf("transmission", "drive_type")
        VehicleConfigurationField.ExteriorColor, VehicleConfigurationField.InteriorColor ->
            listOf("name", "color", "rgb")
    }
    val values = keys.mapNotNull { key -> text(key)?.let { key to it } }.toMap()
    if (values.isEmpty()) return null
    if (field == VehicleConfigurationField.Body && text("type") == null) return null
    if (field == VehicleConfigurationField.Transmission && text("transmission") == null) return null
    val name = when (field) {
        VehicleConfigurationField.Body -> listOfNotNull(
            text("type"), text("doors")?.let { "$it дв." }, text("seats")?.let { "$it місць" },
        ).joinToString(" · ")

        VehicleConfigurationField.Engine -> equipmentFields(JsonObject(
            listOf("fuel_type", "size").mapNotNull { key -> this[key]?.let { key to it } }.toMap(),
        ))
            .joinToString(" · ") { (label, value) -> "$label: $value" }

        VehicleConfigurationField.Transmission -> listOfNotNull(
            text("transmission"),
            text("drive_type")
        )
            .joinToString(" · ")

        VehicleConfigurationField.ExteriorColor, VehicleConfigurationField.InteriorColor ->
            listOfNotNull(text("name") ?: text("color"), text("rgb")).joinToString(" · ")
    }
    if (name.isBlank()) return null
    val identity = JsonObject(values.mapValues { JsonPrimitive(it.value) }).toString()
    return VehicleConfigurationOption(
        identity, name, this,
        popupName = if (field == VehicleConfigurationField.Engine) name.replace(" · ", "\n") else name,
    )
}

private fun VehicleEquipmentDetails.matchesEngine(
    record: JsonObject,
    filters: Map<VehicleConfigurationField, VehicleConfigurationOption>,
): Boolean =
    listOf(VehicleConfigurationField.Engine, VehicleConfigurationField.Transmission).all { field ->
        filters[field]?.let { record.option(field)?.id == it.id } ?: true
    }

private fun VehicleEquipmentDetails.matches(
    filters: Map<VehicleConfigurationField, VehicleConfigurationOption>,
): Boolean {
    val engineFilters = filters.keys.any {
        it == VehicleConfigurationField.Engine || it == VehicleConfigurationField.Transmission
    }
    if (engineFilters && engines.none { matchesEngine(it, filters) }) return false
    return filters.all { (field, selected) ->
        field == VehicleConfigurationField.Engine || field == VehicleConfigurationField.Transmission ||
                records(field).any { it.option(field)?.id == selected.id }
    }
}
