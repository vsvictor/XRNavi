package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal data class RoutePreferences(
    val avoidTolls: Boolean = false,
    val avoidUnpavedRoads: Boolean = false,
    val avoidFerries: Boolean = false
) {
    val isDefault: Boolean
        get() = !avoidTolls && !avoidUnpavedRoads && !avoidFerries
}

internal data class RoutePreferencesDraft(
    val vehicleId: String,
    val applied: RoutePreferences,
    val selected: RoutePreferences
) {
    fun update(preferences: RoutePreferences): RoutePreferencesDraft =
        copy(selected = preferences)

    fun cancel(): RoutePreferencesDraft = copy(selected = applied)

    val hasPendingChanges: Boolean
        get() = selected != applied
}

internal data class RoutePreferencesApplyResult(
    val preferencesByVehicle: Map<String, RoutePreferences>,
    val applied: Boolean,
    val changed: Boolean,
    val message: String
)

internal fun routeSettingsReturnScreen(
    previousScreen: AppScreen,
    preferencesChanged: Boolean
): AppScreen = if (preferencesChanged) AppScreen.Map else previousScreen

internal fun applyRoutePreferences(
    activeVehicleId: String?,
    preferencesByVehicle: Map<String, RoutePreferences>,
    draft: RoutePreferencesDraft,
    activeConfigurationAvailable: Boolean
): RoutePreferencesApplyResult {
    val failure = when {
        activeVehicleId.isNullOrBlank() ->
            "Спочатку оберіть демоавтомобіль у гаражі."
        draft.vehicleId != activeVehicleId ->
            "Чернетка належить іншому автомобілю. Відкрийте параметри для активного профілю."
        !activeConfigurationAvailable ->
            "Застосована конфігурація недоступна. Спочатку виберіть доступну конфігурацію в S16."
        else -> null
    }
    if (failure != null) {
        return RoutePreferencesApplyResult(
            preferencesByVehicle = preferencesByVehicle,
            applied = false,
            changed = false,
            message = failure
        )
    }

    val current = preferencesByVehicle[requireNotNull(activeVehicleId)] ?: RoutePreferences()
    val changed = current != draft.selected
    val updated = if (changed) {
        preferencesByVehicle + (activeVehicleId to draft.selected)
    } else {
        preferencesByVehicle
    }
    return RoutePreferencesApplyResult(
        preferencesByVehicle = updated,
        applied = true,
        changed = changed,
        message = if (changed) {
            "Параметри збережено локально. Створіть попередній перегляд повторно."
        } else {
            "Параметри вже застосовані; значення не змінено."
        }
    )
}

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

internal val routePreferencesSaver = listSaver<Map<String, RoutePreferences>, String>(
    save = { encodeRoutePreferences(it) },
    restore = { decodeRoutePreferences(it) }
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
