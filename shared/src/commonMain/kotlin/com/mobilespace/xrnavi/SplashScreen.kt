package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
internal fun SplashScreen(state: SplashState, darkTheme: Boolean) {
    val font = rememberSpaceGroteskFontFamily()
    val foreground = if (darkTheme) Color(0xFFF2F5FA) else Color(0xFF172033)
    BoxWithConstraints(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .testTag("splash-screen"),
        contentAlignment = Alignment.Center,
    ) {
        val tablet = min(maxWidth.value, maxHeight.value) >= 600f
        val motion = splashMotion(state.elapsedMillis, tablet)
        val identityHeight = if (tablet) 196.19258.dp else 160.64444.dp
        Box(
            Modifier.width(if (tablet) 280.dp else 210.dp).height(identityHeight)
                .graphicsLayer {
                    scaleX = motion.scale
                    scaleY = motion.scale
                    alpha = motion.alpha
                    translationX = motion.translationX.dp.toPx()
                    translationY = motion.translationY.dp.toPx()
                },
        ) {
            SplashOrbit(
                color = if (darkTheme) Color(0xFF9AAFFF) else Color(0xFF335CFF),
                orbitDegrees = motion.orbitDegrees,
                modifier = Modifier.align(Alignment.TopCenter)
                    .offset(y = if (tablet) 22.09629.dp else 10.55469.dp)
                    .size(if (tablet) 96.dp else 83.53511.dp)
                    .testTag("splash-orbit"),
            )
            Text(
                "XRNavi",
                Modifier.fillMaxWidth()
                    .offset(y = if (tablet) 138.09629.dp else 114.08975.dp)
                    .testTag("splash-wordmark"),
                color = foreground,
                fontFamily = font,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "Продукт Mobile space",
            Modifier.width(if (tablet) 280.dp else 210.dp)
                .offset(y = identityHeight / 2 + 8.dp + 8.dp)
                .testTag("splash-endorsement"),
            color = foreground.copy(alpha = 0.75f),
            fontFamily = font,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
            textAlign = TextAlign.Center,
        )
    }
}
