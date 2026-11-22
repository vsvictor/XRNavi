package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource

@Composable
internal fun SignInHero(
    modifier: Modifier,
    dark: Boolean,
    horizontalLayout: Boolean,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    backDescription: String = "Повернутися до входу",
    image: DrawableResource? = null,
    showOverlayContent: Boolean = true,
    reducedGradient: Boolean = false,
    grayscale: Boolean = false,
) {
    BoxWithConstraints(modifier = modifier) {
        val heroWidth = maxWidth
        Image(
            painter = painterResource(image ?: if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = if (grayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(signInHeroGradient(dark, horizontalLayout, reducedGradient))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (showOverlayContent) Modifier.headerSafeTop() else Modifier)
                .padding(
                    start = 0.dp,
                    top = if (showOverlayContent) 0.dp else HeroContentTopSpacing,
                    end = 0.dp,
                    bottom = 24.dp,
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (showOverlayContent) {
                Row(
                    modifier = Modifier.padding(start = 8.dp, end = 24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (onBack != null) {
                        AuthBackButton(onBack, backDescription)
                    }
                    ProductWordmark()
                }
            } else if (!horizontalLayout && title != null) {
                Spacer(Modifier.weight(1f))
            }
            if (!horizontalLayout && (showOverlayContent || title != null)) {
                HeroTitle(
                    title = title,
                    horizontalLayout = false,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}
