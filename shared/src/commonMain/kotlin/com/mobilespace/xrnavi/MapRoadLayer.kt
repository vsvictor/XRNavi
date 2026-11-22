package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.ContentScale
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MapRoadLayer(
    image: DrawableResource,
    width: Dp,
    height: Dp
) {
    Box(
        modifier = Modifier
            .offset(x = width * (-0.05f), y = height * 0.0148f)
            .size(width = width * 1.12f, height = height * 0.656f)
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = width * 0.1999f,
                    top = height * 0.123f,
                    end = width * 0.2669f,
                    bottom = height * 0.2293f
                )
        )
    }
}
