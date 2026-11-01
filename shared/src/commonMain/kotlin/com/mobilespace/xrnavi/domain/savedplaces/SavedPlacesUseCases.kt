package com.mobilespace.xrnavi

internal class LoadSavedPlaces(private val repository: SavedPlacesRepository) {
    operator fun invoke(): SavedPlacesDecodeResult = repository.load()
}

internal class PersistSavedPlaces(private val repository: SavedPlacesRepository) {
    operator fun invoke(places: List<SavedPlace>): Boolean = repository.save(places)
}
