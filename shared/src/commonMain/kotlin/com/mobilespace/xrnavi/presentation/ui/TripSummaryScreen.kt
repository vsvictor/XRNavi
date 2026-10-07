package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.mobilespace.xrnavi.presentation.*

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.nav_car
import xrnavi.shared.generated.resources.nav_map
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.nav_settings
import xrnavi.shared.generated.resources.trip_arrival
import xrnavi.shared.generated.resources.trip_arrow_right
import xrnavi.shared.generated.resources.trip_badge
import xrnavi.shared.generated.resources.trip_back
import xrnavi.shared.generated.resources.trip_completed_route
import xrnavi.shared.generated.resources.trip_origin
import xrnavi.shared.generated.resources.trip_river
import xrnavi.shared.generated.resources.trip_road_casing
import xrnavi.shared.generated.resources.trip_road_network
import xrnavi.shared.generated.resources.trip_route_halo
import xrnavi.shared.generated.resources.trip_share
import xrnavi.shared.generated.resources.trip_shield
import xrnavi.shared.generated.resources.mustang_thumbnail

private val TripMapBackground = Color(0xFF151C24)
private val TripGreen = Color(0xFF1E332F)
private val TripCard = Color(0xFF14191F)
private val TripMetricCard = Color(0xFF20262E)
private val TripGreenText = Color(0xFF85C6AC)
private val TripGold = Color(0xFFE9BA76)

@Composable
internal fun TripSummaryScreen(
    viewModel: TripSummaryViewModel,
    onBack: () -> Unit,
    onReturnToMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTripHistory: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val statusMessage = navigationNotice(state.notice)
    NavigationEffects(viewModel) {
        when (it) {
            NavigationAction.Back -> onBack()
            NavigationAction.Map -> onReturnToMap()
            NavigationAction.Garage -> onOpenGarage()
            NavigationAction.Preferences -> onOpenPreferences()
            NavigationAction.Profile -> onOpenProfile()
            NavigationAction.TripHistory -> onOpenTripHistory()
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
        TripSummaryHeader(
            onBack = { viewModel.navigate(NavigationAction.Back) },
            onShare = viewModel::share,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TripMetricCard)
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.trip_badge),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )
                    LabelText(stringResource(Res.string.ui_demonstratsiynyy_pidsumok_poyizdky), TripGreenText, 10.sp, weight = FontWeight.SemiBold)
                }
                LabelText(stringResource(Res.string.ui_6_zhovtnya_2026), ApexMuted, 10.sp)
            }
            statusMessage?.let {
                LabelText(it, ApexMuted, 11.sp, lineHeight = 15.sp)
            }
            CompletedRouteMap()
            JourneyMetrics()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActivityMetric(
                    value = stringResource(Res.string.ui_5_hod_54_khv),
                    label = stringResource(Res.string.ui_u_rusi),
                    modifier = Modifier.weight(1f),
                )
                ActivityMetric(
                    value = stringResource(Res.string.ui_18_khv),
                    label = stringResource(Res.string.ui_zupynky_1),
                    modifier = Modifier.weight(1f),
                )
            }
            TripVehicleCard()
            TripHistoryNotice { viewModel.navigate(NavigationAction.TripHistory) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexRed)
                    .clickable { viewModel.navigate(NavigationAction.Map) },
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.nav_map),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    colorFilter = ColorFilter.tint(ApexText),
                )
                LabelText(stringResource(Res.string.ui_povernutysya_do_mapy), ApexText, 15.sp, weight = FontWeight.SemiBold)
            }
        }
        TripSummaryBottomNavigation(
            bottomInset = bottomInset,
            onOpenMap = { viewModel.navigate(NavigationAction.Map) },
            onOpenGarage = { viewModel.navigate(NavigationAction.Garage) },
            onOpenPreferences = { viewModel.navigate(NavigationAction.Preferences) },
            onOpenProfile = { viewModel.navigate(NavigationAction.Profile) },
        )
    }
}

@Composable
private fun TripSummaryHeader(onBack: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart,
        ) {
            Image(
                painter = painterResource(Res.drawable.trip_back),
                contentDescription = stringResource(Res.string.ui_nazad),
                modifier = Modifier.size(22.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            LabelText(stringResource(Res.string.ui_vy_na_mistsi), ApexText, 26.sp, weight = FontWeight.Bold, condensed = true)
            LabelText(stringResource(Res.string.ui_lviv_ploshcha_rynok_1), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.trip_share),
            contentDescription = stringResource(Res.string.ui_podilytysya_marshrutom),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onShare),
        )
    }
}

