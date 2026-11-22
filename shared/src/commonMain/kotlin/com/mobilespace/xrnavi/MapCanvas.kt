package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MapCanvas(
    modifier: Modifier,
    preview: Boolean,
    compact: Boolean = false
) {
    val dark = isDarkXrnaviTheme()
    val showScaledMapLabels = LocalDensity.current.fontScale < 1.5f
    val land = if (dark) Color(0xFF111D2A) else Color(0xFFF1F5F9)
    val park = if (dark) Color(0xFF1C352F) else Color(0xFFDFEBD9)
    BoxWithConstraints(
        modifier = modifier.clipToBounds().background(land)
    ) {
        val w = maxWidth
        val h = if (compact) maxHeight * 2.3f else maxHeight

        Image(
            painter = painterResource(if (dark) Res.drawable.map_river_dark else Res.drawable.map_river_light),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .offset(x = w * 0.783f, y = h * 0.035f)
                .size(width = w * 0.209f, height = h * 0.636f)
        )
        Box(
            Modifier
                .offset(x = w * 0.037f, y = h * 0.194f)
                .size(width = w * 0.274f, height = h * 0.049f)
                .clip(RoundedCornerShape(18.dp))
                .background(park)
        )
        Box(
            Modifier
                .offset(x = w * 0.249f, y = h * 0.415f)
                .size(width = w * 0.204f, height = h * 0.087f)
                .clip(RoundedCornerShape(18.dp))
                .background(park)
        )
        Box(
            Modifier
                .offset(x = w * 0.684f, y = h * 0.076f)
                .size(width = w * 0.137f, height = h * 0.049f)
                .clip(RoundedCornerShape(18.dp))
                .background(park)
        )
        Box(
            Modifier
                .offset(x = w * 0.659f, y = h * 0.517f)
                .size(width = w * 0.187f, height = h * 0.067f)
                .clip(RoundedCornerShape(18.dp))
                .background(park)
        )
        MapRoadLayer(
            image = if (dark) Res.drawable.map_road_casing_dark else Res.drawable.map_road_casing_light,
            width = w,
            height = h
        )
        MapRoadLayer(
            image = if (dark) Res.drawable.map_road_network_dark else Res.drawable.map_road_network_light,
            width = w,
            height = h
        )
        if (preview) {
            Image(
                painter = painterResource(if (dark) Res.drawable.map_route_halo_dark else Res.drawable.map_route_halo_light),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(x = w * 0.112f, y = h * 0.209f)
                    .size(width = w * 0.769f, height = h * 0.186f)
            )
            Image(
                painter = painterResource(if (dark) Res.drawable.map_planned_route_dark else Res.drawable.map_planned_route_light),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(x = w * 0.112f, y = h * 0.209f)
                    .size(width = w * 0.769f, height = h * 0.186f)
            )
        }
        if (showScaledMapLabels && !compact) {
            MapPlaceLabel("Київ", w * 0.764f, h * 0.176f, strong = true)
            MapPlaceLabel("Житомир", w * 0.555f, h * 0.276f)
            MapPlaceLabel("Рівне", w * 0.274f, h * 0.315f)
            MapPlaceLabel("Львів", w * 0.042f, h * 0.358f, strong = true)
            MapPlaceLabel("М-06 / E40", w * 0.565f, h * 0.333f, size = 10.sp, strong = true)
        }
        if (preview) {
            Image(
                painter = painterResource(if (dark) Res.drawable.map_origin_pin_dark else Res.drawable.map_origin_pin_light),
                contentDescription = "Початкова точка демо-маршруту",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(x = w * 0.863f, y = h * 0.202f)
                    .size(width = w * 0.035f, height = h * 0.019f)
            )
            Image(
                painter = painterResource(if (dark) Res.drawable.map_destination_pin_dark else Res.drawable.map_destination_pin_light),
                contentDescription = "Кінцева точка демо-маршруту",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(x = w * 0.089f, y = h * 0.387f)
                    .size(width = w * 0.045f, height = h * 0.024f)
            )
            if (showScaledMapLabels) {
                RouteDurationChip(
                    modifier = Modifier
                        .offset(x = w * 0.47f, y = h * 0.454f)
                )
            }
        }
    }
}
