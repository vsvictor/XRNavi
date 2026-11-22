package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import org.jetbrains.compose.resources.DrawableResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.mustang_thumbnail_dark
import xrnavi.shared.generated.resources.mustang_thumbnail_light

internal fun defaultVehiclePhoto(dark: Boolean): DrawableResource =
    if (dark) Res.drawable.mustang_thumbnail_dark else Res.drawable.mustang_thumbnail_light

internal data class VehiclePhotoQuery(val make: String, val model: String, val year: Int)

internal data class VehiclePhoto(
    val url: String,
    val sourceUrl: String,
    val author: String,
    val license: String,
    val licenseUrl: String,
    val title: String,
)

internal fun GarageVehicle.photoQuery(): VehiclePhotoQuery? {
    val data = vehicleData ?: return null
    fun name(key: String) = ((data[key] as? JsonObject)?.get("name") as? JsonPrimitive)
        ?.content?.trim()?.takeIf(String::isNotEmpty)
    val make = name("make") ?: return null
    val model = name("model") ?: return null
    val year = (data["year"] as? JsonPrimitive)?.intOrNull?.takeIf { it > 0 } ?: return null
    return VehiclePhotoQuery(make, model, year)
}
