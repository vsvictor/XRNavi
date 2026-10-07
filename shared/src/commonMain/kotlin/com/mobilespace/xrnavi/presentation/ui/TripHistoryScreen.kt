package com.mobilespace.xrnavi.presentation.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobilespace.xrnavi.domain.TripHistoryAction
import com.mobilespace.xrnavi.presentation.TripHistoryViewModel
import com.mobilespace.xrnavi.presentation.FeatureDestination
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.history_lock
import xrnavi.shared.generated.resources.history_position
import xrnavi.shared.generated.resources.history_river
import xrnavi.shared.generated.resources.history_road_casing
import xrnavi.shared.generated.resources.history_road_network
import xrnavi.shared.generated.resources.history_route
import xrnavi.shared.generated.resources.history_route_halo
import xrnavi.shared.generated.resources.history_tags
import xrnavi.shared.generated.resources.offline_back
import xrnavi.shared.generated.resources.offline_bottom_car
import xrnavi.shared.generated.resources.offline_bottom_map
import xrnavi.shared.generated.resources.offline_bottom_profile
import xrnavi.shared.generated.resources.offline_bottom_settings
import xrnavi.shared.generated.resources.offline_chevron
import xrnavi.shared.generated.resources.offline_help

private val HistoryCard = Color(0xFF14191F)
private val HistoryMapBackground = Color(0xFF151C24)
private val HistoryMapGreen = Color(0xFF1E332F)
private val HistoryBadgeBackground = Color(0xFF20262E)
private val HistoryWorkBackground = Color(0xFF30291F)
private val HistoryGreen = Color(0xFF85C6AC)
private val HistoryGold = Color(0xFFE9BA76)

@Composable
internal fun TripHistoryScreen(
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: TripHistoryViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onBack, onOpenMap, onOpenGarage, onOpenSettings, onOpenProfile) {
        viewModel.effects.collect {
            when (it.destination) {
                FeatureDestination.Back -> onBack()
                FeatureDestination.Map -> onOpenMap()
                FeatureDestination.Garage -> onOpenGarage()
                FeatureDestination.Settings -> onOpenSettings()
                FeatureDestination.Profile -> onOpenProfile()
                else -> Unit
            }
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
        TripHistoryHeader(
            onBack = { viewModel.navigate(FeatureDestination.Back) },
            onHelp = viewModel::toggleHelp,
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
            TripHistoryFilter(
                selected = state.selectedFilter,
                onSelect = viewModel::selectFilter,
            )
            if (state.showWorkTrips) ActiveWorkTripNotice()
            if (state.showHelp) HistoryDemonstrationNote()
            if (state.showPersonalTrips) {
                HistorySectionHeading(stringResource(Res.string.ui_osobysta_poyizdka), stringResource(Res.string.ui_6_zhovtnya))
                PersonalHistoryTrip(onClick = { viewModel.perform(TripHistoryAction.OpenPersonalTrip) })
            }
            if (state.showWorkTrips) {
                HistorySectionHeading(stringResource(Res.string.ui_robochi_poyizdky), stringResource(Res.string.ui_5_zhovtnya))
                WorkHistoryTrip(onClick = { viewModel.perform(TripHistoryAction.OpenWorkTrip) })
            }
            TripClassificationRow(
                enabled = !state.isSubmitting,
                onClick = { viewModel.perform(TripHistoryAction.OpenClassification) },
            )
            state.statusMessage?.let {
                LabelText(stringResource(it), HistoryGold, 10.sp, lineHeight = 14.sp)
            }
            LabelText(
                stringResource(Res.string.ui_osobysti_marshruty_ne_bachat_ni_dyspetcher_ni_administrator_vypr),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
        TripHistoryBottomNavigation(
            bottomInset = bottomInset,
            onOpenMap = { viewModel.navigate(FeatureDestination.Map) },
            onOpenGarage = { viewModel.navigate(FeatureDestination.Garage) },
            onOpenSettings = { viewModel.navigate(FeatureDestination.Settings) },
            onOpenProfile = { viewModel.navigate(FeatureDestination.Profile) },
        )
    }
}

@Composable
private fun TripHistoryHeader(onBack: () -> Unit, onHelp: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.offline_back),
            contentDescription = stringResource(Res.string.ui_nazad),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onBack),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            LabelText(stringResource(Res.string.ui_istoriya_poyizdok), ApexText, 26.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_andriy_koval_7_zhovtnya_2026), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.offline_help),
            contentDescription = stringResource(Res.string.ui_pro_istoriyu_poyizdok),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onHelp),
        )
    }
}

