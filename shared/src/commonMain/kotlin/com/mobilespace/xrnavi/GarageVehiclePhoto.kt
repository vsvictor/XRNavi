package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun GarageVehiclePhoto(photo: VehiclePhoto?, modifier: Modifier) {
    val defaultPhoto = painterResource(defaultVehiclePhoto(isDarkXrnaviTheme()))
    if (photo == null) {
        Image(
            painter = defaultPhoto,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photo.url)
                .httpHeaders(
                    NetworkHeaders.Builder()
                        .set("User-Agent", "XRNavi/1.0 (https://github.com/vsvictor/XRNavi)")
                        .build()
                )
                .build(),
            contentDescription = null,
            placeholder = defaultPhoto,
            error = defaultPhoto,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}
