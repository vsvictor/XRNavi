package com.mobilespace.xrnavi

internal class FakeGarageService : GarageService {
    var fail = false
    var failModels = false
    var failAddVehicle = false
    var submittedVehicle: NewVehicle? = null
    var addVehicleResponse: kotlinx.coroutines.CompletableDeferred<GarageVehicle>? = null
    var updatedVehicleId: String? = null
    var deletedVehicleId: String? = null
    var deleteResponse: kotlinx.coroutines.CompletableDeferred<String?>? = null
    var failVinDecode = false
    var decodedVin = DecodedVehicleVin("Toyota", "Focus", 2021, kotlinx.serialization.json.JsonObject(emptyMap()))
    var catalogClasses = listOf(VehicleClassItem("c", "Легкові"))
    var requestedMakeClassId: String? = null
    var requestedVin: String? = null
    var vinDecodeCalls = 0
    var vinDecodeResponse: kotlinx.coroutines.CompletableDeferred<Unit>? = null
    var failMakeSearch = false
    var searchMakesResult = listOf(VehicleMake("1", "Toyota"), VehicleMake("2", "Ford"))
    var makeSearchCalls = 0
    var makeSearchResponse: kotlinx.coroutines.CompletableDeferred<Unit>? = null
    var requestedModelMake: String? = null
    var requestedYears: Pair<String, String>? = null
    var yearsResult = listOf(2020, 2021)
    var yearsResponse: kotlinx.coroutines.CompletableDeferred<List<Int>>? = null
    var failYears = false
    var failEquipment = false
    var failBodies = false
    var failEngines = false
    var failMileages = false
    var requestedMileageTrim: String? = null
    var mileagesResponse: kotlinx.coroutines.CompletableDeferred<List<kotlinx.serialization.json.JsonObject>>? = null
    var mileagesResult = listOf(kotlinx.serialization.json.Json.parseToJsonElement("""{"combined_mpg":"30","epa_city_mpg":"25","epa_highway_mpg":"35"}""") as kotlinx.serialization.json.JsonObject)
    var requestedEngineTrim: String? = null
    var enginesResponse: kotlinx.coroutines.CompletableDeferred<List<kotlinx.serialization.json.JsonObject>>? = null
    var enginesResult = listOf(kotlinx.serialization.json.Json.parseToJsonElement("""{"fuel_type":"Gasoline","size":"2.5","horsepower_hp":200}""") as kotlinx.serialization.json.JsonObject)
    var requestedBodyTrim: String? = null
    var bodiesResponse: kotlinx.coroutines.CompletableDeferred<List<kotlinx.serialization.json.JsonObject>>? = null
    var bodiesResult = listOf(kotlinx.serialization.json.Json.parseToJsonElement("""{"type":"Sedan","doors":4}""") as kotlinx.serialization.json.JsonObject)
    var requestedEquipment: Triple<String, String, Int>? = null
    var requestedEquipmentTrim: String? = null
    var equipmentResponse: kotlinx.coroutines.CompletableDeferred<List<VehicleEquipmentTrim>>? = null
    var equipmentDetailsResponse: kotlinx.coroutines.CompletableDeferred<VehicleEquipmentDetails>? = null
    var equipmentTrimsResult = listOf(VehicleEquipmentTrim(
        "10", "SE", kotlinx.serialization.json.JsonObject(emptyMap()),
    ))
    var equipmentDetailsResult = VehicleEquipmentDetails(
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
        kotlinx.serialization.json.JsonObject(emptyMap()),
    )
    var vehicles = listOf(
        GarageVehicle("a", "Ford Focus", "passenger", VerificationStatus.Verified),
        GarageVehicle("t", "Volvo FH", "truck", VerificationStatus.Pending),
    )
    var active: String? = "a"
    private fun check() { if (fail) throw GarageException(GarageException.Kind.Failed) }
    override suspend fun vehicles(session: AuthSession): GarageSnapshot { check(); return GarageSnapshot(vehicles, active) }
    override suspend fun setActive(session: AuthSession, vehicleId: String): String { check(); active = vehicleId; return vehicleId }
    override suspend fun classes(session: AuthSession): List<VehicleClassItem> { check(); return catalogClasses }
    override suspend fun makes(session: AuthSession, classId: String): List<VehicleMake> {
        check()
        requestedMakeClassId = classId
        return listOf(VehicleMake("m", "Ford"))
    }
    override suspend fun models(session: AuthSession, makeId: String): List<VehicleModelItem> {
        check()
        if (failModels) throw GarageException(GarageException.Kind.Failed)
        return listOf(VehicleModelItem("mo", "Focus", listOf(2020, 2021), listOf(VehicleTrim("tr", "Titanium"))))
    }
    override suspend fun decodeVin(session: AuthSession, vin: String): DecodedVehicleVin {
        vinDecodeCalls++
        vinDecodeResponse?.await()
        check()
        if (failVinDecode) throw GarageException(GarageException.Kind.Failed)
        requestedVin = vin
        return decodedVin
    }
    override suspend fun searchModels(session: AuthSession, make: String): List<VehicleModelItem> {
        requestedModelMake = make
        return models(session, make)
    }
    override suspend fun searchMakes(session: AuthSession): List<VehicleMake> {
        makeSearchCalls++
        makeSearchResponse?.await()
        check()
        if (failMakeSearch) throw GarageException(GarageException.Kind.Failed)
        return searchMakesResult
    }
    override suspend fun years(session: AuthSession, make: String, model: String): List<Int> {
        requestedYears = make to model
        check()
        if (failYears) throw GarageException(GarageException.Kind.Failed)
        return yearsResponse?.await() ?: yearsResult
    }
    override suspend fun addVehicle(session: AuthSession, vehicle: NewVehicle): GarageVehicle {
        check()
        if (failAddVehicle) throw GarageException(GarageException.Kind.Failed)
        submittedVehicle = vehicle
        val saved = addVehicleResponse?.await() ?: GarageVehicle(
            "n", "Ford Focus ${vehicle.year}", "passenger", VerificationStatus.None,
            vehicleData = vehicle.vehicleData,
        )
        vehicles = vehicles + saved
        return saved
    }
    override suspend fun equipmentTrims(
        session: AuthSession, make: String, model: String, year: Int,
    ): List<VehicleEquipmentTrim> {
        requestedEquipment = Triple(make, model, year)
        if (failEquipment) throw GarageException(GarageException.Kind.Failed)
        return equipmentResponse?.await() ?: equipmentTrimsResult
    }

