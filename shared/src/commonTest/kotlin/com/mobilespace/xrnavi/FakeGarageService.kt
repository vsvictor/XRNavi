package com.mobilespace.xrnavi

internal class FakeGarageService : GarageService {
    var fail = false
    var vehicles = listOf(
        GarageVehicle("a", "Ford Focus", "passenger", VerificationStatus.Verified),
        GarageVehicle("t", "Volvo FH", "truck", VerificationStatus.Pending),
    )
    var active: String? = "a"
    private fun check() { if (fail) throw GarageException(GarageException.Kind.Failed) }
    override suspend fun vehicles(session: AuthSession): GarageSnapshot { check(); return GarageSnapshot(vehicles, active) }
    override suspend fun setActive(session: AuthSession, vehicleId: String): String { check(); active = vehicleId; return vehicleId }
    override suspend fun classes(session: AuthSession): List<VehicleClassItem> { check(); return listOf(VehicleClassItem("c", "Легкові")) }
    override suspend fun makes(session: AuthSession, classId: String): List<VehicleMake> { check(); return listOf(VehicleMake("m", "Ford")) }
    override suspend fun models(session: AuthSession, makeId: String): List<VehicleModelItem> {
        check()
        return listOf(VehicleModelItem("mo", "Focus", listOf(2020, 2021), listOf(VehicleTrim("tr", "Titanium"))))
    }
    override suspend fun addVehicle(session: AuthSession, vehicle: NewVehicle): GarageVehicle {
        check()
        return GarageVehicle("n", "Ford Focus ${vehicle.year}", "passenger", VerificationStatus.None)
    }
}

internal fun testSession() = AuthSession("t", "r", "Bearer", 60u)
