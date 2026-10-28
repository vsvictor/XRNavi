package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun DemoVehicleImage(
    vehicle: DemoVehicleProfile,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    thumbnail: Boolean = false
) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    val resource = when (vehicle.artwork) {
        DemoVehicleArtwork.Mustang -> when {
            thumbnail && dark -> Res.drawable.mustang_thumbnail_dark
            thumbnail -> Res.drawable.mustang_thumbnail_light
            dark -> Res.drawable.profile_mustang_hero_dark
            else -> Res.drawable.profile_mustang_hero_light
        }

        DemoVehicleArtwork.Passenger ->
            if (dark) Res.drawable.vehicle_car_dark else Res.drawable.vehicle_car_light

        DemoVehicleArtwork.Cargo ->
            if (dark) Res.drawable.vehicle_truck_dark else Res.drawable.vehicle_truck_light

        DemoVehicleArtwork.Special ->
            if (dark) Res.drawable.vehicle_special_dark else Res.drawable.vehicle_special_light
    }
    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        contentScale = if (vehicle.artwork == DemoVehicleArtwork.Mustang) {
            ContentScale.Crop
        } else {
            ContentScale.Fit
        },
        modifier = modifier
    )
}
