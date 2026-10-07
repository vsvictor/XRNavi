package com.mobilespace.xrnavi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilespace.xrnavi.domain.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

enum class NavigationAction {
    Back, Map, Garage, Preferences, Profile, Settings, AddressSearch, Parking, NearbyStops,
    WorkTrip, TripPlan, StartPreview, StopPreview, Overview, Xr, TripHistory, ChooseOnMap, Apply, AddressSelected,
}

enum class NavigationNotice {
    LocationUnavailable, TruckValidationUnavailable, MissingDestination, ParkingDemo, PhoneUnavailable,
    AddStopUnavailable, SoundEnabled, SoundDisabled, ReportUnavailable, ShareUnavailable, MapUnavailable, MembershipRequired,
}

abstract class NavigationViewModel : ViewModel() {
    private val actionChannel = Channel<NavigationAction>(Channel.UNLIMITED)
    val effects = actionChannel.receiveAsFlow()
    fun navigate(action: NavigationAction) { actionChannel.trySend(action) }
    override fun onCleared() { actionChannel.close() }
}

data class RouteUiState(
    val destination: String,
    val context: DrivingContext,
    val vehicle: VehicleId,
    val vehicleType: Int,
    val workTask: WorkTask?,
    val routingSettings: RoutingSettings,
    val mapLayer: Int = 0,
    val notice: NavigationNotice? = null,
)

class RouteViewModel(private val session: SessionRepository) : NavigationViewModel() {
    private val local = MutableStateFlow(0 to (null as NavigationNotice?))
    private fun category(s: SessionState) = when {
        s.selectedVehicle == VehicleId.Man -> 1
        s.routingSettings.vehicleCategory == 2 -> 2
        else -> 0
    }
    private fun render(s: SessionState, layer: Int, notice: NavigationNotice?) = RouteUiState(
        s.destination, s.context, s.selectedVehicle, category(s),
        s.selectedTask.takeIf { s.context == DrivingContext.Work }, s.routingSettings, layer, notice,
    )
    val state: StateFlow<RouteUiState> = combine(session.state, local) { s, l -> render(s, l.first, l.second) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), render(session.state.value, 0, null))
    fun selectVehicle(category: Int) {
        if (!SelectRouteVehicle(session)(category)) local.update { it.first to NavigationNotice.MembershipRequired }
        else local.update { it.first to null }
    }
    fun clearDestination() { SetRouteDestination(session)("") }
    fun toggleLayers() { local.update { (it.first + 1) % 3 to it.second } }
    fun locate() { local.update { it.first to NavigationNotice.LocationUnavailable } }
    fun swap() { locate() }
    fun start() {
        val s = session.state.value
        when (CheckRoutePreview()(s.destination, category(s))) {
            RoutePreviewResult.DemoPreview -> {
                local.update { it.first to null }
                navigate(NavigationAction.StartPreview)
            }
            RoutePreviewResult.MissingDestination -> local.update { it.first to NavigationNotice.MissingDestination }
            RoutePreviewResult.TruckValidationUnavailable ->
                local.update { it.first to NavigationNotice.TruckValidationUnavailable }
        }
    }
}

data class AddressSearchUiState(
    val query: String,
    val results: List<DemoAddress>,
    val recent: List<DemoAddress>,
    val notice: NavigationNotice? = null,
    val selectedAddress: String? = null,
)

class AddressSearchViewModel(
    private val session: SessionRepository,
    private val search: SearchDemoAddresses,
    initialQuery: String,
) : NavigationViewModel() {
    private val mutableState = MutableStateFlow(AddressSearchUiState(initialQuery, search(initialQuery), search.recent()))
    val state = mutableState.asStateFlow()
    fun queryChanged(query: String) { mutableState.update { it.copy(query = query, results = search(query)) } }
    fun swap() { mutableState.update { it.copy(notice = NavigationNotice.LocationUnavailable) } }
    fun chooseOnMap() {
        mutableState.update { it.copy(notice = NavigationNotice.MapUnavailable) }
        navigate(NavigationAction.ChooseOnMap)
    }
    fun selectAddress(address: String) {
        SetRouteDestination(session)(address)
        mutableState.update { it.copy(selectedAddress = address) }
        navigate(NavigationAction.AddressSelected)
    }
}

data class RouteSettingsUiState(val draft: RoutingSettings, val notice: NavigationNotice? = null)