    override suspend fun updateVehicle(session: AuthSession, vehicleId: String, vehicle: NewVehicle): GarageVehicle {
        check()
        if (failAddVehicle) throw GarageException(GarageException.Kind.Failed)
        submittedVehicle = vehicle
        updatedVehicleId = vehicleId
        val saved = addVehicleResponse?.await() ?: vehicles.first { it.id == vehicleId }.copy(
            name = "${vehicle.vehicleData["make"]?.let { (it as kotlinx.serialization.json.JsonObject)["name"] } } ${vehicle.year}",
            vehicleData = vehicle.vehicleData,
        )
        vehicles = vehicles.map { if (it.id == vehicleId) saved else it }
        return saved
    }

    override suspend fun deleteVehicle(session: AuthSession, vehicleId: String): String? {
        check()
        deletedVehicleId = vehicleId
        val response = deleteResponse
        val confirmed = if (response != null) response.await() else {
            if (active == vehicleId) vehicles.firstOrNull { it.id != vehicleId }?.id else active
        }
        vehicles = vehicles.filterNot { it.id == vehicleId }
        active = confirmed
        return confirmed
    }

    override suspend fun equipmentBodies(session: AuthSession, trimId: String): List<kotlinx.serialization.json.JsonObject> {
        requestedBodyTrim = trimId
        if (failBodies) throw GarageException(GarageException.Kind.Failed)
        return bodiesResponse?.await() ?: bodiesResult
    }

    override suspend fun equipmentEngines(session: AuthSession, trimId: String): List<kotlinx.serialization.json.JsonObject> {
        requestedEngineTrim = trimId
        if (failEngines) throw GarageException(GarageException.Kind.Failed)
        return enginesResponse?.await() ?: enginesResult
    }

    override suspend fun equipmentMileages(session: AuthSession, trimId: String): List<kotlinx.serialization.json.JsonObject> {
        requestedMileageTrim = trimId
        if (failMileages) throw GarageException(GarageException.Kind.Failed)
        return mileagesResponse?.await() ?: mileagesResult
    }

    override suspend fun equipmentDetails(session: AuthSession, trimId: String): VehicleEquipmentDetails {
        requestedEquipmentTrim = trimId
        if (failEquipment) throw GarageException(GarageException.Kind.Failed)
        return equipmentDetailsResponse?.await() ?: equipmentDetailsResult
    }
}

internal fun testSession() = AuthSession("t", "r", "Bearer", 60u)
