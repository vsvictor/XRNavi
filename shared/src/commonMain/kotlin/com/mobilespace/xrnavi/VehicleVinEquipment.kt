package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

internal data class VehicleVinEquipment(
    val trim: VehicleEquipmentTrim,
    val body: JsonObject?,
    val engine: JsonObject?,
    val mileage: JsonObject?,
)

internal fun DecodedVehicleVin.equipment(trims: List<VehicleEquipmentTrim>): VehicleVinEquipment? {
    fun JsonObject.text(key: String) = (this[key] as? JsonPrimitive)
        ?.takeUnless { it == JsonNull }?.content?.takeIf { it.isNotBlank() }

    fun normalized(value: String) = value.trim().lowercase().replace(Regex("\\s+"), " ")
    val trimName = rawData.text("trim")
    val records = (rawData["trims"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
        .filter {
            trimName == null || it.text("name")?.equals(trimName, true) == true || it.text("trim")
                ?.equals(trimName, true) == true
        }

    fun matching(trim: VehicleEquipmentTrim): List<JsonObject> = records.filter { record ->
        val description = record.text("description") ?: return@filter false
        normalized(description) == normalized(trim.name) ||
                trimName?.let { normalized("$it $description") == normalized(trim.name) } == true
    }

    val specs = rawData["specs"] as? JsonObject
    fun compatible(record: JsonObject): Boolean {
        val body = record["make_model_trim_body"] as? JsonObject
        val engine = record["make_model_trim_engine"] as? JsonObject
        return listOf(
            specs?.get("displacement_l") to engine?.get("size"),
            specs?.get("doors") to body?.get("doors"),
            specs?.get("number_of_seats") to body?.get("seats"),
        ).all { (decoded, catalog) ->
            val a = (decoded as? JsonPrimitive)?.doubleOrNull
            val b = (catalog as? JsonPrimitive)?.doubleOrNull
            a == null || b == null || kotlin.math.abs(a - b) < 0.01
        }
    }

    val exact = trims.filter { matching(it).any(::compatible) }
    val candidates = if (exact.isNotEmpty()) exact else trims.filter {
        trimName != null && (it.rawData.text("trim")?.equals(trimName, true) == true ||
                it.name.equals(trimName, true))
    }
    val selected = candidates.singleOrNull() ?: return null
    // VIN trim IDs belong to the legacy catalog; only a matched v2 ID is used for requests.
    val record = matching(selected).filter(::compatible).singleOrNull()
    fun mapped(keys: Map<String, String>): JsonObject? {
        val values = keys.mapNotNull { (source, target) ->
            specs?.get(source)?.takeUnless { it == JsonNull }?.let { target to it }
        }.toMap()
        return values.takeIf { it.isNotEmpty() }?.let(::JsonObject)
    }

    val body = mapped(
        mapOf(
            "curb_weight_pounds" to "curb_weight",
            "doors" to "doors",
            "number_of_seats" to "seats"
        )
    )
    val engine = mapped(
        mapOf(
            "displacement_l" to "size", "fuel_type_primary" to "fuel_type",
            "engine_number_of_cylinders" to "cylinders", "engine_brake_hp_from" to "horsepower_hp",
            "transmission_style" to "transmission", "drive_type" to "drive_type"
        )
    )

    fun combine(base: JsonObject?, specific: JsonObject?): JsonObject? =
        if (base == null && specific == null) null else JsonObject(
            base.orEmpty() + specific.orEmpty().filterValues { it != JsonNull },
        )
    return VehicleVinEquipment(
        selected,
        combine(record?.get("make_model_trim_body") as? JsonObject, body),
        combine(record?.get("make_model_trim_engine") as? JsonObject, engine),
        record?.get("make_model_trim_mileage") as? JsonObject,
    )
}

internal fun JsonObject?.hasVinFields(vararg keys: String): Boolean =
    this != null && keys.all { key ->
        val value = this[key] as? JsonPrimitive
        value != null && value != JsonNull && value.content.isNotBlank()
    }

internal fun mergeVinRecord(records: List<JsonObject>, seed: JsonObject?): List<JsonObject> {
    if (seed == null) return records
    val values = seed.filterValues { it != JsonNull }
        .filterKeys { it != "id" && !it.endsWith("_id") }
    return if (records.isEmpty()) listOf(JsonObject(values))
    else if (records.size == 1) listOf(JsonObject(records.single() + values))
    else records
}
