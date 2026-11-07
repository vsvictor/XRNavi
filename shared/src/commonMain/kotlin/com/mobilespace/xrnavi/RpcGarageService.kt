package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put

internal class RpcGarageService(
    private val call: suspend (method: String, paramsJson: String) -> String,
) : GarageService {
    private suspend fun request(
        method: String,
        session: AuthSession,
        build: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit = {},
    ): JsonObject {
        val raw = try {
            call(method, buildJsonObject {
                put("access_token", session.accessToken)
                build()
            }.toString())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            throw GarageException(GarageException.Kind.Failed)
        }
        return try {
            Json.parseToJsonElement(raw) as? JsonObject
        } catch (_: Exception) {
            null
        } ?: throw invalid()
    }

    private fun invalid() = GarageException(GarageException.Kind.InvalidResponse)

    private fun JsonObject.text(name: String): String =
        (this[name] as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }
            ?: throw invalid()

    private fun JsonObject.optionalText(name: String): String? =
        (this[name] as? JsonPrimitive)?.takeIf { it.isString }?.content

    private fun JsonObject.list(name: String): JsonArray = this[name] as? JsonArray ?: throw invalid()

    private fun JsonElement.obj(): JsonObject = this as? JsonObject ?: throw invalid()

    private fun JsonObject.parseVehicle(): GarageVehicle {
        val dimensions = this["dimensions"] as? JsonObject
        fun dim(name: String) = (dimensions?.get(name) as? JsonPrimitive)?.doubleOrNull
        return GarageVehicle(
            id = text("id"),
            name = text("name"),
            category = optionalText("category").orEmpty(),
            verification = when (optionalText("verification")) {
                "verified" -> VerificationStatus.Verified
                "pending" -> VerificationStatus.Pending
                "rejected" -> VerificationStatus.Rejected
                else -> VerificationStatus.None
            },
            heightMeters = dim("height_m"),
            widthMeters = dim("width_m"),
            lengthMeters = dim("length_m"),
            weightKg = (this["weight_kg"] as? JsonPrimitive)?.intOrNull,
        )
    }

    override suspend fun vehicles(session: AuthSession): GarageSnapshot {
        val result = request("garage.vehicles.list", session)
        return GarageSnapshot(
            vehicles = result.list("vehicles").map { it.obj().parseVehicle() },
            activeVehicleId = result.optionalText("active_vehicle_id"),
        )
    }

    override suspend fun setActive(session: AuthSession, vehicleId: String): String =
        request("garage.vehicles.set_active", session) { put("vehicle_id", vehicleId) }
            .text("active_vehicle_id")

    override suspend fun classes(session: AuthSession): List<VehicleClassItem> =
        request("catalog.classes.list", session).list("classes").map {
            it.obj().let { item -> VehicleClassItem(item.text("id"), item.text("name")) }
        }

    override suspend fun makes(session: AuthSession, classId: String): List<VehicleMake> =
        request("catalog.makes.list", session) { put("class_id", classId) }.list("makes").map {
            it.obj().let { item -> VehicleMake(item.text("id"), item.text("name")) }
        }

    override suspend fun models(session: AuthSession, makeId: String): List<VehicleModelItem> =
        request("catalog.models.list", session) { put("make_id", makeId) }.list("models").map { element ->
            val item = element.obj()
            VehicleModelItem(
                id = item.text("id"),
                name = item.text("name"),
                years = item.list("years").map { (it as? JsonPrimitive)?.intOrNull ?: throw invalid() },
                trims = item.list("trims").map {
                    it.obj().let { trim -> VehicleTrim(trim.text("id"), trim.text("name")) }
                },
            )
        }

    override suspend fun addVehicle(session: AuthSession, vehicle: NewVehicle): GarageVehicle =
        request("garage.vehicles.add", session) {
            put("class_id", vehicle.classId)
            put("make_id", vehicle.makeId)
            put("model_id", vehicle.modelId)
            put("year", vehicle.year)
            put("trim_id", vehicle.trimId)
        }.let { result -> (result["vehicle"] as? JsonObject ?: result).parseVehicle() }
}
