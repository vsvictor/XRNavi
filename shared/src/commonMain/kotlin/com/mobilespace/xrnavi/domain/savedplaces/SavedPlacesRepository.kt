package com.mobilespace.xrnavi

internal interface SavedPlacesRepository {
    fun load(): SavedPlacesDecodeResult
    fun save(places: List<SavedPlace>): Boolean
}
