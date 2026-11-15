package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonObject

internal data class DecodedVehicleVin(
    val make: String,
    val model: String,
    val year: Int,
    val rawData: JsonObject,
)
