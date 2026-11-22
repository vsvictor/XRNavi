package com.mobilespace.xrnavi

internal enum class VerificationStatus { Verified, Pending, Rejected, None }

internal data class VehicleClassItem(val id: String, val name: String)

internal data class VehicleMake(val id: String, val name: String)

internal data class VehicleTrim(val id: String, val name: String)

internal data class VehicleModelItem(
    val id: String,
    val name: String,
    val years: List<Int>,
    val trims: List<VehicleTrim>,
)

internal data class GarageVehicle(
    val id: String,
    val name: String,
    val category: String,
    val verification: VerificationStatus,
    val heightMeters: Double? = null,
    val widthMeters: Double? = null,
    val lengthMeters: Double? = null,
    val weightKg: Int? = null,
) {
    /** Heavy vehicles need a verified profile before a truck-safe route can be promised. */
    val needsVerification: Boolean
        get() = category.lowercase() in heavyCategories && verification != VerificationStatus.Verified

    private companion object {
        val heavyCategories = setOf("truck", "bus", "вантажний", "автобус", "вантажівка")
    }
}

internal data class GarageSnapshot(val vehicles: List<GarageVehicle>, val activeVehicleId: String?)

internal data class NewVehicle(
    val classId: String,
    val makeId: String,
    val modelId: String,
    val year: Int,
    val trimId: String,
)

internal class GarageException(val kind: Kind) : Exception(kind.name) {
    enum class Kind { Session, Failed, InvalidResponse }
}

internal interface GarageService {
    suspend fun vehicles(session: AuthSession): GarageSnapshot
    suspend fun setActive(session: AuthSession, vehicleId: String): String
    suspend fun classes(session: AuthSession): List<VehicleClassItem>
    suspend fun makes(session: AuthSession, classId: String): List<VehicleMake>
    suspend fun models(session: AuthSession, makeId: String): List<VehicleModelItem>
    suspend fun addVehicle(session: AuthSession, vehicle: NewVehicle): GarageVehicle
}

/** Used until a backend implementation is injected; every call reports a failure, never an empty garage. */
internal class UnavailableGarageService : GarageService {
    private fun fail(): Nothing = throw GarageException(GarageException.Kind.Failed)
    override suspend fun vehicles(session: AuthSession) = fail()
    override suspend fun setActive(session: AuthSession, vehicleId: String) = fail()
    override suspend fun classes(session: AuthSession) = fail()
    override suspend fun makes(session: AuthSession, classId: String) = fail()
    override suspend fun models(session: AuthSession, makeId: String) = fail()
    override suspend fun addVehicle(session: AuthSession, vehicle: NewVehicle) = fail()
}

internal fun VerificationStatus.label(): String = when (this) {
    VerificationStatus.Verified -> "Підтверджено"
    VerificationStatus.Pending -> "На перевірці"
    VerificationStatus.Rejected -> "Відхилено"
    VerificationStatus.None -> "Не підтверджено"
}
