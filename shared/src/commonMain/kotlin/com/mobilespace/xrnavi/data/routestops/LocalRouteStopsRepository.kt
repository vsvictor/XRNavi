package com.mobilespace.xrnavi.data.routestops

import com.mobilespace.xrnavi.RouteStopsSnapshot
import com.mobilespace.xrnavi.RouteStopsStore
import com.mobilespace.xrnavi.decodeRouteStopsSnapshot
import com.mobilespace.xrnavi.encodeRouteStopsSnapshot
import com.mobilespace.xrnavi.domain.routestops.RouteStopsRepository

internal class LocalRouteStopsRepository(
    private val store: RouteStopsStore
) : RouteStopsRepository {
    private var canWrite = false

    override fun load(): RouteStopsSnapshot? =
        decodeRouteStopsSnapshot(store.read()).also { canWrite = it != null }

    override fun save(snapshot: RouteStopsSnapshot): Boolean {
        return canWrite && store.write(encodeRouteStopsSnapshot(snapshot))
    }
}
