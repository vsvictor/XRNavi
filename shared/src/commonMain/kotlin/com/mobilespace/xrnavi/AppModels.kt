package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal enum class AppScreen {
    SignIn,
    Registration,
    Recovery,
    Map,
    Search,
    Preview,
    RouteSettings,
    RouteStops,
    RouteStopSearch,
    StopPlaces,
    SavedPlaces
}

internal fun initialScreenForSession(hasConfiguredSession: Boolean): AppScreen =
    if (hasConfiguredSession) AppScreen.Map else AppScreen.SignIn

internal enum class SearchTarget {
    Origin,
    Destination
}

internal fun searchQueryAfterTargetChange(
    currentTarget: SearchTarget,
    nextTarget: SearchTarget,
    query: String
): String = if (currentTarget == nextTarget) query else ""

internal data class AddressSearchState(
    val origin: String = "",
    val destination: String = "",
    val target: SearchTarget = SearchTarget.Destination,
    val query: String = ""
) {
    fun open(target: SearchTarget) = copy(target = target, query = "")

    fun activate(target: SearchTarget) = copy(
        target = target,
        query = searchQueryAfterTargetChange(this.target, target, query)
    )

    fun select(place: DemoPlace) = when (target) {
        SearchTarget.Origin -> copy(origin = place.title, query = "")
        SearchTarget.Destination -> copy(destination = place.title, query = "")
    }

    fun selectSavedPlace(place: SavedPlace) = select(DemoPlace(place.address, place.detail))

    fun cancel() = copy(query = "")

    companion object {
        val Saver = listSaver<AddressSearchState, String>(
            save = { listOf(it.origin, it.destination, it.target.name, it.query) },
            restore = { AddressSearchState(it[0], it[1], SearchTarget.valueOf(it[2]), it[3]) }
        )
    }

}

internal data class SavedPlaceAddressSelection(
    val addresses: AddressSearchState,
    val screen: AppScreen,
    val changed: Boolean
)

internal fun selectSavedPlaceAddress(
    addresses: AddressSearchState,
    place: SavedPlace,
    currentScreen: AppScreen
): SavedPlaceAddressSelection {
    val previousAddress = when (addresses.target) {
        SearchTarget.Origin -> addresses.origin
        SearchTarget.Destination -> addresses.destination
    }
    val updated = addresses.selectSavedPlace(place)
    val changed = previousAddress != place.address
    return SavedPlaceAddressSelection(
        addresses = updated,
        screen = if (changed) AppScreen.Map else currentScreen,
        changed = changed
    )
}

internal data class DemoVehicleProfile(
    val id: String,
    val name: String,
    val category: String,
    val specification: String,
    val artwork: DemoVehicleArtwork,
    val configurationName: String? = null
)

internal enum class DemoVehicleArtwork {
    Mustang,
    Passenger,
    Cargo,
    Special
}

internal val demoVehicle = DemoVehicleProfile(
    id = "ford-mustang-gt",
    name = "Ford Mustang GT",
    category = "Легкове",
    specification = "Легковий · 1,4 м · 1,8 т",
    artwork = DemoVehicleArtwork.Mustang
)

internal val demoVehicles = listOf(
    demoVehicle,
    DemoVehicleProfile(
        id = "demo-passenger-02",
        name = "Легковий автомобіль · демо 02",
        category = "Легкове",
        specification = "Легкове · характеристики не задані",
        artwork = DemoVehicleArtwork.Passenger
    ),
    DemoVehicleProfile(
        id = "demo-cargo-01",
        name = "Вантажний транспорт · демо 01",
        category = "Вантажне",
        specification = "Вантажне · характеристики не задані",
        artwork = DemoVehicleArtwork.Cargo
    ),
    DemoVehicleProfile(
        id = "demo-special-01",
        name = "Спецтехніка · демо 01",
        category = "Спецтехніка",
        specification = "Спецтехніка · характеристики не задані",
        artwork = DemoVehicleArtwork.Special
    )
)

internal fun demoVehicleById(vehicleId: String): DemoVehicleProfile? =
    demoVehicles.firstOrNull { it.id == vehicleId }

internal fun demoVehicleForClass(vehicleClass: String): DemoVehicleProfile? =
    demoVehicles.firstOrNull { it.category == vehicleClass }

internal fun routePreviewBlockMessage(
    origin: String,
    destination: String,
    vehicleClass: String
): String? = when {
    origin.isBlank() || destination.isBlank() ->
        "Вкажіть початкову та кінцеву точки, щоб переглянути демо-маршрут."

    demoVehicleForClass(vehicleClass) == null ->
        "Демо-профіль для транспорту «$vehicleClass» не налаштовано. Попередній перегляд недоступний."

    else -> null
}

internal fun vehicleClassUnavailableMessage(vehicleClass: String): String? =
    if (demoVehicleForClass(vehicleClass) != null) {
        null
    } else {
        "Демо-профіль для транспорту «$vehicleClass» не налаштовано. Виберіть категорію з наявними локальними демопрофілями."
    }

internal enum class GeoStatus {
    NotConfigured,
    PermissionDenied,
    ServiceDisabled,
    Acquiring,
    FreshFix,
    StaleFix
}

internal enum class SearchStatus {
    Idle,
    Loading,
    Results,
    Empty,
    Error
}

internal data class DemoPlace(
    val title: String,
    val detail: String
)

internal val demoPlaces = listOf(
    DemoPlace("площа Ринок, 1", "Львів · Львівська область · Ратуша"),
    DemoPlace("площа Ринок, 10", "Львів · Львівська область"),
    DemoPlace("площа Ринок, 1", "Дрогобич · Львівська область"),
    DemoPlace("вулиця Хрещатик, 22", "Київ · Київська міська рада")
)
