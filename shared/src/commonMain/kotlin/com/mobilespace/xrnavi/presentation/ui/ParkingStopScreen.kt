package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.mobilespace.xrnavi.presentation.*

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.stop_alert
import xrnavi.shared.generated.resources.stop_back
import xrnavi.shared.generated.resources.stop_chevron
import xrnavi.shared.generated.resources.stop_help
import xrnavi.shared.generated.resources.stop_parking
import xrnavi.shared.generated.resources.stop_phone
import xrnavi.shared.generated.resources.stop_plus
import xrnavi.shared.generated.resources.stop_position
import xrnavi.shared.generated.resources.stop_river
import xrnavi.shared.generated.resources.stop_road_casing
import xrnavi.shared.generated.resources.stop_road_network
import xrnavi.shared.generated.resources.stop_route
import xrnavi.shared.generated.resources.stop_route_halo

private val StopMapBackground = Color(0xFF151C24)
private val StopCard = Color(0xFF14191F)
private val StopMetric = Color(0xFF20262E)
private val StopPark = Color(0xFF1E332F)
private val StopGold = Color(0xFFE9BA76)

@Composable
internal fun ParkingStopScreen(viewModel: ParkingStopViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message = navigationNotice(state.notice)
    NavigationEffects(viewModel) { if (it == NavigationAction.Back) onBack() }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        ParkingStopHeader(
            onBack = { viewModel.navigate(NavigationAction.Back) },
            onHelp = viewModel::help,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            message?.let { LabelText(it, ApexMuted, 11.sp, lineHeight = 15.sp) }
            ParkingStopMap()
            ParkingStopIdentity()
            LabelText(stringResource(Res.string.ui_dlya_man_tgx_16_5_m), ApexMuted, 11.sp, weight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StopMetricCard(stringResource(Res.string.ui_v_yizd_za_opysom), stringResource(Res.string.ui_4_5_m), Modifier.weight(1f))
                StopMetricCard(stringResource(Res.string.ui_dovzhyna_mistsya), stringResource(Res.string.ui_20_m), Modifier.weight(1f))
                StopMetricCard(stringResource(Res.string.ui_mists_zahalom), stringResource(Res.string.ui_32), Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(stringResource(Res.string.ui_okhorona), stringResource(Res.string.ui_dush), stringResource(Res.string.ui_tualet), stringResource(Res.string.ui_kafe), stringResource(Res.string.ui_dyzel_poruch)).forEach { facility ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StopMetric)
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                    ) {
                        LabelText(facility, ApexMuted, 10.sp, weight = FontWeight.SemiBold)
                    }
                }
            }
            ParkingStopContact(
                onClick = viewModel::call,
            )
            ParkingStopWarning()
            LabelText(
                stringResource(Res.string.ui_oriyentovnyy_ob_yizd_6_khv_zupynka_ne_pidtverdzhuye_bezpechnist),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexRed)
                    .clickable(onClick = viewModel::addStop),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.stop_plus),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    colorFilter = ColorFilter.tint(ApexText),
                )
                LabelText(stringResource(Res.string.ui_dodaty_do_marshrutu), ApexText, 14.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ParkingStopHeader(onBack: () -> Unit, onHelp: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.stop_back),
            contentDescription = stringResource(Res.string.ui_nazad),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onBack),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            LabelText(stringResource(Res.string.ui_stoyanka_m_06_2), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_rivne_pryklad_ob_yekta_1_2_km_vid_marshrutu), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.stop_help),
            contentDescription = stringResource(Res.string.ui_dopomoha_shchodo_stoyanky),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onHelp),
        )
    }
}

