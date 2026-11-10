package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthenticatedProfileScreen(
    profile: AuthenticatedProfile,
    layout: AccountLayout,
    garageState: GarageState,
    onBack: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenVehicleDetails: (String) -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenFavorites: () -> Unit,
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
        ProfileStatistics(layout, showUnavailableNotice = false)
    }
    val organization: @Composable ColumnScope.() -> Unit = {
        if (profile.accountType != AuthenticatedAccountType.Personal) {
            AuthenticatedProfileOrganization(profile, layout)
        }
    }
    val actions: @Composable ColumnScope.() -> Unit = {
        AuthenticatedProfileActions(layout, onOpenSecurity, onOpenFavorites)
    }

    Column(
        Modifier.fillMaxWidth().widthIn(max = 1280.dp).testTag("authenticated-profile"),
        verticalArrangement = Arrangement.spacedBy(if (layout.tablet) 16.dp else 8.dp),
    ) {
        AuthenticatedProfileHeader(onBack)
        when (layout) {
            AccountLayout.PhoneLandscape -> ResponsiveSections(
                columns = true,
                firstWeight = 0.46f,
                first = { identity(); organization(); actions() },
                second = vehicle,
            )
            AccountLayout.TabletLandscape -> ResponsiveSections(
                columns = true,
                firstWeight = 0.48f,
                gap = 24,
                first = vehicle,
                second = { identity(); organization(); actions() },
            )
            else -> {
                identity()
                vehicle()
                organization()
                actions()
            }
        }
    }
}
