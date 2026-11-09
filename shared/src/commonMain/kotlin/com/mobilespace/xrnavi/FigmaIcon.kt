package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.layout.ContentScale
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun FigmaIcon(
    light: DrawableResource,
    dark: DrawableResource,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color? = null
) {
    Image(
        painter = painterResource(if (isDarkXrnaviTheme()) dark else light),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        colorFilter = tint?.let { ColorFilter.tint(it) },
        modifier = modifier
    )
}
