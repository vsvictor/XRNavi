package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.garage_mustang_gt_hero

@Composable
internal fun GarageScreen(
    state: GarageState,
    dispatch: (GarageEvent) -> Unit,
    onAddVehicle: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onEditVehicle: (GarageVehicle) -> Unit,
    landscape: Boolean = false,
) {
    val content: @Composable ColumnScope.() -> Unit = {
        GarageListContent(
            state = state,
            dispatch = dispatch,
            onAddVehicle = onAddVehicle,
            onOpenDetails = onOpenDetails,
            onEditVehicle = onEditVehicle,
            showTitle = landscape,
        )
    }

    if (landscape) {
        GarageLandscapeLayout(content = content)
    } else {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SignInHero(
                modifier = Modifier.fillMaxWidth().height(196.dp),
                dark = isDarkXrnaviTheme(),
                horizontalLayout = false,
                image = Res.drawable.garage_mustang_gt_hero,
                grayscale = true,
                title = "Мій гараж",
                showOverlayContent = false,
            )
            content()
        }
    }
}
