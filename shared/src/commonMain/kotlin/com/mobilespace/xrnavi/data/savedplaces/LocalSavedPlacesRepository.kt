package com.mobilespace.xrnavi

internal class LocalSavedPlacesRepository(
    private val store: SavedPlacesStore
) : SavedPlacesRepository {
    private var canWrite = false

    override fun load(): SavedPlacesDecodeResult =
        decodeSavedPlaces(store.read()).also { result ->
            canWrite = result is SavedPlacesDecodeResult.Success
        }

    override fun save(places: List<SavedPlace>): Boolean {
        if (!canWrite) return false
        return store.write(encodeSavedPlaces(places))
    }
}