@Composable
private fun CompletedRouteMap() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TripMapBackground),
    ) {
        val scaleX = maxWidth.value / 362f
        val scaleY = maxHeight.value / 200f
        MapImage(
            Res.drawable.trip_river,
            x = 302f,
            y = 0f,
            width = 66f,
            height = 340f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        Box(
            modifier = Modifier
                .offset(x = 40.dp * scaleX, y = 22.dp * scaleY)
                .size(width = 84.dp * scaleX, height = 46.dp * scaleY)
                .clip(RoundedCornerShape(18.dp))
                .background(TripGreen),
        )
        MapImage(
            Res.drawable.trip_road_casing,
            x = 81.5f,
            y = -15f,
            width = 227.413f,
            height = 185.156f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        MapImage(
            Res.drawable.trip_road_network,
            x = 81.5f,
            y = -15f,
            width = 227.413f,
            height = 185.156f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        MapImage(
            Res.drawable.trip_route_halo,
            x = 37f,
            y = 42f,
            width = 281f,
            height = 128f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        MapImage(
            Res.drawable.trip_completed_route,
            x = 37f,
            y = 42f,
            width = 281f,
            height = 128f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        MapImage(
            Res.drawable.trip_origin,
            x = 312f,
            y = 36f,
            width = 12f,
            height = 12f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        MapImage(
            Res.drawable.trip_arrival,
            x = 29f,
            y = 162f,
            width = 16f,
            height = 16f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        MapLabel(stringResource(Res.string.ui_kyyiv), x = 280f, y = 17f, scaleX = scaleX, scaleY = scaleY, color = ApexText, fontSize = 13.sp, weight = FontWeight.Bold)
        MapLabel(stringResource(Res.string.ui_lviv), x = 19f, y = 141f, scaleX = scaleX, scaleY = scaleY, color = ApexText, fontSize = 13.sp, weight = FontWeight.Bold)
        MapLabel(stringResource(Res.string.ui_zhytomyr), x = 193f, y = 92f, scaleX = scaleX, scaleY = scaleY, color = ApexMuted, fontSize = 10.sp)
        MapLabel(stringResource(Res.string.ui_rivne), x = 106f, y = 127f, scaleX = scaleX, scaleY = scaleY, color = ApexMuted, fontSize = 10.sp)
        MapLabel(stringResource(Res.string.ui_m_06_e40_2), x = 207f, y = 150f, scaleX = scaleX, scaleY = scaleY, color = TripGold, fontSize = 9.sp)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp * scaleX, vertical = 4.dp * scaleY),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(stringResource(Res.string.ui_mapbox_brand_badge), ApexText, 10.sp, weight = FontWeight.Bold)
            LabelText(stringResource(Res.string.ui_mapbox_openstreetmap), ApexMuted, 7.sp)
        }
    }
}

@Composable
private fun MapImage(
    resource: DrawableResource,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    scaleX: Float,
    scaleY: Float,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(x = x.dp * scaleX, y = y.dp * scaleY)
            .size(width = width.dp * scaleX, height = height.dp * scaleY),
    )
}

@Composable
private fun MapLabel(
    text: String,
    x: Float,
    y: Float,
    scaleX: Float,
    scaleY: Float,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    LabelText(
        text = text,
        color = color,
        fontSize = fontSize,
        weight = weight,
        modifier = Modifier.offset(x = x.dp * scaleX, y = y.dp * scaleY),
    )
}

@Composable
private fun JourneyMetrics() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TripCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LabelText(stringResource(Res.string.ui_540_km), ApexText, 28.sp, weight = FontWeight.Bold, condensed = true)
                LabelText(stringResource(Res.string.ui_vidstan), ApexMuted, 10.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LabelText(stringResource(Res.string.ui_6_hod_12_khv), ApexText, 28.sp, weight = FontWeight.Bold, condensed = true)
                LabelText(stringResource(Res.string.ui_zahalnyy_chas), ApexMuted, 10.sp)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(ApexBorder))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(stringResource(Res.string.ui_kyyiv_09_41), ApexMuted, 11.sp, modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(Res.drawable.trip_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            LabelText(stringResource(Res.string.ui_lviv_15_53), ApexText, 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun ActivityMetric(value: String, label: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TripMetricCard)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LabelText(value, ApexText, 21.sp, weight = FontWeight.Bold, condensed = true)
        LabelText(label, ApexMuted, 10.sp)
    }
}

@Composable
private fun TripVehicleCard() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.mustang_thumbnail),
            contentDescription = stringResource(Res.string.ui_ford_mustang_gt),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 78.dp, height = 54.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_ford_mustang_gt), ApexText, 21.sp, weight = FontWeight.Bold, condensed = true)
            LabelText(stringResource(Res.string.ui_aa_1964_mt_osobystyy_rezhym), ApexMuted, 10.sp)
        }
    }
}

@Composable
private fun TripHistoryNotice(onOpenTripHistory: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TripCard)
            .clickable(onClick = onOpenTripHistory)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.trip_shield),
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            colorFilter = ColorFilter.tint(TripGreenText),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_demonstratsiynyy_pidsumok_poyizdky), ApexText, 11.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_dani_marshrutu_navedeni_dlya_prykladu_zberezhennya_istoriyi_ta_p),
                ApexMuted,
                10.sp,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun TripSummaryBottomNavigation(
    bottomInset: Dp,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val tabs = listOf(
        Res.drawable.nav_map to stringResource(Res.string.ui_mapa),
        Res.drawable.nav_car to stringResource(Res.string.ui_harazh),
        Res.drawable.nav_settings to stringResource(Res.string.ui_nalashtuvannya),
        Res.drawable.nav_profile to stringResource(Res.string.ui_profil),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TripCard)
            .border(BorderStroke(0.5.dp, ApexBorder)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(61.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            when (index) {
                                0 -> onOpenMap()
                                1 -> onOpenGarage()
                                2 -> onOpenPreferences()
                                else -> onOpenProfile()
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Image(
                        painter = painterResource(tab.first),
                        contentDescription = tab.second,
                        modifier = Modifier.size(21.dp),
                        colorFilter = ColorFilter.tint(if (index == 0) ApexRed else ApexMuted),
                    )
                    LabelText(
                        tab.second,
                        if (index == 0) ApexText else ApexMuted,
                        9.sp,
                        weight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxOf(26.dp, bottomInset)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 132.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(ApexText),
            )
        }
    }
}
