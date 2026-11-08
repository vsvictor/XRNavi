package com.mobilespace.xrnavi

import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Routes the current additional screen to its own composable; holds no screen state itself. */
@Composable
internal fun AdditionalScreens(
    state: AdditionalNavigationState,
    onEvent: (AdditionalNavigationEvent) -> Unit
) {
    val screen = state.screen ?: return
    val notices: AdditionalNoticeViewModel = viewModel { AdditionalNoticeViewModel() }
    val noticeState by notices.bloc.state.collectAsStateWithLifecycle()
    LaunchedEffect(screen) { notices.bloc.dispatch(AdditionalNoticeEvent.Dismiss) }
    val back = { onEvent(AdditionalNavigationEvent.Back) }
    val actions = AdditionalActions(
        open = { onEvent(AdditionalNavigationEvent.Open(it)) },
        back = back,
        section = { onEvent(AdditionalNavigationEvent.Section(it)) },
        unavailable = { notices.bloc.dispatch(AdditionalNoticeEvent.Unavailable(it)) }
    )
    AuthBackHandler(back)
    if (screen.statePreview) {
        Box(Modifier.fillMaxSize().exportTestTags().testTag("design-${screen.designId}")) {
            when (screen) {
                AdditionalScreen.SelectedPoint -> SelectedPointScreen(actions)
                AdditionalScreen.LocationAcquiring -> LocationAcquiringScreen(actions)
                AdditionalScreen.LocationDenied -> LocationDeniedScreen(actions)
                AdditionalScreen.LocationDisabled -> LocationDisabledScreen(actions)
                AdditionalScreen.SearchLoading -> SearchLoadingScreen(actions)
                AdditionalScreen.SearchEmpty -> SearchEmptyScreen(actions)
                AdditionalScreen.SearchError -> SearchErrorScreen(actions)
                AdditionalScreen.RouteBuilding -> RouteBuildingScreen(actions)
                AdditionalScreen.RouteFailed -> RouteFailedScreen(actions)
                else -> error("Not a state preview: $screen")
            }
            AdditionalNoticeDialog(noticeState.notice) {
                notices.bloc.dispatch(AdditionalNoticeEvent.Dismiss)
            }
        }
        return
    }
    AdditionalScreenFrame(screen, actions) { layout ->
        when (screen) {
            AdditionalScreen.Company -> CompanyScreen(layout, actions)
            AdditionalScreen.Invitation -> InvitationScreen(layout, actions)
            AdditionalScreen.TripSummary -> TripSummaryScreen(layout, actions)
            AdditionalScreen.VehicleCatalog -> VehicleCatalogScreen(layout, actions)
            AdditionalScreen.VehicleModel -> VehicleModelScreen(layout, actions)
            AdditionalScreen.VehicleReview -> VehicleReviewScreen(layout, actions)
            AdditionalScreen.RouteUnverified -> RouteUnverifiedScreen(layout, actions)
            AdditionalScreen.OfflineMaps -> OfflineMapsScreen(layout, actions)
            AdditionalScreen.DriverTasks -> DriverTasksScreen(layout, actions)
            AdditionalScreen.TaskDetails -> TaskDetailsScreen(layout, actions)
            AdditionalScreen.TripHistory -> TripHistoryScreen(layout, actions)
            AdditionalScreen.Privacy -> PrivacyScreen(layout, actions)
            AdditionalScreen.DeleteAccount -> DeleteAccountScreen(layout, actions)
            AdditionalScreen.Diagnostics -> DiagnosticsScreen(layout, actions)
            AdditionalScreen.RouteExplanation -> RouteExplanationScreen(layout, actions)
            AdditionalScreen.LocationEducation -> LocationEducationScreen(actions)
            else -> error("State previews render without the frame: $screen")
        }
    }
    AdditionalNoticeDialog(noticeState.notice) {
        notices.bloc.dispatch(AdditionalNoticeEvent.Dismiss)
    }
}
