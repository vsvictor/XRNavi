package com.mobilespace.xrnavi.data.routepreferences

import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferences

internal fun encodeRoutePreferences(
    preferencesByVehicle: Map<String, RoutePreferences>
): List<String> = preferencesByVehicle.flatMap { (vehicleId, preferences) ->
    listOf(
        vehicleId,
        preferences.avoidTolls.toString(),
        preferences.avoidUnpavedRoads.toString(),
        preferences.avoidFerries.toString()
    )
}

internal fun decodeRoutePreferences(values: List<String>): Map<String, RoutePreferences> =
    values.chunked(4)
        .filter { it.size == 4 }
        .associate { (vehicleId, tolls, unpaved, ferries) ->
            vehicleId to RoutePreferences(
                avoidTolls = tolls.toBooleanStrictOrNull() ?: false,
                avoidUnpavedRoads = unpaved.toBooleanStrictOrNull() ?: false,
                avoidFerries = ferries.toBooleanStrictOrNull() ?: false
            )
        }
