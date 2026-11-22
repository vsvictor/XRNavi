package com.mobilespace.xrnavi

import android.animation.ValueAnimator
import com.mapbox.common.location.LocationError
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import kotlin.test.*

class TrackerMapboxLocationProviderTest {
    private class Consumer : LocationConsumer {
        val points = mutableListOf<Point>()
        val bearings = mutableListOf<Double>()
        val accuracies = mutableListOf<Double>()

        override fun onLocationUpdated(vararg location: Point, options: (ValueAnimator.() -> Unit)?) {
            points.addAll(location)
        }
        override fun onBearingUpdated(vararg bearing: Double, options: (ValueAnimator.() -> Unit)?) {
            bearings.addAll(bearing.toList())
        }
        override fun onHorizontalAccuracyRadiusUpdated(vararg radius: Double, options: (ValueAnimator.() -> Unit)?) {
            accuracies.addAll(radius.toList())
        }
        override fun onPuckLocationAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) = Unit
        override fun onPuckBearingAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) = Unit
        override fun onPuckAccuracyRadiusAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) = Unit
        override fun onError(error: LocationError) = fail("Unexpected native location error: $error")
    }

    @Test fun onlyTrackerUpdatesReachConsumersAndLatestFixIsReplayed() {
        val provider = TrackerMapboxLocationProvider()
        val consumer = Consumer()
        provider.registerLocationConsumer(consumer)
        assertTrue(consumer.points.isEmpty())
        val fix = DeviceFix(GeoPoint(50.45, 30.52), 1_000, 4.5, 85.0, 12.0)
        provider.update(fix)
        provider.update(fix)
        assertEquals(listOf(Point.fromLngLat(30.52, 50.45)), consumer.points)
        assertEquals(listOf(85.0), consumer.bearings)
        assertEquals(listOf(4.5), consumer.accuracies)
        val second = Consumer()
        provider.registerLocationConsumer(second)
        assertEquals(consumer.points, second.points)
        provider.unRegisterLocationConsumer(consumer)
        provider.update(fix.copy(point = GeoPoint(51.0, 31.0)))
        assertEquals(1, consumer.points.size)
        assertEquals(2, second.points.size)
    }

    @Test fun invalidatedFixIsNotReplayedAndUnknownBearingIsNotInvented() {
        val provider = TrackerMapboxLocationProvider()
        provider.update(DeviceFix(GeoPoint(50.45, 30.52), 1_000))
        val consumer = Consumer()
        provider.registerLocationConsumer(consumer)
        assertEquals(1, consumer.points.size)
        assertTrue(consumer.bearings.isEmpty())
        assertTrue(consumer.accuracies.isEmpty())
        provider.update(null)
        val recreated = Consumer()
        provider.registerLocationConsumer(recreated)
        assertTrue(recreated.points.isEmpty())
    }
}
