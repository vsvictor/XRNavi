package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class VehiclePhotoSelections(private val storage: SecureSessionStorage) {
    private var invalidStorage = false
    private val mutableSelections = MutableStateFlow(readSelections())
    val selections = mutableSelections.asStateFlow()

    private fun readSelections(): Map<String, Map<String, VehiclePhoto>> {
        val saved = storage.read() ?: return emptyMap()
        return try {
            val accounts = Json.parseToJsonElement(saved) as? JsonObject
                ?: throw SerializationException("Invalid accounts")
            accounts.mapValues { (_, account) ->
                val vehicles =
                    account as? JsonObject ?: throw SerializationException("Invalid vehicles")
                vehicles.mapValues { (_, value) ->
                    val fields =
                        value as? JsonObject ?: throw SerializationException("Invalid photo")

                    fun field(name: String): String =
                        (fields[name] as? JsonPrimitive)?.takeIf { it.isString }?.content
                            ?: throw SerializationException("Missing photo field $name")
                    VehiclePhoto(
                        field("url"), field("sourceUrl"), field("author"), field("license"),
                        field("licenseUrl"), field("title"),
                    )
                }
            }
        } catch (_: SerializationException) {
            invalidStorage = true
            emptyMap()
        }
    }

    fun select(accountId: String, vehicleId: String, photo: VehiclePhoto?): Boolean {
        if (invalidStorage) return false
        val accounts = mutableSelections.value
        val vehicles = accounts[accountId].orEmpty().toMutableMap()
        if (photo == null) vehicles.remove(vehicleId) else vehicles[vehicleId] = photo
        val updated = accounts.toMutableMap()
        if (vehicles.isEmpty()) updated.remove(accountId) else updated[accountId] = vehicles
        val encoded = buildJsonObject {
            updated.forEach { (account, accountVehicles) ->
                put(account, buildJsonObject {
                    accountVehicles.forEach { (id, selected) ->
                        put(id, buildJsonObject {
                            put("url", selected.url)
                            put("sourceUrl", selected.sourceUrl)
                            put("author", selected.author)
                            put("license", selected.license)
                            put("licenseUrl", selected.licenseUrl)
                            put("title", selected.title)
                        })
                    }
                })
            }
        }
        if (!storage.write(encoded.toString())) return false
        mutableSelections.value = updated
        return true
    }
}
