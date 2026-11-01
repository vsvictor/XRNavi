package com.mobilespace.xrnavi.presentation.routepreferences

import androidx.compose.runtime.saveable.listSaver
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferences
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferencesDraft

internal val routePreferencesSaver = listSaver<Map<String, RoutePreferences>, String>(
    save = { preferencesByVehicle ->
        preferencesByVehicle.flatMap { (vehicleId, preferences) ->
            listOf(
                vehicleId,
                preferences.avoidTolls.toString(),
                preferences.avoidUnpavedRoads.toString(),
                preferences.avoidFerries.toString()
            )
        }
    },
    restore = { values ->
        values.chunked(4)
            .filter { it.size == 4 }
            .associate { (vehicleId, tolls, unpaved, ferries) ->
                vehicleId to RoutePreferences(
                    avoidTolls = tolls.toBooleanStrictOrNull() ?: false,
                    avoidUnpavedRoads = unpaved.toBooleanStrictOrNull() ?: false,
                    avoidFerries = ferries.toBooleanStrictOrNull() ?: false
                )
            }
    }
)

internal val routePreferencesDraftSaver = listSaver<RoutePreferencesDraft?, String>(
    save = { draft ->
        if (draft == null) emptyList() else listOf(
            draft.vehicleId,
            draft.applied.avoidTolls.toString(),
            draft.applied.avoidUnpavedRoads.toString(),
            draft.applied.avoidFerries.toString(),
            draft.selected.avoidTolls.toString(),
            draft.selected.avoidUnpavedRoads.toString(),
            draft.selected.avoidFerries.toString()
        )
    },
    restore = { values ->
        if (values.size != 7) null else RoutePreferencesDraft(
            vehicleId = values[0],
            applied = RoutePreferences(
                values[1].toBooleanStrictOrNull() ?: false,
                values[2].toBooleanStrictOrNull() ?: false,
                values[3].toBooleanStrictOrNull() ?: false
            ),
            selected = RoutePreferences(
                values[4].toBooleanStrictOrNull() ?: false,
                values[5].toBooleanStrictOrNull() ?: false,
                values[6].toBooleanStrictOrNull() ?: false
            )
        )
    }
)
