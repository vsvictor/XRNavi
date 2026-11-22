package com.mobilespace.xrnavi

internal class FakeRoutePlanningService : RoutePlanningService {
    var places = listOf(RoutePlace("Львів", "Львів, Україна", GeoPoint(49.84, 24.03)))
    var failure: RoutePlanningException? = null
    var routeCalls = 0

    override suspend fun search(query: String, near: GeoPoint?): List<RoutePlace> {
        failure?.let { throw it }
        return places
    }

    override suspend fun route(from: GeoPoint, to: GeoPoint): PlannedRoute {
        routeCalls++
        failure?.let { throw it }
        return PlannedRoute(listOf(from, to), 540_000.0, 22_800.0, "М-06")
    }
}
