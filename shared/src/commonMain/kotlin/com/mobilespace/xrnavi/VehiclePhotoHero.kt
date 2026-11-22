package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun VehiclePhotoHero(
    title: String,
    onBack: () -> Unit,
    state: VehiclePhotosState,
    dispatch: (VehiclePhotosEvent) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("vehicle-photo-hero")) {
        val window = LocalWindowInfo.current.containerSize
        val imageHeight = if (window.width > window.height) 196.dp
        else if (maxWidth < 600.dp) 220.dp else 266.dp
        VehiclePhotoSlider(state, dispatch, imageHeight) {
            Column(
                Modifier.fillMaxSize().headerSafeTop()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    Modifier.padding(start = 8.dp, end = 24.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    AuthBackButton(onBack, "Назад")
                    ProductWordmark()
                }
                HeroTitle(
                    title,
                    horizontalLayout = false,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}
