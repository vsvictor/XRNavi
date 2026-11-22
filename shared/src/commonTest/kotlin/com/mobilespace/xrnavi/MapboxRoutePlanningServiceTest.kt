package com.mobilespace.xrnavi

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class MapboxRoutePlanningServiceTest {
    private val directions = """{"routes":[{"distance":540123.4,"duration":22800.0,
        "geometry":{"coordinates":[[30.5,50.4],[24.0,49.8]]},"legs":[{"summary":"М-06, Е40"}]}]}"""
    private val geocoding = """{"features":[{"geometry":{"coordinates":[24.03,49.84]},
        "properties":{"name":"Львів","full_address":"Львів, Україна",
        "coordinates":{"latitude":49.84,"longitude":24.03}}},{"properties":{}}]}"""

    @Test
    fun parsesDirections() {
        val route = parseDirections(directions)
        assertEquals(2, route.points.size)
        assertEquals("М-06, Е40", route.roads)
        assertEquals("6 год 20 хв", formatDuration(route.durationSeconds))
        assertEquals("540 км", formatDistance(route.distanceMeters))
    }

    @Test
    fun emptyDirectionsMeanNoRoute() {
        val error = assertFailsWith<RoutePlanningException> { parseDirections("""{"routes":[]}""") }
        assertEquals(RoutePlanningException.Kind.NoRoute, error.kind)
    }

    @Test
    fun parsesGeocodingAndSkipsIncompleteFeatures() {
        val places = parseGeocoding(geocoding)
        assertEquals(1, places.size)
        assertEquals(GeoPoint(49.84, 24.03), places.single().point)
    }

    @Test
    fun serviceSendsTokenButErrorsNeverContainIt() = runTest {
        var url = ""
        val ok = MockEngine { request -> url = request.url.toString(); respond(geocoding, HttpStatusCode.OK) }
        val service = MapboxRoutePlanningService({ "secret-token" }, HttpClient(ok))
        service.search("Львів", GeoPoint(50.0, 30.0))
        assertEquals(true, url.contains("access_token=secret-token"))

        val failing = MockEngine { respond("", HttpStatusCode.Unauthorized) }
        val error = assertFailsWith<RoutePlanningException> {
            MapboxRoutePlanningService({ "secret-token" }, HttpClient(failing)).route(GeoPoint(1.0, 1.0), GeoPoint(2.0, 2.0))
        }
        assertEquals(RoutePlanningException.Kind.Rejected, error.kind)
        assertFalse(error.toString().contains("secret-token"))
    }

    @Test
    fun missingTokenIsReportedAsUnavailable() = runTest {
        val service = MapboxRoutePlanningService({ "" }, HttpClient(MockEngine { respond("", HttpStatusCode.OK) }))
        val error = assertFailsWith<RoutePlanningException> { service.search("Львів", null) }
        assertEquals(RoutePlanningException.Kind.Unavailable, error.kind)
    }
}
