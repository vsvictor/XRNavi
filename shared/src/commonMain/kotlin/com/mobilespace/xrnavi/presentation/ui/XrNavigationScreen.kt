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
import androidx.compose.ui.graphics.Brush
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
import xrnavi.shared.generated.resources.nav_active_route
import xrnavi.shared.generated.resources.nav_location_arrow
import xrnavi.shared.generated.resources.nav_location_halo
import xrnavi.shared.generated.resources.nav_local_streets
import xrnavi.shared.generated.resources.nav_road_casing
import xrnavi.shared.generated.resources.nav_roads
import xrnavi.shared.generated.resources.nav_route_halo
import xrnavi.shared.generated.resources.nav_street_river
import xrnavi.shared.generated.resources.xr_camera
import xrnavi.shared.generated.resources.xr_chevrons
import xrnavi.shared.generated.resources.xr_map
import xrnavi.shared.generated.resources.xr_road_camera
import xrnavi.shared.generated.resources.xr_route_corridor
import xrnavi.shared.generated.resources.xr_route_edges
import xrnavi.shared.generated.resources.xr_scan
import xrnavi.shared.generated.resources.xr_turn_marker
import xrnavi.shared.generated.resources.xr_turn_right
import xrnavi.shared.generated.resources.xr_volume

private val XrOverlay = Color(0xED11171F)
private val XrRed = Color(0xFFE64242)
private val XrRedDark = Color(0xFFF45151)

@Composable
internal fun XrNavigationScreen(viewModel: XrNavigationViewModel, onBackToMap: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NavigationEffects(viewModel) { if (it == NavigationAction.Map) onBackToMap() }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        XrHeader()
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            XrCameraView(
                soundEnabled = state.soundEnabled,
                showMessage = state.showMessage,
                onToggleSound = viewModel::toggleSound,
                onDismissMessage = viewModel::dismissMessage,
            )
        }
        XrTripPanel(
            onBackToMap = { viewModel.navigate(NavigationAction.Map) },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxOf(bottomInset, 26.dp)),
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

@Composable
private fun XrHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(ApexBackground)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.xr_scan),
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                colorFilter = ColorFilter.tint(XrRedDark),
            )
            Spacer(Modifier.width(9.dp))
            LabelText(
                stringResource(Res.string.ui_xr_navihatsiya),
                ApexText,
                24.sp,
                weight = FontWeight.Bold,
                condensed = true,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF20262E))
                .padding(horizontal = 9.dp, vertical = 6.dp),
        ) {
            LabelText(stringResource(Res.string.ui_kontsept), ApexMuted, 10.sp, weight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun XrCameraView(
    soundEnabled: Boolean,
    showMessage: Boolean,
    onToggleSound: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF19232A)),
    ) {
        val sx = maxWidth / 402f
        val sy = maxHeight / 548f
        Image(
            painter = painterResource(Res.drawable.xr_road_camera),
            contentDescription = stringResource(Res.string.ui_statychne_zobrazhennya_dorohy_dlya_demonstratsiyi_xr_navihatsiyi),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to ApexBackground.copy(alpha = 0.4f),
                        0.45f to Color.Transparent,
                        1f to ApexBackground.copy(alpha = 0.8f),
                    ),
                ),
        )
        XrGraphic(Res.drawable.xr_route_corridor, 101, 252, 208, 256, sx, sy)
        XrGraphic(Res.drawable.xr_route_edges, 99, 250, 210, 258, sx, sy)
        XrGraphic(Res.drawable.xr_chevrons, 152, 260, 108, 194, sx, sy)

        XrManeuverCard(sx, sy)
        WorldTurnMarker(sx, sy)
        CameraInfoBadge(sx, sy)
        XrSpeedReadout(sx, sy)
        XrSoundControl(
            sx = sx,
            sy = sy,
            soundEnabled = soundEnabled,
            onClick = onToggleSound,
        )
        XrAttribution(sx, sy)
        LabelText(
            stringResource(Res.string.ui_xr_rezhym_uvimkneno_lokalno_kameru_ta_xr_servis_ne_pidklyucheno),
            ApexMuted, 10.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 12.dp, end = 12.dp)
                .clip(RoundedCornerShape(8.dp)).background(XrOverlay).padding(8.dp),
        )
        if (showMessage) {
            LabelText(
                if (soundEnabled) stringResource(Res.string.ui_zvuk_navihatsiyi_uvimkneno) else stringResource(Res.string.ui_zvuk_navihatsiyi_vymkneno),
                ApexText,
                11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(XrOverlay)
                    .clickable(onClick = onDismissMessage)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun XrGraphic(
    resource: DrawableResource,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    sx: Dp,
    sy: Dp,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(sx * x, sy * y)
            .size(sx * width, sy * height),
    )
}

@Composable
private fun BoxScope.XrManeuverCard(sx: Dp, sy: Dp) {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(sx * 16, sy * 14)
            .size(width = sx * 370, height = sy * 99)
            .clip(RoundedCornerShape(16.dp))
            .background(XrOverlay)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.xr_turn_right),
            contentDescription = stringResource(Res.string.ui_povorot_pravoruch),
            modifier = Modifier.size(sx * 44, sy * 44),
        )
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(
                stringResource(Res.string.ui_350_m_pravoruch),
                ApexText,
                34.sp,
                weight = FontWeight.Bold,
                condensed = true,
                lineHeight = 36.sp,
            )
            LabelText(stringResource(Res.string.ui_vul_zhylyanska), ApexMuted, 13.sp)
        }
    }
}

