package com.mobilespace.xrnavi

import androidx.compose.ui.text.TextRange
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

internal fun GarageVehicle.editState(): AddVehicleState {
    val snapshot = vehicleData ?: throw GarageException(GarageException.Kind.InvalidResponse)
    fun objectField(key: String) = snapshot[key] as? JsonObject
        ?: throw GarageException(GarageException.Kind.InvalidResponse)

    fun JsonObject.text(key: String) =
        (this[key] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
            ?: throw GarageException(GarageException.Kind.InvalidResponse)

    fun records(key: String) = (snapshot[key] as? JsonArray
        ?: throw GarageException(GarageException.Kind.InvalidResponse)).map {
        it as? JsonObject ?: throw GarageException(GarageException.Kind.InvalidResponse)
    }

    val makeRecord = objectField("make")
    val modelRecord = objectField("model")
    val trimRecord = objectField("trim")
    val make = VehicleMake(
        makeRecord.text("id"),
        makeRecord.text("name"),
        makeRecord["raw_data"] as? JsonObject
    )
    val year = (snapshot["year"] as? JsonPrimitive)?.intOrNull
        ?: throw GarageException(GarageException.Kind.InvalidResponse)
    val model = VehicleModelItem(
        modelRecord.text("id"),
        modelRecord.text("name"),
        listOf(year),
        emptyList(),
        modelRecord["raw_data"] as? JsonObject
    )
    val trim = VehicleEquipmentTrim(
        trimRecord.text("id"),
        trimRecord.text("name"),
        trimRecord["raw_data"] as? JsonObject ?: JsonObject(emptyMap())
    )
    val details = VehicleEquipmentDetails(
        records("bodies"), records("engines"), records("mileages"),
        records("exterior_colors"), records("interior_colors"),
        snapshot["equipment_data"] as? JsonObject ?: JsonObject(emptyMap()),
    )
    var equipment = VehicleEquipmentState(
        trims = listOf(trim), selectedTrim = trim, trimQuery = trim.name,
        trimCursor = TextRange(trim.name.length), configurations = listOf(details),
        bodies = details.bodies, engines = details.engines, mileages = details.mileages,
    )
    val colors = listOf(
        VehicleConfigurationField.ExteriorColor to "selected_exterior_color",
        VehicleConfigurationField.InteriorColor to "selected_interior_color",
    ).mapNotNull { (field, key) ->
        val selected = snapshot[key] as? JsonObject ?: return@mapNotNull null
        equipment.options(field).firstOrNull { it.rawData == selected }?.let { field to it }
    }.toMap()
    equipment = equipment.copy(
        selections = colors, queries = colors.mapValues { it.value.name },
        cursors = colors.mapValues { TextRange(it.value.name.length) },
    )
    val vin = (snapshot["vin"] as? JsonPrimitive)?.takeUnless { it == JsonNull }?.content.orEmpty()
    val vinData = snapshot["vin_data"] as? JsonObject
    return AddVehicleState(
        open = true, editingVehicle = this, step = AddVehicleStep.Details,
        selectedMake = make, selectedModel = model, year = year,
        searchQuery = model.name, makeSelection = TextRange(make.name.length),
        modelSelection = TextRange(model.name.length), makeResults = listOf(make),
        models = listOf(model), equipment = equipment,
        selectedClass = VehicleClassItem(
            category.ifBlank { "passenger" },
            garageCategoryLabel(category)
        ),
        vinQuery = vin, vinSearched = vin.isNotEmpty() && vinData != null,
        decodedVin = vinData?.let { DecodedVehicleVin(make.name, model.name, year, it) },
    )
}
