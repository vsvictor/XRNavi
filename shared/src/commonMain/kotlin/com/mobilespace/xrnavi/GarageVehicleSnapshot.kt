package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun AddVehicleState.vehicleSnapshot(): JsonObject {
    val make = requireNotNull(selectedMake)
    val model = requireNotNull(selectedModel)
    val selectedYear = requireNotNull(year)
    val selectedTrim = equipment.selectedTrim
    val trimId = selectedTrim?.id ?: trim?.id
    val trimName = selectedTrim?.name ?: trim?.name
    val details =
        equipment.configurations.getOrNull(equipment.trims.indexOfFirst { it.id == trimId })
    val decoded = decodedVin?.takeIf {
        vinSearched && it.make.equals(make.name, true) &&
                it.model.equals(model.name, true) && it.year == selectedYear
    }
    val measures = buildJsonObject { put("bodies", JsonArray(equipment.bodies)) }.bodyMeasures()
    fun record(id: String, name: String, raw: JsonObject?) = buildJsonObject {
        put("id", id)
        put("name", name)
        put("raw_data", raw ?: JsonObject(emptyMap()))
    }
    return buildJsonObject {
        editingVehicle?.vehicleData?.filterKeys { it !in setOf("dimensions", "weight_kg") }
            ?.forEach { (key, value) ->
                put(key, value)
            }
        put("make", record(make.id, make.name, make.rawData))
        put("model", record(model.id, model.name, model.rawData))
        put("year", selectedYear)
        put(
            "trim",
            if (trimId != null && trimName != null) record(
                trimId,
                trimName,
                selectedTrim?.rawData
            ) else JsonNull
        )
        put("bodies", JsonArray(equipment.bodies))
        put("dimensions", buildJsonObject {
            measures.heightMeters?.let { put("height_m", it) }
            measures.widthMeters?.let { put("width_m", it) }
            measures.lengthMeters?.let { put("length_m", it) }
        })
        measures.weightKg?.let { put("weight_kg", it) }
        put("engines", JsonArray(equipment.engines))
        put("mileages", JsonArray(equipment.mileages))
        put("exterior_colors", JsonArray(details?.exteriorColors.orEmpty()))
        put("interior_colors", JsonArray(details?.interiorColors.orEmpty()))
        put(
            "selected_exterior_color",
            equipment.selections[VehicleConfigurationField.ExteriorColor]?.rawData ?: JsonNull
        )
        put(
            "selected_interior_color",
            equipment.selections[VehicleConfigurationField.InteriorColor]?.rawData ?: JsonNull
        )
        put("vin", decoded?.let { JsonPrimitive(vinQuery) } ?: JsonNull)
        put("vin_data", decoded?.rawData ?: JsonNull)
        put("equipment_data", details?.rawData ?: JsonObject(emptyMap()))
    }
}
