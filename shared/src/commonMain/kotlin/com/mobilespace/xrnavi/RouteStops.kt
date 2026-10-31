package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

internal data class RouteStop(
    val stopId: String,
    val address: String,
    val detail: String,
    val placeName: String? = null
)

internal data class RouteStopsState(
    val applied: List<RouteStop> = emptyList(),
    val draft: List<RouteStop>? = null,
    val nextStopOrdinal: Int = 1,
    val searchStopId: String? = null,
    val searchIsNew: Boolean = false,
    val query: String = ""
) {
    val visibleStops: List<RouteStop> get() = draft ?: applied
    val isEditing: Boolean get() = draft != null

    companion object {
        val Saver: Saver<RouteStopsState, *> = listSaver(
            save = {
                listOf(
                    encodeRouteStops(it.applied),
                    it.draft?.let(::encodeRouteStops).orEmpty(),
                    it.draft != null,
                    it.nextStopOrdinal,
                    it.searchStopId.orEmpty(),
                    it.searchIsNew,
                    it.query
                )
            },
            restore = { values ->
                val applied = decodeRouteStops(values[0] as String) as? RouteStopsDecodeResult.Success
                val draftValue = values[1] as String
                val draft = if (values[2] as Boolean) {
                    decodeRouteStops(draftValue) as? RouteStopsDecodeResult.Success
                } else {
                    null
                }
                if (applied == null || ((values[2] as Boolean) && draft == null)) {
                    null
                } else {
                    val searchStopId = (values[4] as String).ifBlank { null }
                    val restoredDraft = draft?.stops
                    val targetExists = searchStopId == null ||
                        (values[5] as Boolean && restoredDraft != null) ||
                        restoredDraft?.any { it.stopId == searchStopId } == true
                    if (!targetExists) {
                        null
                    } else {
                        RouteStopsState(
                            applied = applied.stops,
                            draft = restoredDraft,
                            nextStopOrdinal = values[3] as Int,
                            searchStopId = searchStopId,
                            searchIsNew = values[5] as Boolean,
                            query = values[6] as String
                        )
                    }
                }
            }
        )
    }
}

internal sealed interface RouteStopsEvent {
    data object BeginEditing : RouteStopsEvent
    data object AddStop : RouteStopsEvent
    data class ReplaceAddress(val stopId: String) : RouteStopsEvent
    data class UpdateQuery(val query: String) : RouteStopsEvent
    data class SelectDemoPlace(val stopId: String, val place: DemoPlace) : RouteStopsEvent
    data class SelectSavedPlace(val stopId: String, val place: SavedPlace) : RouteStopsEvent
    data object CancelSearch : RouteStopsEvent
    data class RemoveStop(val stopId: String) : RouteStopsEvent
    data class MoveUp(val stopId: String) : RouteStopsEvent
    data class MoveDown(val stopId: String) : RouteStopsEvent
    data object Save : RouteStopsEvent
    data object CancelEditing : RouteStopsEvent
}

internal fun reduceRouteStops(
    state: RouteStopsState,
    event: RouteStopsEvent
): RouteStopsState = when (event) {
    RouteStopsEvent.BeginEditing -> state.copy(
        draft = state.applied,
        searchStopId = null,
        searchIsNew = false,
        query = ""
    )

    RouteStopsEvent.AddStop -> {
        if (state.draft == null || state.searchStopId != null) {
            state
        } else {
            val stopId = "stop-${state.nextStopOrdinal}"
            state.copy(
                nextStopOrdinal = state.nextStopOrdinal + 1,
                searchStopId = stopId,
                searchIsNew = true,
                query = ""
            )
        }
    }

    is RouteStopsEvent.ReplaceAddress -> {
        if (state.draft?.any { it.stopId == event.stopId } != true) {
            state
        } else {
            state.copy(searchStopId = event.stopId, searchIsNew = false, query = "")
        }
    }

    is RouteStopsEvent.UpdateQuery -> if (state.searchStopId == null) {
        state
    } else {
        state.copy(query = event.query)
    }

    is RouteStopsEvent.SelectDemoPlace -> state.selectPlace(
        stopId = event.stopId,
        place = DemoPlace(event.place.title.trim(), event.place.detail.trim())
    )

    is RouteStopsEvent.SelectSavedPlace -> state.selectPlace(
        stopId = event.stopId,
        place = DemoPlace(event.place.address.trim(), event.place.detail.trim()),
        placeName = event.place.label.trim().takeIf {
            it.isNotBlank() && !it.equals(event.place.address, ignoreCase = true)
        }
    )

    RouteStopsEvent.CancelSearch -> state.copy(
        searchStopId = null,
        searchIsNew = false,
        query = ""
    )

    is RouteStopsEvent.RemoveStop -> {
        state.draft?.let { draft ->
            val updated = draft.filterNot { it.stopId == event.stopId }
            if (updated.size == draft.size && state.searchStopId != event.stopId) {
                state
            } else {
                state.copy(
                    draft = updated,
                    searchStopId = state.searchStopId.takeUnless { it == event.stopId },
                    searchIsNew = if (state.searchStopId == event.stopId) false else state.searchIsNew
                )
            }
        } ?: state
    }

    is RouteStopsEvent.MoveUp -> state.move(event.stopId, -1)
    is RouteStopsEvent.MoveDown -> state.move(event.stopId, 1)

    RouteStopsEvent.Save -> state.copy(
        applied = state.draft ?: state.applied,
        draft = null,
        searchStopId = null,
        searchIsNew = false,
        query = ""
    )

    RouteStopsEvent.CancelEditing -> state.copy(
        draft = null,
        searchStopId = null,
        searchIsNew = false,
        query = ""
    )
}

