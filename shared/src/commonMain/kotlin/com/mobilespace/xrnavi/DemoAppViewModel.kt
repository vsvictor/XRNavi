package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class DemoAppViewModel(
    private val savedPlacesStore: SavedPlacesStore,
    private val routeStopsStore: RouteStopsStore,
    initialScreen: AppScreen = initialScreenForSession(false)
) : ViewModel() {
    private val savedPlacesInitial = decodeSavedPlaces(savedPlacesStore.read())
    private val routeStopsInitial = decodeRouteStopsSnapshot(routeStopsStore.read())
    private val savedPlacesLoaded = savedPlacesInitial as? SavedPlacesDecodeResult.Success

    val canSaveRouteStops: Boolean = routeStopsInitial != null

    val bloc = DemoAppBloc(
        DemoAppState(
            screen = initialScreen,
            routeStops = RouteStopsState(
                applied = routeStopsInitial?.stops.orEmpty(),
                nextStopOrdinal = routeStopsInitial?.nextStopOrdinal ?: 1
            ),
            routeStopsNotice = if (routeStopsInitial == null) {
                "Дані зупинок не вдалося прочитати. Існуючий вміст не перезаписано."
            } else {
                ""
            },
            savedPlaces = savedPlacesLoaded?.places.orEmpty(),
            savedPlacesNotice = if (savedPlacesInitial is SavedPlacesDecodeResult.Invalid) {
                "Збережені дані пошкоджені. Їх не перезаписано; зміни недоступні."
            } else {
                ""
            }
        )
    )

    fun dispatch(event: DemoAppEvent) = bloc.dispatch(event)

    fun saveRouteStops() {
        val current = bloc.state.value.routeStops
        if (routeStopsInitial == null) {
            dispatch(
                DemoAppEvent.RouteStopsSaveFailed(
                    "Дані зупинок недоступні для запису; попередній вміст не змінено."
                )
            )
            return
        }
        val next = reduceRouteStops(current, RouteStopsEvent.Save)
        val written = routeStopsStore.write(
            encodeRouteStopsSnapshot(RouteStopsSnapshot(next.applied, next.nextStopOrdinal))
        )
        dispatch(
            if (written) DemoAppEvent.RouteStopsSaved(next)
            else DemoAppEvent.RouteStopsSaveFailed("Не вдалося зберегти зупинки на цьому пристрої.")
        )
    }

    fun persistSavedPlaces(next: List<SavedPlace>): Boolean {
        if (savedPlacesLoaded == null) {
            dispatch(
                DemoAppEvent.SavedPlacesNoticeChanged(
                    "Збережені дані не вдалося прочитати. Існуючий вміст не перезаписано."
                )
            )
            return false
        }
        return if (savedPlacesStore.write(encodeSavedPlaces(next))) {
            dispatch(DemoAppEvent.SavedPlacesChanged(next))
            true
        } else {
            dispatch(
                DemoAppEvent.SavedPlacesNoticeChanged("Не вдалося зберегти зміни на цьому пристрої.")
            )
            false
        }
    }

    fun saveDemoPlace(place: DemoPlace): SavedPlaceAddStatus {
        if (savedPlacesLoaded == null) {
            dispatch(
                DemoAppEvent.SavedPlacesNoticeChanged(
                    "Збережені дані не вдалося прочитати. Нову адресу не додано."
                )
            )
            return SavedPlaceAddStatus.Invalid
        }
        val result = addSavedPlace(bloc.state.value.savedPlaces, place)
        return when (result.status) {
            SavedPlaceAddStatus.Added ->
                if (persistSavedPlaces(result.places)) SavedPlaceAddStatus.Added
                else SavedPlaceAddStatus.Invalid

            SavedPlaceAddStatus.Duplicate -> SavedPlaceAddStatus.Duplicate
            SavedPlaceAddStatus.Invalid -> SavedPlaceAddStatus.Invalid
        }
    }

    fun renamePlace(key: String, label: String) {
        val updated = renameSavedPlace(bloc.state.value.savedPlaces, key, label)
        if (updated == null) {
            dispatch(
                DemoAppEvent.SavedPlacesNoticeChanged("Введіть непорожній ярлик і спробуйте ще раз.")
            )
        } else {
            persistSavedPlaces(updated)
        }
    }

    fun deletePlace(place: SavedPlace) {
        persistSavedPlaces(removeSavedPlace(bloc.state.value.savedPlaces, place.key))
    }
}

internal fun demoAppViewModelFactory(
    savedPlacesStore: SavedPlacesStore,
    routeStopsStore: RouteStopsStore
): ViewModelProvider.Factory = viewModelFactory {
    initializer { DemoAppViewModel(savedPlacesStore, routeStopsStore) }
}
