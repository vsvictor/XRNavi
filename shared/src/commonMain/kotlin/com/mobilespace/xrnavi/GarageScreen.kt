package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
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
    headerHeight: Dp = 0.dp,
    vehiclePhotos: Map<String, VehiclePhoto> = emptyMap(),
) {
    val content: @Composable ColumnScope.() -> Unit = {
        GarageListContent(
            state = state,
            dispatch = dispatch,
            onAddVehicle = onAddVehicle,
            onOpenDetails = onOpenDetails,
            onEditVehicle = onEditVehicle,
            showTitle = landscape,
            vehiclePhotos = vehiclePhotos,
        )
    }

    if (landscape) {
        GarageLandscapeLayout(headerHeight = headerHeight, content = content)
    } else {
        Column(
            Modifier.fillMaxWidth(),
        ) {
            SignInHero(
                modifier = Modifier.fillMaxWidth().height(196.dp),
                dark = isDarkXrnaviTheme(),
                horizontalLayout = false,
                image = Res.drawable.garage_mustang_gt_hero,
                grayscale = true,
                title = "Мій гараж",
            )
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), content = content)
        }
    }
}
