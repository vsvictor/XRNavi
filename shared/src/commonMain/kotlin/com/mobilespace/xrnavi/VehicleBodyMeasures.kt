package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.math.roundToInt

internal data class VehicleBodyMeasures(
    val heightMeters: Double?,
    val widthMeters: Double?,
    val lengthMeters: Double?,
    val weightKg: Int?,
)

internal fun JsonObject.bodyMeasures(): VehicleBodyMeasures {
    val bodies = (this["bodies"] as? JsonArray).orEmpty().map {
        it as? JsonObject ?: throw GarageException(GarageException.Kind.InvalidResponse)
    }
    fun measure(key: String, factor: Double): Double? =
        bodies.firstNotNullOfOrNull { body ->
            val value = body[key]
            if (value == null || value == JsonNull) return@firstNotNullOfOrNull null
            val number = (value as? JsonPrimitive)?.doubleOrNull
                ?: throw GarageException(GarageException.Kind.InvalidResponse)
            val converted = number * factor
            if (!converted.isFinite() || converted < 0) {
                throw GarageException(GarageException.Kind.InvalidResponse)
            }
            converted.takeIf { it > 0 }
        }
    val weight = measure("curb_weight", 0.45359237)
    if (weight != null && weight > Int.MAX_VALUE) {
        throw GarageException(GarageException.Kind.InvalidResponse)
    }
    return VehicleBodyMeasures(
        heightMeters = measure("height", 0.0254),
        widthMeters = measure("width", 0.0254),
        lengthMeters = measure("length", 0.0254),
        weightKg = weight?.roundToInt(),
    )
}
