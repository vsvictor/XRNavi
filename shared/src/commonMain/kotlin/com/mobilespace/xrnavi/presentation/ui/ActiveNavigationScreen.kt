package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.mobilespace.xrnavi.presentation.*

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.nav_active_route
import xrnavi.shared.generated.resources.nav_arrow_up
import xrnavi.shared.generated.resources.nav_lane_turn_right
import xrnavi.shared.generated.resources.nav_location_arrow
import xrnavi.shared.generated.resources.nav_location_halo
import xrnavi.shared.generated.resources.nav_local_streets
import xrnavi.shared.generated.resources.nav_locate
import xrnavi.shared.generated.resources.nav_road_casing
import xrnavi.shared.generated.resources.nav_roads
import xrnavi.shared.generated.resources.nav_route
import xrnavi.shared.generated.resources.nav_route_halo
import xrnavi.shared.generated.resources.nav_scan
import xrnavi.shared.generated.resources.nav_stop
import xrnavi.shared.generated.resources.nav_turn_left
import xrnavi.shared.generated.resources.nav_turn_right
import xrnavi.shared.generated.resources.nav_volume
import xrnavi.shared.generated.resources.nav_street_river
import xrnavi.shared.generated.resources.nav_alert

private val NavigationMap = Color(0xFF151C24)
private val NavigationRiverLabel = Color(0xFF527C91)
private val NavigationParkLabel = Color(0xFF6D9783)
private val NavigationLocation = Color(0xFFE64242)
private val NavigationRouteRed = Color(0xFFF45151)

@Composable
internal fun ActiveNavigationScreen(
    viewModel: ActiveNavigationViewModel,
    onStop: () -> Unit,
    onOverview: () -> Unit,
    onOpenXr: () -> Unit,
    onOpenNearbyStops: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NavigationEffects(viewModel) {
        when (it) {
            NavigationAction.StopPreview -> onStop()
            NavigationAction.Overview -> onOverview()
            NavigationAction.Xr -> onOpenXr()
            NavigationAction.NearbyStops -> onOpenNearbyStops()
            else -> Unit
        }
    }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            StreetNavigationMap(
                soundEnabled = state.soundEnabled,
                message = navigationNotice(state.notice),
                onToggleSound = viewModel::toggleSound,
                onRecenter = viewModel::recenter,
                onOpenXr = { viewModel.navigate(NavigationAction.Xr) },
                onDismissMessage = viewModel::dismissMessage,
            )
        }
        TripPanel(
            bottomInset = bottomInset,
            onStop = { viewModel.navigate(NavigationAction.StopPreview) },
            onOverview = { viewModel.navigate(NavigationAction.Overview) },
            onReport = viewModel::report,
            onOpenNearbyStops = { viewModel.navigate(NavigationAction.NearbyStops) },
        )
    }
}

@Composable
private fun StreetNavigationMap(
    soundEnabled: Boolean,
    message: String?,
    onToggleSound: () -> Unit,
    onRecenter: () -> Unit,
    onOpenXr: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(NavigationMap),
    ) {
        val sx = maxWidth / 402f
        val sy = maxHeight / 650f

        MapGraphic(Res.drawable.nav_street_river, 289, 0, 131, 620, sx, sy)
        NavigationPark(15, 155, 110, 48, sx, sy)
        NavigationPark(100, 370, 82, 85, sx, sy)
        NavigationPark(275, 40, 55, 85, sx, sy)
        NavigationPark(265, 470, 75, 65, sx, sy)
        MapGraphic(Res.drawable.nav_road_casing, 57, 125, 343.316f, 445f, sx, sy)
        MapGraphic(Res.drawable.nav_roads, 57, 125, 343.316f, 445f, sx, sy)
        MapGraphic(Res.drawable.nav_local_streets, 110, 75, 185, 445, sx, sy)
        MapGraphic(Res.drawable.nav_route_halo, 204, 24, 118, 622, sx, sy)
        MapGraphic(Res.drawable.nav_active_route, 207, 27, 111, 615, sx, sy)

        MapText(stringResource(Res.string.ui_pechersk), ApexSubtle, 12.sp, 28, 260, sx, sy)
        MapText(stringResource(Res.string.ui_vul_zhylyanska), ApexMuted, 10.sp, 218, 354, sx, sy)
        MapText(stringResource(Res.string.ui_vul_antonovycha), ApexMuted, 10.sp, 120, 148, sx, sy)
        MapText(stringResource(Res.string.ui_dnipro), NavigationRiverLabel, 11.sp, 343, 414, sx, sy)
        MapText(stringResource(Res.string.ui_botanichnyy_sad), NavigationParkLabel, 10.sp, 25, 180, sx, sy)

        Image(
            painter = painterResource(Res.drawable.nav_location_halo),
            contentDescription = null,
            modifier = Modifier
                .offset(sx * 180, sy * 465)
                .size(sx * 60, sy * 60),
        )
        Box(
            modifier = Modifier
                .offset(sx * 191, sy * 476)
                .size(sx * 38, sy * 38)
                .clip(CircleShape)
                .background(NavigationLocation)
                .border(3.dp, ApexText, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.nav_location_arrow),
                contentDescription = stringResource(Res.string.ui_potochne_mistseznakhodzhennya_demonstratsiyne),
                modifier = Modifier.size(sx * 22, sy * 22),
            )
        }

        ManeuverCard(sx, sy)
        LaneGuidance(sx, sy)
        FollowingManeuver(sx, sy)
        NavigationControls(
            sy = sy,
            soundEnabled = soundEnabled,
            onToggleSound = onToggleSound,
            onRecenter = onRecenter,
            onOpenXr = onOpenXr,
        )
        SpeedReadout(sx, sy)

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText("⊙", ApexText, 12.sp, weight = FontWeight.Bold)
            LabelText(stringResource(Res.string.ui_mapbox_brand_with_leading_space), ApexText, 10.sp, weight = FontWeight.SemiBold)
        }
        LabelText(
            stringResource(Res.string.ui_mapbox_openstreetmap),
            ApexMuted,
            8.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 7.dp),
        )
        if (message != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 28.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ApexSurface)
                    .clickable(onClick = onDismissMessage)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LabelText(message, ApexText, 11.sp, modifier = Modifier.weight(1f))
                LabelText("×", ApexMuted, 18.sp)
            }
        }
    }
}