@Composable
private fun TripHistoryFilter(selected: Int, onSelect: (Int) -> Unit) {
    val filters = listOf(stringResource(Res.string.ui_usi), stringResource(Res.string.ui_osobysti_2), stringResource(Res.string.ui_robochi))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HistoryCard)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        filters.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(35.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected == index) Color(0xFF341B21) else HistoryCard)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                LabelText(
                    label,
                    if (selected == index) ApexLinkRed else ApexMuted,
                    11.sp,
                    weight = if (selected == index) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ActiveWorkTripNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HistoryCard)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        RadioWavesIcon()
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_zaraz_robochyy_reys_dl_204), HistoryGreen, 12.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_peredacha_pozytsiyi_uvimknena_politykoyu_aktyvnyy_reys_ne_mozhna),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun RadioWavesIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        val iconColor = HistoryGreen
        drawCircle(
            color = iconColor,
            radius = size.minDimension * 0.11f,
            center = center,
        )
        drawArc(
            color = iconColor,
            startAngle = -55f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.22f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.56f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            color = iconColor,
            startAngle = 125f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.22f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.56f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            color = iconColor,
            startAngle = -55f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.04f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.92f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            color = iconColor,
            startAngle = 125f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.04f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.92f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun HistorySectionHeading(title: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(title, ApexMuted, 11.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        LabelText(date, ApexSubtle, 10.sp)
    }
}

@Composable
private fun PersonalHistoryTrip(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HistoryCard)
            .border(BorderStroke(1.dp, HistoryCard), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(
                stringResource(Res.string.ui_kyyiv_lviv),
                ApexText,
                23.sp,
                weight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                condensed = true,
                modifier = Modifier.weight(1f),
            )
            HistoryBadge(stringResource(Res.string.ui_osobysta), ApexMuted, HistoryBadgeBackground)
        }
        LabelText(stringResource(Res.string.ui_ford_mustang_gt_aa_1964_mt), ApexMuted, 11.sp)
        PersonalTripMap()
        LabelText(stringResource(Res.string.ui_09_41_15_53_540_km_6_hod_12_khv), ApexText, 12.sp)
        LabelText(stringResource(Res.string.ui_lyshe_vy_heolokatsiya_kompaniyi_ne_peredavalasya), HistoryGreen, 10.sp)
    }
}