@Composable
private fun BoxScope.WorldTurnMarker(sx: Dp, sy: Dp) {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(sx * 248, sy * 205)
            .clip(RoundedCornerShape(8.dp))
            .background(XrRed)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.xr_turn_marker),
            contentDescription = null,
            modifier = Modifier.size(sx * 19, sy * 19),
        )
        Spacer(Modifier.width(7.dp))
        LabelText(stringResource(Res.string.ui_350_m), ApexText, 14.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun BoxScope.CameraInfoBadge(sx: Dp, sy: Dp) {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(sx * 18, sy * 132)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xD911171F))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.xr_camera),
            contentDescription = null,
            modifier = Modifier.size(sx * 12, sy * 12),
            colorFilter = ColorFilter.tint(Color(0xFF85C6AC)),
        )
        LabelText(stringResource(Res.string.ui_vizualizatsiya_kamery_ta_marshrutu), ApexMuted, 9.sp)
    }
}

@Composable
private fun BoxScope.XrSpeedReadout(sx: Dp, sy: Dp) {
    Column(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(sx * 20, sy * 455)
            .size(width = sx * 70, height = sy * 62)
            .clip(RoundedCornerShape(12.dp))
            .background(XrOverlay),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LabelText(stringResource(Res.string.ui_42), ApexText, 30.sp, weight = FontWeight.Bold, condensed = true)
        LabelText(stringResource(Res.string.ui_km_hod), ApexMuted, 9.sp)
    }
}

@Composable
private fun BoxScope.XrSoundControl(
    sx: Dp,
    sy: Dp,
    soundEnabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(sx * 338, sy * 469)
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(XrOverlay)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.xr_volume),
            contentDescription = if (soundEnabled) stringResource(Res.string.ui_vymknuty_zvuk) else stringResource(Res.string.ui_uvimknuty_zvuk),
            modifier = Modifier.size(sx * 21, sy * 21),
            colorFilter = ColorFilter.tint(if (soundEnabled) ApexText else ApexSubtle),
        )
    }
}

@Composable
private fun BoxScope.XrAttribution(sx: Dp, sy: Dp) {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(sx * 12, sy * 526)
            .size(width = sx * 378, height = sy * 16),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(stringResource(Res.string.ui_mapbox_brand_badge), ApexText, 10.sp, weight = FontWeight.SemiBold)
        LabelText(stringResource(Res.string.ui_mapbox_openstreetmap), ApexMuted, 8.sp)
    }
}

@Composable
private fun XrTripPanel(onBackToMap: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(ApexSurface)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            XrMiniMap()
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelText(
                        stringResource(Res.string.ui_16_01),
                        XrRedDark,
                        27.sp,
                        weight = FontWeight.Bold,
                        condensed = true,
                    )
                    Spacer(Modifier.width(10.dp))
                    LabelText(stringResource(Res.string.ui_prybuttya_do_lvova), ApexMuted, 11.sp)
                }
                LabelText(stringResource(Res.string.ui_6_hod_18_khv_538_km), ApexText, 12.sp)
                LabelText(stringResource(Res.string.ui_ford_mustang_gt), ApexSubtle, 10.sp)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF20262E))
                .clickable(onClick = onBackToMap),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.xr_map),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                colorFilter = ColorFilter.tint(ApexText),
            )
            Spacer(Modifier.width(8.dp))
            LabelText(stringResource(Res.string.ui_povernutysya_do_mapy), ApexText, 12.sp, weight = FontWeight.SemiBold)
        }
        LabelText(
            stringResource(Res.string.ui_telefon_u_trymachi_stezhte_za_dorohoyu_a_ne_za_ekranom),
            ApexMuted,
            10.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun XrMiniMap() {
    BoxWithConstraints(
        modifier = Modifier
            .size(width = 92.dp, height = 74.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF151C24)),
    ) {
        val sx = maxWidth / 402f
        val sy = maxHeight / 650f
        XrGraphic(Res.drawable.nav_street_river, 289, 0, 131, 620, sx, sy)
        XrGraphic(Res.drawable.nav_road_casing, 57, 125, 343, 445, sx, sy)
        XrGraphic(Res.drawable.nav_roads, 57, 125, 343, 445, sx, sy)
        XrGraphic(Res.drawable.nav_local_streets, 110, 75, 185, 445, sx, sy)
        XrGraphic(Res.drawable.nav_route_halo, 204, 24, 118, 622, sx, sy)
        XrGraphic(Res.drawable.nav_active_route, 207, 27, 111, 615, sx, sy)
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
                .background(XrRed)
                .border(2.dp, ApexText, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.nav_location_arrow),
                contentDescription = null,
                modifier = Modifier.size(sx * 22, sy * 22),
            )
        }
    }
}
