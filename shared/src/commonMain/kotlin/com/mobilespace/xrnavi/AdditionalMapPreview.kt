package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

/** Shared map-with-bottom-sheet host for the S35/S48/S49/S50/S54/S55 state previews. */
@Composable
internal fun AdditionalMapPreview(
    actions: AdditionalActions,
    origin: String = "",
    destination: String = "",
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    val open = actions.open
    val unavailable = actions.unavailable
    MapScreen(
        preview = false,
        demoMode = true,
        origin = origin,
        destination = destination,
        routeStops = emptyList(),
        sheetLevel = SheetLevel.Expanded,
        geoStatus = GeoStatus.NotConfigured,
        notice = "",
        vehicleClass = "Легкове",
        selectedVehicle = demoVehicle,
        vehicleMode = "Особистий · приклад стану",
        onVehicleClassChange = { unavailable("Зміна транспорту у прикладі") },
        onOpenSearch = { open(AdditionalScreen.SearchEmpty) },
        onOpenRouteStops = { unavailable("Зупинки у прикладі") },
        onOpenStopPlaces = { unavailable("Місця зупинки у прикладі") },
        onUseLocation = { open(AdditionalScreen.LocationEducation) },
        onSheetLevelChange = {},
        onPreview = { open(AdditionalScreen.RouteBuilding) },
        onStartRoute = { unavailable("Навігація") },
        onOpenRouteSettings = { unavailable("Параметри у прикладі") },
        onBack = actions.back,
        onSectionChange = actions.section,
        onToggleTheme = { unavailable("Зміна теми у прикладі") },
        onOpenAdditional = open,
        onMapLayers = { unavailable("Шари мапи") },
        sheetContent = {
            AdditionalHeading("Локальний приклад стану · запит не виконується")
            sheetContent()
        }
    )
}