@Composable
private fun PersonalTripMap() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(HistoryMapBackground),
    ) {
        val scaleX = maxWidth.value / 362f
        val scaleY = maxHeight.value / 126f
        HistoryMapImage(
            Res.drawable.history_river,
            x = 292f,
            y = 0f,
            width = 70f,
            height = 166f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        Box(
            modifier = Modifier
                .offset(x = 28.dp * scaleX, y = 31.5.dp * scaleY)
                .size(width = 83.dp * scaleX, height = 60.dp * scaleY)
                .clip(RoundedCornerShape(18.dp))
                .background(HistoryMapGreen),
        )
        Box(
            modifier = Modifier
                .offset(x = 152.dp * scaleX, y = 86.dp * scaleY)
                .size(width = 60.dp * scaleX, height = 50.dp * scaleY)
                .clip(RoundedCornerShape(18.dp))
                .background(HistoryMapGreen),
        )
        HistoryMapImage(
            Res.drawable.history_road_casing,
            x = 0f,
            y = -20f,
            width = 400f,
            height = 166f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        HistoryMapImage(
            Res.drawable.history_road_network,
            x = 0f,
            y = -20f,
            width = 400f,
            height = 166f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        HistoryMapImage(
            Res.drawable.history_route_halo,
            x = 38f,
            y = 30f,
            width = 282f,
            height = 60.5f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        HistoryMapImage(
            Res.drawable.history_route,
            x = 38f,
            y = 30f,
            width = 282f,
            height = 60.5f,
            scaleX = scaleX,
            scaleY = scaleY,
        )
        HistoryMapLabel(stringResource(Res.string.ui_lviv), 16f, 88f, scaleX, scaleY, ApexText, 13.sp)
        HistoryMapLabel(stringResource(Res.string.ui_kyyiv), 290f, 25f, scaleX, scaleY, ApexMuted, 12.sp)
        HistoryMapLabel(stringResource(Res.string.ui_rivne_m_06), 145f, 70f, scaleX, scaleY, ApexMuted, 10.sp)
        Image(
            painter = painterResource(Res.drawable.history_position),
            contentDescription = stringResource(Res.string.ui_potochna_pozytsiya_na_demonstratsiynomu_marshruti),
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
            modifier = Modifier
                .offset(x = 220.dp * scaleX, y = 58.dp * scaleY)
                .size(26.dp * scaleX, 26.dp * scaleY),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(stringResource(Res.string.ui_mapbox_brand_badge), ApexText, 10.sp, weight = androidx.compose.ui.text.font.FontWeight.Bold)
            LabelText(stringResource(Res.string.ui_mapbox_openstreetmap), ApexMuted, 7.sp)
        }
    }
}

@Composable
private fun HistoryMapImage(
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
        contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
        modifier = Modifier
            .offset(x = x.dp * scaleX, y = y.dp * scaleY)
            .size(width = width.dp * scaleX, height = height.dp * scaleY),
    )
}

@Composable
private fun HistoryMapLabel(
    text: String,
    x: Float,
    y: Float,
    scaleX: Float,
    scaleY: Float,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
) {
    LabelText(
        text,
        color,
        fontSize,
        modifier = Modifier.offset(x = x.dp * scaleX, y = y.dp * scaleY),
    )
}

@Composable
private fun WorkHistoryTrip(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HistoryCard)
            .border(BorderStroke(1.dp, HistoryCard), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(
                stringResource(Res.string.ui_dl_198_kyyiv_zhytomyr),
                ApexText,
                13.sp,
                weight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HistoryWorkBackground)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.history_lock),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(HistoryGold),
                )
                LabelText(stringResource(Res.string.ui_robocha), HistoryGold, 10.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
        LabelText(stringResource(Res.string.ui_man_tgx_aa_4821_kkh_zaversheno_15_40), ApexMuted, 11.sp)
        LabelText(stringResource(Res.string.ui_142_km_2_hod_25_khv), ApexText, 11.sp)
        LabelText(stringResource(Res.string.ui_pozytsiya_y_zvit_dostupni_dnipro_logistics), HistoryGold, 10.sp)
    }
}

@Composable
private fun HistoryBadge(title: String, color: Color, background: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        LabelText(title, color, 10.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    }
}

@Composable
private fun TripClassificationRow(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HistoryCard)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.history_tags),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(stringResource(Res.string.ui_klasyfikatsiya_poyizdky), ApexText, 13.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_osobystyy_robochyy_rezhym_obyrayetsya_do_startu), ApexMuted, 10.sp)
        }
        Image(
            painter = painterResource(Res.drawable.offline_chevron),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            colorFilter = ColorFilter.tint(ApexSubtle),
        )
    }
}

@Composable
private fun HistoryDemonstrationNote() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HistoryWorkBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        LabelText(stringResource(Res.string.ui_demonstratsiynyy_ekran), HistoryGold, 12.sp, weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        LabelText(
            stringResource(Res.string.ui_imena_marshruty_daty_poyizdky_ta_statusy_dostupu_ye_prykladamy_m),
            ApexMuted,
            11.sp,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun TripHistoryBottomNavigation(
    bottomInset: Dp,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val tabs: List<Triple<DrawableResource, String, () -> Unit>> = listOf(
        Triple(Res.drawable.offline_bottom_map, stringResource(Res.string.ui_mapa), onOpenMap),
        Triple(Res.drawable.offline_bottom_car, stringResource(Res.string.ui_harazh), onOpenGarage),
        Triple(Res.drawable.offline_bottom_settings, stringResource(Res.string.ui_nalashtuvannya), onOpenSettings),
        Triple(Res.drawable.offline_bottom_profile, stringResource(Res.string.ui_profil), onOpenProfile),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HistoryCard)
            .border(BorderStroke(0.5.dp, ApexBorder)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(61.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, (icon, title, onClick) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = title,
                        modifier = Modifier.size(21.dp),
                        colorFilter = ColorFilter.tint(if (index == 3) ApexRed else ApexMuted),
                    )
                    LabelText(
                        title,
                        if (index == 3) ApexText else ApexMuted,
                        9.sp,
                        weight = if (index == 3) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
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
