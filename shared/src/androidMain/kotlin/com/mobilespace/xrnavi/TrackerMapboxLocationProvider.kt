package com.mobilespace.xrnavi

import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.LocationProvider

internal class TrackerMapboxLocationProvider : LocationProvider {
    private val consumers = mutableSetOf<LocationConsumer>()
    private var latest: DeviceFix? = null

    override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
        consumers.add(locationConsumer)
        latest?.let { deliver(locationConsumer, it) }
    }

    override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
        consumers.remove(locationConsumer)
    }

    fun update(fix: DeviceFix?) {
        if (latest == fix) return
        latest = fix
        if (fix != null) consumers.toList().forEach { deliver(it, fix) }
    }

    private fun deliver(consumer: LocationConsumer, fix: DeviceFix) {
        consumer.onLocationUpdated(Point.fromLngLat(fix.point.longitude, fix.point.latitude))
        fix.bearingDegrees?.let { consumer.onBearingUpdated(it) }
        fix.accuracyMeters?.let { consumer.onHorizontalAccuracyRadiusUpdated(it) }
    }
}
