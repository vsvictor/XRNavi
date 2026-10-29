package com.mobilespace.xrnavi

internal data class SavedPlace(
    val address: String,
    val detail: String,
    val label: String
) {
    val key: String get() = "${address.length}:$address${detail.length}:$detail"
}

internal fun DemoPlace.toSavedPlace(): SavedPlace =
    SavedPlace(title.trim(), detail.trim(), title.trim())

internal sealed interface SavedPlacesDecodeResult {
    data class Success(val places: List<SavedPlace>) : SavedPlacesDecodeResult
    data object Invalid : SavedPlacesDecodeResult
}

internal enum class SavedPlaceAddStatus {
    Added,
    Duplicate,
    Invalid
}

internal enum class SavedPlacesOrigin {
    Search,
    Profile
}

internal data class SavedPlaceAddResult(
    val places: List<SavedPlace>,
    val status: SavedPlaceAddStatus,
    val existing: SavedPlace? = null
)

internal interface SavedPlacesStore {
    fun read(): String?
    fun write(value: String): Boolean
}

internal const val savedPlacesPreferenceKey = "saved.places.v1"

internal fun addSavedPlace(
    places: List<SavedPlace>,
    place: DemoPlace
): SavedPlaceAddResult {
    val address = place.title.trim()
    val detail = place.detail.trim()
    if (address.isEmpty() || detail.isEmpty()) {
        return SavedPlaceAddResult(places, SavedPlaceAddStatus.Invalid)
    }
    val existing = places.firstOrNull {
        it.address.equals(address, ignoreCase = true) &&
            it.detail.equals(detail, ignoreCase = true)
    }
    if (existing != null) {
        return SavedPlaceAddResult(places, SavedPlaceAddStatus.Duplicate, existing)
    }
    val saved = SavedPlace(address, detail, address)
    return SavedPlaceAddResult(places + saved, SavedPlaceAddStatus.Added, saved)
}

internal fun renameSavedPlace(
    places: List<SavedPlace>,
    key: String,
    label: String
): List<SavedPlace>? {
    val normalizedLabel = label.trim()
    if (normalizedLabel.isEmpty() || places.none { it.key == key }) return null
    return places.map { if (it.key == key) it.copy(label = normalizedLabel) else it }
}

internal fun removeSavedPlace(places: List<SavedPlace>, key: String): List<SavedPlace> =
    places.filterNot { it.key == key }

internal fun encodeSavedPlaces(places: List<SavedPlace>): String = buildString {
    append(places.size)
    append(':')
    places.forEach { place ->
        appendLengthPrefixed(place.address)
        appendLengthPrefixed(place.detail)
        appendLengthPrefixed(place.label)
    }
}

internal fun decodeSavedPlaces(value: String?): SavedPlacesDecodeResult {
    if (value == null) return SavedPlacesDecodeResult.Success(emptyList())
    return runCatching {
        var cursor = 0
        val countTokenEnd = value.indexOf(':', cursor)
        require(countTokenEnd >= 0)
        val count = value.substring(cursor, countTokenEnd).toInt()
        require(count in 0..1_000)
        cursor = countTokenEnd + 1
        val places = List(count) {
            val address = value.readLengthPrefixed(cursor)
            cursor = address.second
            val detail = value.readLengthPrefixed(cursor)
            cursor = detail.second
            val label = value.readLengthPrefixed(cursor)
            cursor = label.second
            require(address.first.isNotBlank() && detail.first.isNotBlank() && label.first.isNotBlank())
            SavedPlace(address.first, detail.first, label.first)
        }
        require(cursor == value.length)
        require(places.map { it.key }.distinct().size == places.size)
        SavedPlacesDecodeResult.Success(places)
    }.getOrElse { SavedPlacesDecodeResult.Invalid }
}

private fun String.readLengthPrefixed(start: Int): Pair<String, Int> {
    require(start in indices)
    val colon = indexOf(':', start)
    require(colon >= start)
    val length = substring(start, colon).toInt()
    require(length >= 0)
    val contentStart = colon + 1
    val contentEnd = contentStart + length
    require(contentEnd <= this.length)
    return substring(contentStart, contentEnd) to contentEnd
}

private fun StringBuilder.appendLengthPrefixed(value: String) {
    append(value.length)
    append(':')
    append(value)
}

@androidx.compose.runtime.Composable
internal expect fun rememberSavedPlacesStore(): SavedPlacesStore
