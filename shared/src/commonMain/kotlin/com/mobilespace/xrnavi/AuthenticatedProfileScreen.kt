package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthenticatedProfileScreen(
    profile: AuthenticatedProfile,
    layout: AccountLayout,
    garageState: GarageState,
    onOpenGarage: () -> Unit,
    onOpenVehicleDetails: (String) -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenFavorites: () -> Unit,
    selectedTab: ProfileAccountTab,
    onSelectTab: (ProfileAccountTab) -> Unit,
    onLogout: () -> Unit,
) {
    val identity: @Composable ColumnScope.() -> Unit = {
        AuthenticatedProfileIdentity(profile, layout)
    }
    val vehicle: @Composable ColumnScope.() -> Unit = {
        AuthenticatedProfileVehicle(
            garageState,
            onOpenGarage,
            onOpenVehicleDetails,
        )
    }
    val statistics: @Composable ColumnScope.() -> Unit = {
        AuthenticatedProfileStatistics(layout)
    }
    val actions: @Composable ColumnScope.() -> Unit = {
        AuthenticatedProfileActions(layout, onOpenSecurity, onOpenFavorites)
    }

    AccountPageLayout(
        title = "Мій профіль",
        modifier = Modifier.testTag("authenticated-profile"),
    ) {
        ProfileAccountTabs(selectedTab, onSelectTab)
        if (selectedTab == ProfileAccountTab.Corporate) {
            CorporateProfilePlaceholder()
        } else {
            when (layout) {
                AccountLayout.PhoneLandscape -> ResponsiveSections(
                    columns = true,
                    firstWeight = 0.46f,
                    first = { identity(); actions() },
                    second = { vehicle(); statistics() },
                )

                AccountLayout.TabletLandscape -> ResponsiveSections(
                    columns = true,
                    firstWeight = 0.46f,
                    gap = 24,
                    first = { identity(); actions() },
                    second = { vehicle(); statistics() },
                )

                else -> {
                    identity()
                    vehicle()
                    statistics()
                    actions()
                }

            }
            ProfileLogoutButton(onLogout)
        }
    }
}
