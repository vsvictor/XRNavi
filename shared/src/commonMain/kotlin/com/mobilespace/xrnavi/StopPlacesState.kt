package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal enum class StopPlaceCategory(val label: String) {
    Parking("Стоянки"), Rest("Відпочинок"), Fuel("Пальне"), Charging("Зарядки"),
    Service("Сервіс"), Weighing("Вагові"), Border("Кордон")
}

internal data class StopPlace(
    val id: String,
    val name: String,
    val category: StopPlaceCategory,
    val address: DemoPlace,
    val description: String
)

internal val stopPlaces = listOf(
    StopPlace(
        "demo-parking-01",
        "Демонстраційна стоянка 01",
        StopPlaceCategory.Parking,
        demoPlaces[0],
        "Синтетичний приклад стоянки. Це не підтвердження існування об’єкта за адресою."
    ),
    StopPlace(
        "demo-rest-01", "Демонстраційне місце відпочинку 01", StopPlaceCategory.Rest,
        demoPlaces[1], "Синтетичний приклад місця відпочинку, не рекомендація безпечної зупинки."
    ),
    StopPlace(
        "demo-fuel-01", "Демонстраційна АЗС 01", StopPlaceCategory.Fuel,
        demoPlaces[3], "Синтетичний приклад АЗС. Послуги та можливість заїзду не перевірено."
    )
)

internal data class StopPlacesState(
    val category: StopPlaceCategory = StopPlaceCategory.Parking,
    val query: String = "",
    val selectedId: String? = null,
    val notice: String = "",
    val source: StopPlacesSource = StopPlacesSource.Map,
    val targetStopId: String? = null
) {
    val results: List<StopPlace>
        get() = stopPlaces.filter {
            it.category == category && (
                    query.isBlank() || listOf(
                        it.name,
                        it.address.title,
                        it.address.detail
                    ).any { text ->
                        text.contains(query.trim(), ignoreCase = true)
                    }
                    )
        }
    val selected: StopPlace? get() = stopPlaces.firstOrNull { it.id == selectedId }

    companion object {
        val Saver = listSaver<StopPlacesState, String>(
            save = {
                listOf(
                    it.category.name,
                    it.query,
                    it.selectedId.orEmpty(),
                    it.notice,
                    it.source.name,
                    it.targetStopId.orEmpty()
                )
            },
            restore = {
                StopPlacesState(
                    StopPlaceCategory.valueOf(it[0]), it[1], it[2].ifBlank { null }, it[3],
                    StopPlacesSource.valueOf(it[4]), it[5].ifBlank { null }
                )
            }
        )
    }
}

internal enum class StopPlacesSource { Map, StopSearch }
internal enum class StopPlacesDestination { Catalog, Map, StopSearch, Editor }
internal data class StopPlacesTransition(
    val catalog: StopPlacesState,
    val routeStops: RouteStopsState,
    val destination: StopPlacesDestination? = null,
    val beganEditing: Boolean = false
)

internal sealed interface StopPlacesEvent {
    data class Open(val source: StopPlacesSource) : StopPlacesEvent
    data class Category(val category: StopPlaceCategory) : StopPlacesEvent
    data class Query(val value: String) : StopPlacesEvent
    data class Details(val id: String) : StopPlacesEvent
    data class Unavailable(val action: String) : StopPlacesEvent
    data object BackToList : StopPlacesEvent
    data object Back : StopPlacesEvent
    data object AddSelected : StopPlacesEvent
}
