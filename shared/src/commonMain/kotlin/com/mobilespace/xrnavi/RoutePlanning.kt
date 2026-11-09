package com.mobilespace.xrnavi

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

data class GeoPoint(val latitude: Double, val longitude: Double)

data class RoutePlace(val name: String, val address: String, val point: GeoPoint)

data class PlannedRoute(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val roads: String,
)

class RoutePlanningException(val kind: Kind) : Exception(kind.name) {
    enum class Kind { Unavailable, Network, NoRoute, Rejected }
}

internal interface RoutePlanningService {
    suspend fun search(query: String, near: GeoPoint?): List<RoutePlace>
    suspend fun route(from: GeoPoint, to: GeoPoint): PlannedRoute
}

/** Mapbox Search/Directions over HTTPS. Errors never carry the request URL, so the token cannot leak. */
internal class MapboxRoutePlanningService(
    private val token: () -> String,
    private val client: HttpClient = HttpClient(),
    private val host: String = "https://api.mapbox.com",
) : RoutePlanningService {
    override suspend fun search(query: String, near: GeoPoint?): List<RoutePlace> {
        val body = fetch("$host/search/geocode/v6/forward") {
            parameter("q", query)
            parameter("language", "uk")
            parameter("limit", 6)
            if (near != null) parameter("proximity", "${near.longitude},${near.latitude}")
        }
        return parseGeocoding(body)
    }

    override suspend fun route(from: GeoPoint, to: GeoPoint): PlannedRoute {
        val coordinates = "${from.longitude},${from.latitude};${to.longitude},${to.latitude}"
        val body = fetch("$host/directions/v5/mapbox/driving/$coordinates") {
            parameter("geometries", "geojson")
            parameter("overview", "full")
            parameter("language", "uk")
        }
        return parseDirections(body)
    }

    private suspend fun fetch(
        url: String,
        configure: io.ktor.client.request.HttpRequestBuilder.() -> Unit,
    ): String {
        val accessToken = token()
        if (accessToken.isBlank()) throw RoutePlanningException(RoutePlanningException.Kind.Unavailable)
        try {
            val response = client.get(url) {
                configure()
                parameter("access_token", accessToken)
            }
            if (!response.status.isSuccess()) {
                throw RoutePlanningException(
                    if (response.status.value in 400..499) RoutePlanningException.Kind.Rejected
                    else RoutePlanningException.Kind.Network,
                )
            }
            return response.bodyAsText()
        } catch (error: CancellationException) {
            throw error
        } catch (error: RoutePlanningException) {
            throw error
        } catch (error: Throwable) {
            throw RoutePlanningException(RoutePlanningException.Kind.Network)
        }
    }
}

private val routeJson = Json { ignoreUnknownKeys = true }

internal fun parseGeocoding(body: String): List<RoutePlace> {
    val features = parseObject(body)["features"] as? JsonArray ?: return emptyList()
    return features.mapNotNull { feature ->
        val properties = (feature as? JsonObject)?.get("properties") as? JsonObject ?: return@mapNotNull null
        val coordinates = properties["coordinates"] as? JsonObject
        val geometry = (feature["geometry"] as? JsonObject)?.get("coordinates") as? JsonArray
        val latitude = coordinates?.get("latitude").number() ?: geometry?.getOrNull(1).number()
        val longitude = coordinates?.get("longitude").number() ?: geometry?.getOrNull(0).number()
        val name = properties["name"].text()
        if (latitude == null || longitude == null || name.isBlank()) return@mapNotNull null
        val address = properties["full_address"].text().ifBlank { properties["place_formatted"].text() }
        RoutePlace(name, address.ifBlank { name }, GeoPoint(latitude, longitude))
    }
}

internal fun parseDirections(body: String): PlannedRoute {
    val route = (parseObject(body)["routes"] as? JsonArray)?.firstOrNull() as? JsonObject
        ?: throw RoutePlanningException(RoutePlanningException.Kind.NoRoute)
    val points = (((route["geometry"] as? JsonObject)?.get("coordinates")) as? JsonArray)
        ?.mapNotNull { pair ->
            val longitude = (pair as? JsonArray)?.getOrNull(0).number()
            val latitude = pair.let { it as? JsonArray }?.getOrNull(1).number()
            if (latitude == null || longitude == null) null else GeoPoint(latitude, longitude)
        }.orEmpty()
    val distance = route["distance"].number()
    val duration = route["duration"].number()
    if (points.size < 2 || distance == null || duration == null) {
        throw RoutePlanningException(RoutePlanningException.Kind.NoRoute)
    }
    val roads = ((route["legs"] as? JsonArray)?.firstOrNull() as? JsonObject)?.get("summary").text()
    return PlannedRoute(points, distance, duration, roads)
}

private fun parseObject(body: String): JsonObject = try {
    routeJson.parseToJsonElement(body).jsonObject
} catch (error: Exception) {
    throw RoutePlanningException(RoutePlanningException.Kind.Network)
}

private fun JsonElement?.number(): Double? = (this as? JsonPrimitive)?.doubleOrNull
private fun JsonElement?.text(): String = (this as? JsonPrimitive)?.contentOrNull.orEmpty()

internal fun formatDuration(seconds: Double): String {
    val minutes = ((seconds + 30) / 60).toLong().coerceAtLeast(1)
    val days = minutes / (24 * 60)
    val hours = minutes % (24 * 60) / 60
    val rest = minutes % 60
    return buildList {
        if (days > 0) add("$days д")
        if (hours > 0) add("$hours год")
        if (rest > 0 || isEmpty()) add("$rest хв")
    }.joinToString(" ")
}

internal fun formatDistance(meters: Double): String {
    if (meters < 1000) return "${meters.toInt()} м"
    val tenths = (meters / 100).toLong()
    val whole = tenths / 10
    return if (whole >= 100 || tenths % 10 == 0L) "$whole км" else "$whole,${tenths % 10} км"
}

internal fun formatClock(minutesOfDay: Int): String {
    val hour = minutesOfDay / 60 % 24
    val minute = minutesOfDay % 60
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}
