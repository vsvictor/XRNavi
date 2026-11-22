package com.mobilespace.xrnavi

import com.exkmptracker.model.TrackingIdentity
import com.exkmptracker.model.TrackingSession
import kotlinx.serialization.json.*

internal fun interface TrackerRegistrationService {
    suspend fun open(deviceId: String): TrackerRegistration
}

internal class RpcTrackerRegistrationService(
    private val refreshSession: suspend () -> Unit = { error("Session refresh is not configured") },
    private val call: suspend (String, String) -> String,
) : TrackerRegistrationService {
    override suspend fun open(deviceId: String): TrackerRegistration {
        val result = Json.parseToJsonElement(call("tracking.open",
            buildJsonObject { put("deviceId", deviceId) }.toString())) as? JsonObject
            ?: error("Invalid tracking.open result")
        fun text(key: String) = (result[key] as? JsonPrimitive)?.takeIf { it.isString }
            ?.content?.takeIf { it.isNotBlank() } ?: error("Missing tracking registration field")
        val trackerId = text("trackerId")
        require(trackerId == deviceId) { "Server returned a different installation ID" }
        val registration = TrackerRegistration(trackerId, TrackingSession(text("sessionId"),
            TrackingIdentity(text("driverId"), text("brandId"))))
        val refreshRequired = result["refreshRequired"]?.let {
            val value = it as? JsonPrimitive
            require(value != null && !value.isString && value.booleanOrNull != null) {
                "Invalid tracking registration refreshRequired"
            }
            value.boolean
        } ?: false
        if (refreshRequired) refreshSession()
        return registration
    }
}
