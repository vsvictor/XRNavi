package com.mobilespace.xrnavi.domain.routepreferences

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
    val notice: RoutePreferencesNotice
)

internal enum class RoutePreferencesNotice {
    NoActiveVehicle,
    WrongVehicle,
    ConfigurationUnavailable,
    SavedChanged,
    SavedUnchanged
}

internal fun applyRoutePreferences(
    activeVehicleId: String?,
    preferencesByVehicle: Map<String, RoutePreferences>,
    draft: RoutePreferencesDraft,
    activeConfigurationAvailable: Boolean
): RoutePreferencesApplyResult {
    val failure = when {
        activeVehicleId.isNullOrBlank() -> RoutePreferencesNotice.NoActiveVehicle
        draft.vehicleId != activeVehicleId -> RoutePreferencesNotice.WrongVehicle
        !activeConfigurationAvailable -> RoutePreferencesNotice.ConfigurationUnavailable
        else -> null
    }
    if (failure != null) {
        return RoutePreferencesApplyResult(
            preferencesByVehicle = preferencesByVehicle,
            applied = false,
            changed = false,
            notice = failure
        )
    }

    val vehicleId = requireNotNull(activeVehicleId)
    val current = preferencesByVehicle[vehicleId] ?: RoutePreferences()
    val changed = current != draft.selected
    val updated = if (changed) {
        preferencesByVehicle + (vehicleId to draft.selected)
    } else {
        preferencesByVehicle
    }
    return RoutePreferencesApplyResult(
        preferencesByVehicle = updated,
        applied = true,
        changed = changed,
        notice = if (changed) RoutePreferencesNotice.SavedChanged
        else RoutePreferencesNotice.SavedUnchanged
    )
}
