package com.mobilespace.xrnavi.presentation.routestops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobilespace.xrnavi.RouteStopsEvent
import com.mobilespace.xrnavi.RouteStopsSnapshot
import com.mobilespace.xrnavi.RouteStopsState
import com.mobilespace.xrnavi.RouteStopsStore
import com.mobilespace.xrnavi.data.routestops.LocalRouteStopsRepository
import com.mobilespace.xrnavi.domain.routestops.LoadRouteStops
import com.mobilespace.xrnavi.domain.routestops.PersistRouteStops
import com.mobilespace.xrnavi.domain.routestops.RouteStopsRepository
import com.mobilespace.xrnavi.reduceRouteStops
import com.mobilespace.xrnavi.routeStopsChanged
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class RouteStopsUiState(
    val route: RouteStopsState,
    val notice: RouteStopsNotice? = null,
    val canSave: Boolean
)

internal enum class RouteStopsNotice {
    DataUnreadable,
    SaveUnavailable,
    SaveFailed,
    MultiStopPreviewUnavailable
}

internal sealed interface RouteStopsEffect {
    data class Saved(val changed: Boolean) : RouteStopsEffect
}

internal class RouteStopsBloc(
    loadRouteStops: LoadRouteStops,
    private val persistRouteStops: PersistRouteStops,
    restoredRoute: RouteStopsState? = null
) : ViewModel() {
    private val initialSnapshot = loadRouteStops()
    private val mutableState = MutableStateFlow(
        RouteStopsUiState(
            route = restoredRoute ?: RouteStopsState(
                applied = initialSnapshot?.stops.orEmpty(),
                nextStopOrdinal = initialSnapshot?.nextStopOrdinal ?: 1
            ),
            notice = if (initialSnapshot == null) RouteStopsNotice.DataUnreadable else null,
            canSave = initialSnapshot != null
        )
    )
    val state: StateFlow<RouteStopsUiState> = mutableState.asStateFlow()

    fun dispatch(event: RouteStopsEvent): RouteStopsEffect? {
        val current = mutableState.value
        if (event == RouteStopsEvent.Save) return save(current)
        mutableState.value = current.copy(
            route = reduceRouteStops(current.route, event),
            notice = if (
                event == RouteStopsEvent.BeginEditing ||
                event == RouteStopsEvent.CancelEditing ||
                event is RouteStopsEvent.SelectSavedPlace
            ) {
                null
            } else {
                current.notice
            }
        )
        return null
    }

    private fun save(current: RouteStopsUiState): RouteStopsEffect? {
        if (!current.canSave) {
            mutableState.value = current.copy(notice = RouteStopsNotice.SaveUnavailable)
            return null
        }
        val next = reduceRouteStops(current.route, RouteStopsEvent.Save)
        val snapshot = RouteStopsSnapshot(next.applied, next.nextStopOrdinal)
        if (!persistRouteStops(snapshot)) {
            mutableState.value = current.copy(notice = RouteStopsNotice.SaveFailed)
            return null
        }
        val changed = routeStopsChanged(current.route.applied, next.applied)
        mutableState.value = current.copy(
            route = next,
            // TODO: Send these persisted waypoints to a routing use case that supports multi-stop routes.
            notice = if (next.applied.isNotEmpty()) {
                RouteStopsNotice.MultiStopPreviewUnavailable
            } else {
                null
            }
        )
        return RouteStopsEffect.Saved(changed)
    }
}

internal fun routeStopsBlocFactory(
    store: RouteStopsStore,
    restoredRoute: RouteStopsState? = null
) = viewModelFactory {
    initializer {
        val repository: RouteStopsRepository = LocalRouteStopsRepository(store)
        RouteStopsBloc(
            loadRouteStops = LoadRouteStops(repository),
            persistRouteStops = PersistRouteStops(repository),
            restoredRoute = restoredRoute
        )
    }
}
