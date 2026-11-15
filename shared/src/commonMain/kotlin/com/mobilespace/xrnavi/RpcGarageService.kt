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
    ): JsonObject = requestValue(method, session, build) as? JsonObject ?: throw invalid()

    private suspend fun requestValue(
        method: String,
        session: AuthSession,
        build: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit = {},
    ): JsonElement {
        val raw = try {
            call(method, buildJsonObject {
                put("access_token", session.accessToken)
                build()
            }.toString())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw GarageException(GarageException.Kind.Failed, error)
        }
        return try {
            Json.parseToJsonElement(raw)
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
        val snapshot = this["vehicle"]?.let { it as? JsonObject ?: throw invalid() }
        val measures = snapshot?.bodyMeasures()
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
            heightMeters = dim("height_m") ?: measures?.heightMeters,
            widthMeters = dim("width_m") ?: measures?.widthMeters,
            lengthMeters = dim("length_m") ?: measures?.lengthMeters,
            weightKg = (this["weight_kg"] as? JsonPrimitive)?.intOrNull ?: measures?.weightKg,
            vehicleData = snapshot,
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

    override suspend fun decodeVin(session: AuthSession, vin: String): DecodedVehicleVin {
        val result = request("car.vindecoder_index_get", session) {
            put("vin", vin)
            put("verbose", "yes")
            put("all_trims", "yes")
        }
        val year = (result["year"] as? JsonPrimitive)?.takeUnless { it.isString }?.intOrNull
            ?.takeIf { it > 0 } ?: throw invalid()
        return DecodedVehicleVin(result.text("make"), result.text("model"), year, result)
    }

    override suspend fun searchMakes(session: AuthSession): List<VehicleMake> =
        request("car.makes_index2_get", session).list("data").map { element ->
            val item = element.obj()
            val id = (item["id"] as? JsonPrimitive)?.intOrNull ?: throw invalid()
            VehicleMake(id.toString(), item.text("name"), rawData = item)
        }

    override suspend fun searchModels(session: AuthSession, make: String): List<VehicleModelItem> =
        request("car.oemmakemodels_index_get", session) {
            put("make", make)
        }.list("data").map { element ->
            val item = element.obj()
            val id = (item["id"] as? JsonPrimitive)?.intOrNull ?: throw invalid()
            VehicleModelItem(id.toString(), item.text("name"), emptyList(), emptyList(), rawData = item)
        }

    override suspend fun addVehicle(session: AuthSession, vehicle: NewVehicle): GarageVehicle =
        saveVehicle("garage.vehicles.add", session, vehicle)

    override suspend fun updateVehicle(session: AuthSession, vehicleId: String, vehicle: NewVehicle): GarageVehicle =
        saveVehicle("garage.vehicles.update", session, vehicle, vehicleId)

    override suspend fun deleteVehicle(session: AuthSession, vehicleId: String): String? {
        val result = request("garage.vehicles.delete", session) { put("vehicle_id", vehicleId) }
        val active = result["active_vehicle_id"] ?: throw invalid()
        if (active == kotlinx.serialization.json.JsonNull) return null
        return result.text("active_vehicle_id")
    }

    private suspend fun saveVehicle(
        method: String, session: AuthSession, vehicle: NewVehicle, vehicleId: String? = null,
    ): GarageVehicle =
        request(method, session) {
            vehicleId?.let { put("vehicle_id", it) }
            put("class_id", vehicle.classId)
            put("make_id", vehicle.makeId)
            put("model_id", vehicle.modelId)
            put("year", vehicle.year)
            put("trim_id", vehicle.trimId)
            put("vehicle", vehicle.vehicleData)
        }.let { result -> (result["vehicle"] as? JsonObject ?: result).parseVehicle() }

    override suspend fun years(session: AuthSession, make: String, model: String): List<Int> {
        val response = requestValue("car.years_index2_get", session) {
            put("make", make)
            put("model", model)
        }
        val result = if (response is JsonObject) response["result"] else response
        return (result as? JsonArray ?: throw invalid()).map {
            (it as? JsonPrimitive)?.takeUnless { value -> value.isString }?.intOrNull
                ?: throw invalid()
        }
    }

    override suspend fun equipmentTrims(
        session: AuthSession, make: String, model: String, year: Int,
    ): List<VehicleEquipmentTrim> {
        val trims = mutableListOf<VehicleEquipmentTrim>()
        var page = 1
        var pages: Int
        do {
            val payload = request("car.oemtrims_index_get", session) {
                put("make", make)
                put("model", model)
                put("year", year.toString())
                put("page", page)
                put("limit", 100)
            }
            val response = if ("result" in payload) payload["result"] as? JsonObject ?: throw invalid()
                else payload
            trims += response.list("data").map { element ->
                val item = element.obj()
                val id = (item["id"] as? JsonPrimitive)?.intOrNull ?: throw invalid()
                val name = item.text("description")
                VehicleEquipmentTrim(id.toString(), name, item)
            }
            val collection = response["collection"] as? JsonObject ?: throw invalid()
            pages = (collection["pages"] as? JsonPrimitive)?.intOrNull ?: throw invalid()
            if (pages < 0 || (pages > 0 && page > pages)) throw invalid()
            page++
        } while (page <= pages)
        return trims.distinctBy { it.id }
    }

    override suspend fun equipmentBodies(session: AuthSession, trimId: String): List<JsonObject> =
        equipmentRecords("car.makemodeltrimbodies_index2_get", session, trimId)

    override suspend fun equipmentEngines(session: AuthSession, trimId: String): List<JsonObject> =
        equipmentRecords("car.makemodeltrimengines_index2_get", session, trimId)

    override suspend fun equipmentMileages(session: AuthSession, trimId: String): List<JsonObject> =
        equipmentRecords("car.makemodeltrimmileages_index2_get", session, trimId)

    private suspend fun equipmentRecords(
        method: String, session: AuthSession, trimId: String,
    ): List<JsonObject> {
        val records = mutableListOf<JsonObject>()
        var page = 1
        var pages: Int
        do {
            val payload = request(method, session) {
                put("trim_id", trimId)
                put("page", page)
            }
            val response = if ("result" in payload) payload["result"] as? JsonObject ?: throw invalid()
                else payload
            records += response.list("data").map { element ->
                element.obj().also { equipmentFields(it) }
            }
            val collection = response["collection"] as? JsonObject ?: throw invalid()
            pages = (collection["pages"] as? JsonPrimitive)?.intOrNull ?: throw invalid()
            if (pages < 0 || (pages > 0 && page > pages)) throw invalid()
            page++
        } while (page <= pages)
        return records
    }

    override suspend fun equipmentDetails(session: AuthSession, trimId: String): VehicleEquipmentDetails {
        val response = request("car.oemtrims_view_get", session) { put("id", trimId) }
        fun objects(name: String): List<JsonObject> = response.list(name).map {
            it.obj().also { item -> equipmentFields(item) }
        }
        return VehicleEquipmentDetails(
            bodies = objects("bodies"),
            engines = objects("engines"),
            mileages = objects("mileages"),
            exteriorColors = objects("exterior_colors"),
            interiorColors = objects("interior_colors"),
            rawData = response,
        )
    }
}