@Composable
private fun MapGraphic(
    resource: DrawableResource,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    sx: Dp,
    sy: Dp,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(sx * x.toFloat(), sy * y.toFloat())
            .size(sx * width.toFloat(), sy * height.toFloat()),
    )
}

@Composable
private fun NavigationPark(x: Int, y: Int, width: Int, height: Int, sx: Dp, sy: Dp) {
    Box(
        modifier = Modifier
            .offset(sx * x, sy * y)
            .size(sx * width, sy * height)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E332F)),
    )
}

@Composable
private fun MapText(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    x: Int,
    y: Int,
    sx: Dp,
    sy: Dp,
) {
    LabelText(text, color, fontSize, modifier = Modifier.offset(sx * x, sy * y))
}

@Composable
private fun ManeuverCard(sx: Dp, sy: Dp) {
    Row(
        modifier = Modifier
            .offset(sx * 16, sy * 10)
            .size(width = sx * 370, height = sy * 112)
            .clip(RoundedCornerShape(16.dp))
            .background(ApexSurface)
            .border(1.dp, ApexBorder, RoundedCornerShape(16.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.nav_turn_right),
            contentDescription = stringResource(Res.string.ui_povorot_pravoruch),
            modifier = Modifier.size(sx * 52, sy * 52),
        )
        Spacer(Modifier.width(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LabelText(stringResource(Res.string.ui_350_m), ApexText, 44.sp, weight = FontWeight.Bold, condensed = true, lineHeight = 46.sp)
            LabelText(stringResource(Res.string.ui_pravoruch), ApexText, 16.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_na_vul_zhylyansku), ApexMuted, 12.sp)
        }
    }
}

@Composable
private fun LaneGuidance(sx: Dp, sy: Dp) {
    Row(
        modifier = Modifier
            .offset(sx * 53, sy * 132)
            .size(width = sx * 296, height = sy * 54)
            .clip(RoundedCornerShape(12.dp))
            .background(ApexSurface)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(
            stringResource(Res.string.ui_trymaytesya_pravoyi_smuhy),
            ApexMuted,
            10.sp,
            modifier = Modifier.width(sx * 104),
            lineHeight = 13.sp,
        )
        Image(
            painter = painterResource(Res.drawable.nav_arrow_up),
            contentDescription = null,
            modifier = Modifier.size(sx * 26, sy * 26),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Spacer(Modifier.width(sx * 13))
        Image(
            painter = painterResource(Res.drawable.nav_arrow_up),
            contentDescription = null,
            modifier = Modifier.size(sx * 26, sy * 26),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Spacer(Modifier.width(sx * 13))
        Box(
            modifier = Modifier
                .size(sx * 38, sy * 38)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF341B21)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.nav_lane_turn_right),
                contentDescription = stringResource(Res.string.ui_rekomendovana_prava_smuha),
                modifier = Modifier.size(sx * 26, sy * 26),
            )
        }
    }
}

