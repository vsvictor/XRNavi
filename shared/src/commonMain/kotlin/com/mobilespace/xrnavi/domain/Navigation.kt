package com.mobilespace.xrnavi.domain

data class DemoAddress(val id: String, val title: String, val city: String, val recent: Boolean) {
    val destination: String get() = "$title, $city"
}

enum class StopCategory { All, Rest, Fuel, Charging, Service, Weighing, Border }

data class DemoStop(
    val id: String,
    val category: StopCategory,
    val distanceFromRouteKm: Double,
    val truckCompatible: Boolean,
)

interface NavigationDemoRepository {
    val addresses: List<DemoAddress>
    val stops: List<DemoStop>
}

class SearchDemoAddresses(private val repository: NavigationDemoRepository) {
    operator fun invoke(query: String): List<DemoAddress> {
        if (query.isBlank()) return emptyList()
        val terms = query.trim().lowercase().split(Regex("\\s+"))
        return repository.addresses.filter { address ->
            terms.all { address.destination.lowercase().contains(it) }
        }
    }
    fun recent(): List<DemoAddress> = repository.addresses.filter { it.recent }
}

class FilterDemoStops(private val repository: NavigationDemoRepository) {
    operator fun invoke(category: StopCategory, trucksOnly: Boolean, withinFiveKm: Boolean): List<DemoStop> =
        repository.stops.filter {
            (category == StopCategory.All || it.category == category) &&
                (!trucksOnly || it.truckCompatible) &&
                (!withinFiveKm || it.distanceFromRouteKm <= 5.0)
        }
}

class SelectRouteVehicle(private val session: SessionRepository) {
    operator fun invoke(category: Int): Boolean {
        require(category in 0..2)
        if (!SelectGarageVehicle(session)(if (category == 1) VehicleId.Man else VehicleId.Mustang)) return false
        session.update {
            it.copy(
                routingSettings = it.routingSettings.copy(vehicleCategory = category),
            )
        }
        return true
    }
}

class SetRouteDestination(private val session: SessionRepository) {
    operator fun invoke(destination: String) {
        session.update { it.copy(destination = destination, selectedTask = null) }
    }
}

class ApplyRoutingSettings(private val session: SessionRepository) {
    operator fun invoke(settings: RoutingSettings): Boolean {
        require(settings.vehicleCategory in 0..2 && settings.trailerIndex in 0..2 && settings.selectedRoute in 0..2)
        if (!SelectRouteVehicle(session)(settings.vehicleCategory)) return false
        session.update {
            it.copy(
                routingSettings = settings,
            )
        }
        return true
    }
}

enum class RoutePreviewResult { DemoPreview, MissingDestination, TruckValidationUnavailable }

class CheckRoutePreview {
    operator fun invoke(destination: String, category: Int): RoutePreviewResult = when {
        destination.isBlank() -> RoutePreviewResult.MissingDestination
        category == 1 -> RoutePreviewResult.TruckValidationUnavailable
        else -> RoutePreviewResult.DemoPreview
    }
}
