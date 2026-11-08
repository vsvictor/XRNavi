package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
internal fun ColumnScope.DemoProfileScreen(
    profile: DemoProfileState,
    layout: AccountLayout,
    savedPlacesCount: Int,
    onOpenSavedPlaces: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenVehicleProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitDemo: () -> Unit,
    onRegister: () -> Unit,
    onRecovery: () -> Unit,
    onOpenAdditional: ((AdditionalScreen) -> Unit)?,
    unavailable: (String) -> Unit
) {
    val savedPlacesSubtitle = if (savedPlacesCount == 0) "Поки порожньо · локально"
    else "$savedPlacesCount місць · локально"
    val additionalActions = listOf(
        AdditionalScreen.DriverTasks, AdditionalScreen.TripHistory, AdditionalScreen.Invitation
    )
    val accountActions: @Composable ColumnScope.() -> Unit = {
        ProfileOrganization(profile, layout)
        AccountAction("Збережені місця", savedPlacesSubtitle, onOpenSavedPlaces)
        ProfileSecondaryActions(
            horizontal = layout == AccountLayout.PhonePortrait ||
                layout == AccountLayout.PhoneLandscape,
            onOpenPrivacy = onOpenAdditional?.let { open ->
                { open(AdditionalScreen.Privacy) }
            },
            onOpenSavedPlaces = onOpenSavedPlaces,
            unavailable = unavailable
        )
        AdditionalAccountActions(additionalActions, onOpenAdditional)
        TextButton(onClick = onOpenSettings) { Text("Відкрити налаштування") }
        ProfileAuthActions(onExitDemo, onRegister, onRecovery)
    }
    val identity: @Composable ColumnScope.() -> Unit = {
        ProfileIdentity(profile, layout, unavailable)
    }
    val vehicle: @Composable ColumnScope.() -> Unit = {
        ProfileVehicle(
            profile,
            layout,
            onOpenGarage = onOpenGarage,
            onOpenVehicleProfile = onOpenVehicleProfile
        )
        ProfileStatistics(layout)
    }
    when (layout) {
        AccountLayout.PhoneLandscape -> ResponsiveSections(
            columns = true,
            firstWeight = 0.46f,
            first = vehicle,
            second = { identity(); accountActions() }
        )

        AccountLayout.TabletLandscape -> ResponsiveSections(
            columns = true,
            firstWeight = 0.48f,
            gap = 24,
            first = { identity(); accountActions() },
            second = vehicle
        )

        else -> {
            identity()
            vehicle()
            if (layout == AccountLayout.TabletPortrait) {
                AccountAction("Збережені місця", savedPlacesSubtitle, onOpenSavedPlaces)
                ResponsiveSections(
                    columns = true,
                    first = { ProfileOrganization(profile, layout) },
                    second = {
                        ProfileSecondaryActions(
                            onOpenPrivacy = onOpenAdditional?.let { open ->
                                { open(AdditionalScreen.Privacy) }
                            },
                            onOpenSavedPlaces = onOpenSavedPlaces,
                            unavailable = unavailable
                        )
                        AdditionalAccountActions(additionalActions, onOpenAdditional)
                    }
                )
                TextButton(onClick = onOpenSettings) { Text("Відкрити налаштування") }
                ProfileAuthActions(onExitDemo, onRegister, onRecovery)
            } else {
                accountActions()
            }
        }
    }
    AccountCard {
        CardHeading("Про демопрофіль")
        AccountBody(profile.accountDetails)
        AccountBody("Єдиний наявний демопрофіль. Це не підтверджене володіння автомобілем.")
        AccountBody(profile.statisticsExplanation)
    }
}