private fun RouteStopsState.selectPlace(
    stopId: String,
    place: DemoPlace,
    placeName: String? = null
): RouteStopsState {
    if (searchStopId != stopId || place.title.isBlank() || place.detail.isBlank()) return this
    val currentDraft = draft ?: return copy(searchStopId = null, searchIsNew = false, query = "")
    val selectedStop = RouteStop(stopId, place.title, place.detail, placeName)
    val updated = if (searchIsNew) {
        if (currentDraft.any { it.stopId == stopId }) currentDraft else currentDraft + selectedStop
    } else {
        if (currentDraft.none { it.stopId == stopId }) {
            return copy(searchStopId = null, searchIsNew = false, query = "")
        }
        currentDraft.map { if (it.stopId == stopId) selectedStop else it }
    }
    return copy(
        draft = updated,
        searchStopId = null,
        searchIsNew = false,
        query = ""
    )
}

private fun RouteStopsState.move(stopId: String, offset: Int): RouteStopsState {
    val currentDraft = draft ?: return this
    val index = currentDraft.indexOfFirst { it.stopId == stopId }
    val targetIndex = index + offset
    if (index < 0 || targetIndex !in currentDraft.indices) return this
    val reordered = currentDraft.toMutableList()
    val selected = reordered.removeAt(index)
    reordered.add(targetIndex, selected)
    return copy(draft = reordered)
}

internal fun routeStopsPreviewBlockMessage(stops: List<RouteStop>): String? =
    "Багатоточковий перегляд ще не підключено".takeIf { stops.isNotEmpty() }

internal fun routeStopsChanged(applied: List<RouteStop>, candidate: List<RouteStop>): Boolean =
    applied != candidate

internal sealed interface RouteStopsDecodeResult {
    data class Success(val stops: List<RouteStop>) : RouteStopsDecodeResult
    data object Invalid : RouteStopsDecodeResult
}

internal data class RouteStopsSnapshot(
    val stops: List<RouteStop>,
    val nextStopOrdinal: Int
)

internal interface RouteStopsStore {
    fun read(): String?
    fun write(value: String): Boolean
}

internal const val routeStopsPreferenceKey = "route.stops.v1"

internal fun encodeRouteStops(stops: List<RouteStop>): String = buildString {
    append("v2:")
    append(stops.size)
    append(':')
    stops.forEach { stop ->
        appendLengthPrefixed(stop.stopId)
        appendLengthPrefixed(stop.address)
        appendLengthPrefixed(stop.detail)
        appendLengthPrefixed(stop.placeName.orEmpty())
    }
}

internal fun decodeRouteStops(value: String?): RouteStopsDecodeResult {
    if (value == null) return RouteStopsDecodeResult.Success(emptyList())
    return runCatching {
        val hasPlaceNames = value.startsWith("v2:")
        var cursor = if (hasPlaceNames) 3 else 0
        val countTokenEnd = value.indexOf(':', cursor)
        require(countTokenEnd >= cursor)
        val count = value.substring(cursor, countTokenEnd).toInt()
        require(count >= 0 && count <= value.length / 6)
        cursor = countTokenEnd + 1
        val stops = List(count) {
            val id = value.readLengthPrefixed(cursor)
            cursor = id.second
            val address = value.readLengthPrefixed(cursor)
            cursor = address.second
            val detail = value.readLengthPrefixed(cursor)
            cursor = detail.second
            val placeName = if (hasPlaceNames) value.readLengthPrefixed(cursor) else null
            if (placeName != null) cursor = placeName.second
            require(id.first.matches(Regex("stop-[1-9][0-9]*")))
            require(address.first.isNotBlank() && detail.first.isNotBlank())
            RouteStop(
                id.first,
                address.first,
                detail.first,
                placeName?.first?.ifBlank { null }
            )
        }
        require(cursor == value.length)
        require(stops.map { it.stopId }.distinct().size == stops.size)
        RouteStopsDecodeResult.Success(stops)
    }.getOrElse { RouteStopsDecodeResult.Invalid }
}

internal fun encodeRouteStopsSnapshot(snapshot: RouteStopsSnapshot): String =
    "${snapshot.nextStopOrdinal}:${encodeRouteStops(snapshot.stops)}"

internal fun decodeRouteStopsSnapshot(value: String?): RouteStopsSnapshot? {
    if (value == null) return RouteStopsSnapshot(emptyList(), 1)
    return runCatching {
        val separator = value.indexOf(':')
        require(separator > 0)
        val nextOrdinal = value.substring(0, separator).toInt()
        require(nextOrdinal > 0)
        val decoded = decodeRouteStops(value.substring(separator + 1))
        val stops = (decoded as? RouteStopsDecodeResult.Success)?.stops ?: error("Invalid stops")
        val minimumNextOrdinal = stops.maxOfOrNull {
            it.stopId.removePrefix("stop-").toInt()
        }?.plus(1) ?: 1
        require(nextOrdinal >= minimumNextOrdinal)
        RouteStopsSnapshot(stops, nextOrdinal)
    }.getOrNull()
}

private fun String.readLengthPrefixed(start: Int): Pair<String, Int> {
    require(start in indices)
    val colon = indexOf(':', start)
    require(colon >= start)
    val fieldLength = substring(start, colon).toInt()
    require(fieldLength >= 0)
    val contentStart = colon + 1
    val contentEnd = contentStart + fieldLength
    require(contentEnd <= this.length)
    return substring(contentStart, contentEnd) to contentEnd
}

private fun StringBuilder.appendLengthPrefixed(value: String) {
    append(value.length)
    append(':')
    append(value)
}

@androidx.compose.runtime.Composable
internal expect fun rememberRouteStopsStore(): RouteStopsStore