class RouteSettingsViewModel(private val session: SessionRepository) : NavigationViewModel() {
    private val mutableState = MutableStateFlow(RouteSettingsUiState(session.state.value.routingSettings.copy()))
    val state = mutableState.asStateFlow()
    fun beginEditing() { mutableState.value = RouteSettingsUiState(session.state.value.routingSettings.copy()) }
    fun selectVehicle(category: Int) {
        require(category in 0..2)
        mutableState.update { it.copy(draft = it.draft.copy(vehicleCategory = category)) }
    }
    fun nextTrailer() { mutableState.update { it.copy(draft = it.draft.copy(trailerIndex = (it.draft.trailerIndex + 1) % 3)) } }
    fun selectRoute(index: Int) {
        require(index in 0..2)
        mutableState.update { it.copy(draft = it.draft.copy(selectedRoute = index)) }
    }
    fun toggleRestStop() { mutableState.update { it.copy(draft = it.draft.copy(restStopEnabled = !it.draft.restStopEnabled)) } }
    fun toggleAvoidTolls() { mutableState.update { it.copy(draft = it.draft.copy(avoidTolls = !it.draft.avoidTolls)) } }
    fun apply() {
        if (ApplyRoutingSettings(session)(state.value.draft)) navigate(NavigationAction.Apply)
        else mutableState.update { it.copy(notice = NavigationNotice.MembershipRequired) }
    }
}

data class NearbyStopsUiState(
    val category: StopCategory = StopCategory.All,
    val truckFilter: Boolean = true,
    val distanceFilter: Boolean = true,
    val stops: List<DemoStop>,
    val notice: NavigationNotice? = null,
)

class NearbyStopsViewModel(private val filter: FilterDemoStops) : NavigationViewModel() {
    private val mutableState = MutableStateFlow(NearbyStopsUiState(stops = filter(StopCategory.All, true, true)))
    val state = mutableState.asStateFlow()
    private fun update(transform: (NearbyStopsUiState) -> NearbyStopsUiState) {
        mutableState.update {
            val next = transform(it)
            next.copy(stops = filter(next.category, next.truckFilter, next.distanceFilter))
        }
    }
    fun selectCategory(category: StopCategory) = update { it.copy(category = category) }
    fun toggleTruckFilter() = update { it.copy(truckFilter = !it.truckFilter) }
    fun toggleDistanceFilter() = update { it.copy(distanceFilter = !it.distanceFilter) }
    fun locate() = update { it.copy(notice = NavigationNotice.LocationUnavailable) }
}

data class ParkingStopUiState(val notice: NavigationNotice? = null)
class ParkingStopViewModel : NavigationViewModel() {
    private val mutableState = MutableStateFlow(ParkingStopUiState())
    val state = mutableState.asStateFlow()
    fun help() { mutableState.value = ParkingStopUiState(NavigationNotice.ParkingDemo) }
    fun call() { mutableState.value = ParkingStopUiState(NavigationNotice.PhoneUnavailable) }
    fun addStop() { mutableState.value = ParkingStopUiState(NavigationNotice.AddStopUnavailable) }
}

data class ActiveNavigationUiState(val soundEnabled: Boolean = true, val notice: NavigationNotice? = NavigationNotice.LocationUnavailable)
class ActiveNavigationViewModel : NavigationViewModel() {
    private val mutableState = MutableStateFlow(ActiveNavigationUiState())
    val state = mutableState.asStateFlow()
    fun toggleSound() {
        mutableState.update {
            it.copy(soundEnabled = !it.soundEnabled, notice =
                if (it.soundEnabled) NavigationNotice.SoundDisabled else NavigationNotice.SoundEnabled)
        }
    }
    fun recenter() { mutableState.update { it.copy(notice = NavigationNotice.LocationUnavailable) } }
    fun report() { mutableState.update { it.copy(notice = NavigationNotice.ReportUnavailable) } }
    fun dismissMessage() { mutableState.update { it.copy(notice = null) } }
}

data class XrNavigationUiState(val soundEnabled: Boolean = true, val showMessage: Boolean = true)
class XrNavigationViewModel : NavigationViewModel() {
    private val mutableState = MutableStateFlow(XrNavigationUiState())
    val state = mutableState.asStateFlow()
    fun toggleSound() { mutableState.update { it.copy(soundEnabled = !it.soundEnabled, showMessage = true) } }
    fun dismissMessage() { mutableState.update { it.copy(showMessage = false) } }
}

data class TripSummaryUiState(val notice: NavigationNotice? = null)
class TripSummaryViewModel : NavigationViewModel() {
    private val mutableState = MutableStateFlow(TripSummaryUiState())
    val state = mutableState.asStateFlow()
    fun share() { mutableState.value = TripSummaryUiState(NavigationNotice.ShareUnavailable) }
}
