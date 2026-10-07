package com.mobilespace.xrnavi.presentation.navigation

import com.mobilespace.xrnavi.domain.DrivingContext

enum class AppDestination {
    SignIn, Registration, OrganizationInvitation, Route, AddressSearch, NearbyStops, WorkTasks,
    WorkTripDetails, TripPlan, ParkingStop, TripSummary, Garage, FordConfiguration, AddVehicle,
    Preferences, DeviceDiagnostics, OfflineMaps, OrganizationSettings, Profile, Privacy,
    DeleteAccount, TripHistory, VehicleProfile, VehicleConfigurations, RouteSettings,
    ActiveNavigation, XrNavigation,
}
data class NavigationEntry(val id: Long, val destination: AppDestination, val taskId: String? = null)
data class NavigationState(val entries: List<NavigationEntry>) {
    val current get() = entries.last()
    val canGoBack get() = entries.size > 1
}

/** Pure stack navigation: each detail remembers its caller by its position, not a shared return variable. */
class AppNavigator {
    private var nextId = 1L
    var state = NavigationState(listOf(NavigationEntry(0L, AppDestination.SignIn)))
        private set

    fun open(destination: AppDestination, context: DrivingContext, member: Boolean, taskId: String? = null): Boolean {
        if (destination in workDestinations && (context != DrivingContext.Work || !member)) return false
        if (destination == AppDestination.OrganizationSettings && !member) {
            return open(AppDestination.OrganizationInvitation, context, member)
        }
        if (state.current.destination == destination && state.current.taskId == taskId) return true
        state = state.copy(entries = state.entries + NavigationEntry(nextId++, destination, taskId))
        return true
    }
    fun back(): Boolean {
        if (!state.canGoBack) return false
        state = state.copy(entries = state.entries.dropLast(1))
        return true
    }
    fun authenticated() {
        state = NavigationState(listOf(NavigationEntry(nextId++, AppDestination.Route)))
    }
    fun selectTab(destination: AppDestination) {
        require(destination in tabs)
        if (state.current.destination == destination) return
        val route = state.entries.firstOrNull { it.destination == AppDestination.Route }
            ?: NavigationEntry(nextId++, AppDestination.Route)
        val tab = state.entries.lastOrNull { it.destination == destination }
            ?: NavigationEntry(nextId++, destination)
        state = NavigationState(if (destination == AppDestination.Route) listOf(route) else listOf(route, tab))
    }
    fun finishPreview() {
        val activeIndex = state.entries.indexOfLast { it.destination == AppDestination.ActiveNavigation }
        val base = if (activeIndex >= 0) state.entries.take(activeIndex) else state.entries
        state = NavigationState(base + NavigationEntry(nextId++, AppDestination.TripSummary))
    }
    private companion object {
        val tabs = setOf(AppDestination.Route, AppDestination.Garage, AppDestination.Preferences, AppDestination.Profile)
        val workDestinations = setOf(AppDestination.WorkTasks, AppDestination.WorkTripDetails)
    }
}