@Composable
private fun FollowingManeuver(sx: Dp, sy: Dp) {
    Row(
        modifier = Modifier
            .offset(sx * 20, sy * 204)
            .clip(RoundedCornerShape(8.dp))
            .background(ApexBackground.copy(alpha = 0.88f))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(stringResource(Res.string.ui_dali), ApexMuted, 10.sp)
        Spacer(Modifier.width(7.dp))
        Image(
            painter = painterResource(Res.drawable.nav_turn_left),
            contentDescription = null,
            modifier = Modifier.size(sx * 14, sy * 14),
            colorFilter = ColorFilter.tint(ApexText),
        )
        Spacer(Modifier.width(7.dp))
        LabelText(stringResource(Res.string.ui_1_2_km), ApexText, 10.sp)
    }
}

@Composable
private fun BoxScope.NavigationControls(
    sy: Dp,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onRecenter: () -> Unit,
    onOpenXr: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(y = sy * 148)
            .padding(end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NavigationControl(
            Res.drawable.nav_volume,
            if (soundEnabled) ApexMuted else ApexSubtle,
            onToggleSound,
        )
        NavigationControl(Res.drawable.nav_locate, ApexMuted, onRecenter)
        NavigationControl(Res.drawable.nav_scan, NavigationRouteRed, onOpenXr)
    }
}

@Composable
private fun NavigationControl(
    icon: DrawableResource,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ApexSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(tint),
        )
    }
}

@Composable
private fun SpeedReadout(sx: Dp, sy: Dp) {
    Box(
        modifier = Modifier
            .offset(sx * 20, sy * 548)
            .size(width = sx * 77, height = sy * 76)
            .clip(RoundedCornerShape(12.dp))
            .background(ApexBackground.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LabelText(stringResource(Res.string.ui_42), ApexText, 34.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_km_hod), ApexMuted, 10.sp)
        }
    }
    Box(
        modifier = Modifier
            .offset(sx * 106, sy * 567)
            .size(sx * 52)
            .clip(CircleShape)
            .background(NavigationRouteRed)
            .padding(5.dp)
            .clip(CircleShape)
            .background(ApexText),
        contentAlignment = Alignment.Center,
    ) {
        LabelText(stringResource(Res.string.ui_50), ApexBackground, 18.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun TripPanel(
    bottomInset: Dp,
    onStop: () -> Unit,
    onOverview: () -> Unit,
    onReport: () -> Unit,
    onOpenNearbyStops: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp + maxOf(26.dp, bottomInset))
            .background(ApexSurface)
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TripMetric(stringResource(Res.string.ui_16_01), stringResource(Res.string.ui_prybuttya), NavigationRouteRed)
            TripMetric(stringResource(Res.string.ui_6_hod_18_khv), stringResource(Res.string.ui_zalyshylos), ApexText)
            TripMetric(stringResource(Res.string.ui_538_km), stringResource(Res.string.ui_vidstan), ApexText)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TripButton(
                modifier = Modifier.size(width = 48.dp, height = 44.dp),
                onClick = onStop,
            ) {
                Image(
                    painter = painterResource(Res.drawable.nav_stop),
                    contentDescription = stringResource(Res.string.ui_zupynyty_marshrut),
                    modifier = Modifier.size(21.dp),
                    colorFilter = ColorFilter.tint(ApexMuted),
                )
            }
            TripButton(
                modifier = Modifier.weight(1f).height(44.dp),
                onClick = onOverview,
            ) {
                Image(
                    painter = painterResource(Res.drawable.nav_route),
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    colorFilter = ColorFilter.tint(ApexMuted),
                )
                Spacer(Modifier.width(8.dp))
                LabelText(stringResource(Res.string.ui_ohlyad_marshrutu), ApexText, 12.sp, weight = FontWeight.Medium)
            }
            TripButton(
                modifier = Modifier.size(width = 48.dp, height = 44.dp),
                onClick = onReport,
            ) {
                Image(
                    painter = painterResource(Res.drawable.nav_alert),
                    contentDescription = stringResource(Res.string.ui_povidomyty_pro_podiyu_na_dorozi),
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFE9BA76)),
                )
            }
        }
        LabelText(
            stringResource(Res.string.ui_zupynky_na_marshruti), ApexMuted, 11.sp,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenNearbyStops),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(width = 132.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(ApexText),
            )
        }
    }
}

@Composable
private fun TripMetric(value: String, label: String, valueColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        LabelText(value, valueColor, 25.sp, weight = FontWeight.Bold, condensed = true)
        LabelText(label, ApexMuted, 10.sp)
    }
}

@Composable
private fun TripButton(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF20262E))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        content()
    }
}
