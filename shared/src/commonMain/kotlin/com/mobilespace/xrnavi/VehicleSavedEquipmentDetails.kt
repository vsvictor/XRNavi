package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
internal fun VehicleSavedEquipmentDetails(snapshot: JsonObject) {
    val trim = (snapshot["trim"] as? JsonObject)?.get("name") as? JsonPrimitive
    trim?.content?.takeIf { it.isNotBlank() }?.let {
        AddVehicleEquipmentSection("Комплектація", listOf(listOf("Комплектація" to it)))
    }
    fun records(key: String): List<JsonObject> =
        (snapshot[key] as? JsonArray).orEmpty().map {
            it as? JsonObject ?: throw GarageException(GarageException.Kind.InvalidResponse)
        }
    val bodies = records("bodies")
    val engines = records("engines")
    val mileages = records("mileages")
    if (bodies.isNotEmpty()) AddVehicleEquipmentSection("Кузов", bodies.map(::equipmentFields))
    if (engines.isNotEmpty()) {
        AddVehicleEquipmentSection("Двигун", engines.map(::engineEquipmentFields))
        AddVehicleEquipmentSection("Трансмісія", engines.map(::transmissionEquipmentFields))
    }
    if (mileages.isNotEmpty()) {
        AddVehicleEquipmentSection("Витрата палива та запас ходу", mileages.map(::equipmentFields))
    }
}
