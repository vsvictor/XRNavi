package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun SignInHero(
    modifier: Modifier,
    dark: Boolean,
    horizontalLayout: Boolean
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (horizontalLayout) {
                        Brush.horizontalGradient(
                            colorStops = if (dark) {
                                arrayOf(
                                    0f to Color(0x660B1220),
                                    0.5f to Color.Transparent,
                                    0.78f to Color(0xCC0B1220),
                                    1f to Color(0xFF0B1220)
                                )
                            } else {
                                arrayOf(
                                    0f to Color(0x66F6F8FC),
                                    0.45f to Color.Transparent,
                                    0.78f to Color(0xF7FFFFFF),
                                    1f to Color.White
                                )
                            }
                        )
                    } else {
                        Brush.verticalGradient(
                            colorStops = if (dark) {
                                arrayOf(
                                    0f to Color(0x660B1220),
                                    0.4f to Color.Transparent,
                                    0.64f to Color(0xCC0B1220),
                                    1f to Color(0xFF0B1220)
                                )
                            } else {
                                arrayOf(
                                    0f to Color(0xDDF6F8FC),
                                    0.23f to Color.Transparent,
                                    0.44f to Color.Transparent,
                                    0.6f to Color(0xF7FFFFFF),
                                    1f to Color.White
                                )
                            }
                        )
                    }
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ProductWordmark()
            Column {
                Text(
                    text = if (horizontalLayout) {
                        "ТВІЙ РИТМ.\nТВІЙ МАРШРУТ."
                    } else {
                        "ТВІЙ РИТМ. ТВІЙ МАРШРУТ."
                    },
                    fontFamily = rememberRobotoCondensedFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (horizontalLayout) 28.sp else 38.sp,
                    lineHeight = if (horizontalLayout) 30.sp else 40.sp,
                    letterSpacing = (-0.45).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Навігація для кожного",
                    modifier = Modifier.padding(top = 7.dp),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
