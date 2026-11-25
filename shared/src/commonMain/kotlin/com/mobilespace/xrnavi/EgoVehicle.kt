package com.mobilespace.xrnavi

import com.exkmptracker.cooperative.VehicleClass

/**
 * Phone-holder height above the local road plane for this app.
 * Passenger and cargo profiles use different cab heights. The spread is the unsurveyed mount, not a lens sample.
 */
internal data class RoadPlane(val heightMeters: Double, val heightStdDevMeters: Double)

internal fun roadPlane(ego: VehicleClass): RoadPlane? = when (ego) {
    VehicleClass.CAR -> RoadPlane(heightMeters = 1.35, heightStdDevMeters = 0.30)
    VehicleClass.TRUCK -> RoadPlane(heightMeters = 2.40, heightStdDevMeters = 0.45)
    else -> null
}

/** Profile and garage labels that this app publishes as its own vehicle. Bus and trolleybus stay neighbor classes. */
internal fun egoVehicleClass(category: String): VehicleClass? = when (garageCategoryFilterKey(category)) {
    "passenger" -> VehicleClass.CAR
    "truck" -> VehicleClass.TRUCK
    else -> null
}

internal object EgoVehicle {
    private val lock = EgoLock()
    private var profile: VehicleClass? = null
    private var garageKnown = false
    private var garage: VehicleClass? = null

    fun noteProfile(category: String) = lock.withLock {
        profile = egoVehicleClass(category)
    }

    fun noteGarage(category: String?) = lock.withLock {
        garageKnown = true
        garage = category?.let(::egoVehicleClass)
    }

    /** Drops the garage overlay and returns the profile recorded so far. */
    fun clearGarage(): VehicleClass? = lock.withLock {
        garageKnown = false
        garage = null
        profile
    }

    fun current(): VehicleClass? = lock.withLock {
        if (garageKnown) garage else profile
    }

    /** The profile is process-wide. Tests reset it so an earlier note cannot leak in. */
    internal fun reset() = lock.withLock {
        profile = null
        garageKnown = false
        garage = null
    }
}

internal expect class EgoLock() {
    fun <T> withLock(action: () -> T): T
}

fun xrnaviCooperativeVehicleClass(): VehicleClass? = EgoVehicle.current()