@Composable
private fun ParkingStopMap() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(StopMapBackground),
    ) {
        val sx = maxWidth / 362f
        val sy = maxHeight / 174f
        StopMapGraphic(Res.drawable.stop_river, 292f, 0f, 70f, 214f, sx, sy)
        StopMapPark(28f, 43.5f, 83f, 60f, sx, sy)
        StopMapPark(152f, 120.06f, 60f, 50f, sx, sy)
        StopMapGraphic(Res.drawable.stop_road_casing, 71.4f, 0f, 233.3f, 99.2f, sx, sy)
        StopMapGraphic(Res.drawable.stop_road_network, 71.4f, 0f, 233.3f, 99.2f, sx, sy)
        StopMapGraphic(Res.drawable.stop_route_halo, 40f, 48.72f, 282f, 83.52f, sx, sy)
        StopMapGraphic(Res.drawable.stop_route, 40f, 48.72f, 282f, 83.52f, sx, sy)
        StopMapLabel(stringResource(Res.string.ui_lviv), 16f, 121.8f, sx, sy, ApexText, 13.sp, FontWeight.SemiBold)
        StopMapLabel(stringResource(Res.string.ui_kyyiv), 290f, 34.8f, sx, sy, ApexMuted, 12.sp)
        StopMapLabel(stringResource(Res.string.ui_rivne_m_06), 145f, 97.44f, sx, sy, ApexMuted, 10.sp)
        StopMapGraphic(Res.drawable.stop_position, 220f, 80.04f, 26f, 26f, sx, sy)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(stringResource(Res.string.ui_mapbox_brand_badge), ApexText, 10.sp, weight = FontWeight.Bold)
            LabelText(stringResource(Res.string.ui_mapbox_openstreetmap), ApexMuted, 7.sp)
        }
        Box(
            modifier = Modifier
                .offset(x = sx * 155f, y = sy * 74f)
                .size(sx * 40f, sy * 40f)
                .clip(RoundedCornerShape(10.dp))
                .background(ApexRed),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.stop_parking),
                contentDescription = stringResource(Res.string.ui_poznachka_stoyanky),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun StopMapGraphic(
    resource: DrawableResource,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    sx: Dp,
    sy: Dp,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(x = sx * x, y = sy * y)
            .size(width = sx * width, height = sy * height),
    )
}

@Composable
private fun StopMapPark(x: Float, y: Float, width: Float, height: Float, sx: Dp, sy: Dp) {
    Box(
        modifier = Modifier
            .offset(x = sx * x, y = sy * y)
            .size(width = sx * width, height = sy * height)
            .clip(RoundedCornerShape(18.dp))
            .background(StopPark),
    )
}

@Composable
private fun StopMapLabel(
    text: String,
    x: Float,
    y: Float,
    sx: Dp,
    sy: Dp,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    LabelText(
        text,
        color,
        size,
        modifier = Modifier.offset(x = sx * x, y = sy * y),
        weight = weight,
    )
}

@Composable
private fun ParkingStopIdentity() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StopCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            LabelText(
                stringResource(Res.string.ui_stoyanka_dlya_avtopoyizdiv),
                ApexText,
                24.sp,
                modifier = Modifier.weight(1f),
                weight = FontWeight.SemiBold,
                condensed = true,
                lineHeight = 32.sp,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StopMetric)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
            ) {
                LabelText(stringResource(Res.string.ui_24_7), StopGreen, 10.sp, weight = FontWeight.SemiBold)
            }
        }
        LabelText(stringResource(Res.string.ui_m_06_pid_yizd_iz_napryamku_kyyiv_lviv), ApexMuted, 11.sp)
        LabelText(stringResource(Res.string.ui_prydatnist_poperednya_v_yizd_utochnyty), StopGold, 11.sp)
    }
}

@Composable
private fun StopMetricCard(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StopMetric)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LabelText(label, ApexMuted, 10.sp, lineHeight = 12.sp)
        LabelText(value, ApexText, 24.sp, weight = FontWeight.SemiBold, condensed = true)
    }
}

@Composable
private fun ParkingStopContact(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StopCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.stop_phone),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(stringResource(Res.string.ui_cherhovyy_stoyanky), ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_380_67_410_22_20_utochnyty_v_yizd), ApexMuted, 10.sp)
        }
        Image(
            painter = painterResource(Res.drawable.stop_chevron),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
private fun ParkingStopWarning() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF30291F))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.stop_alert),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_nemaye_danykh_pro_vilni_mistsya), StopGold, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_opys_onovleno_03_10_2026_o_18_10_vilni_mistsya_taryfy_y_dopustym),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

private val StopGreen = Color(0xFF85C6AC)
