package com.mobilespace.xrnavi.domain.routestops

import com.mobilespace.xrnavi.RouteStopsSnapshot

internal class LoadRouteStops(
    private val repository: RouteStopsRepository
) {
    operator fun invoke(): RouteStopsSnapshot? = repository.load()
}

internal class PersistRouteStops(
    private val repository: RouteStopsRepository
) {
    operator fun invoke(snapshot: RouteStopsSnapshot): Boolean = repository.save(snapshot)
}
