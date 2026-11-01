package com.mobilespace.xrnavi.domain.routestops

import com.mobilespace.xrnavi.RouteStopsSnapshot

internal interface RouteStopsRepository {
    fun load(): RouteStopsSnapshot?
    fun save(snapshot: RouteStopsSnapshot): Boolean
}
