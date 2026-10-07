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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import com.mobilespace.xrnavi.presentation.TripPlanViewModel
import com.mobilespace.xrnavi.presentation.FeatureDestination
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.offline_back
import xrnavi.shared.generated.resources.offline_help
import xrnavi.shared.generated.resources.trip_plan_alert
import xrnavi.shared.generated.resources.trip_plan_check
import xrnavi.shared.generated.resources.trip_plan_pin
import xrnavi.shared.generated.resources.trip_plan_plus

private val TripPlanCard = Color(0xFF14191F)
private val TripPlanWarning = Color(0xFF30291F)
private val TripPlanGold = Color(0xFFE9BA76)
private val TripPlanGreen = Color(0xFF85C6AC)
private val TripPlanRed = Color(0xFFF45151)

@Composable
internal fun TripPlanScreen(
    onBack: () -> Unit,
    viewModel: TripPlanViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onBack) {
        viewModel.effects.collect {
            if (it.destination == FeatureDestination.Back) onBack()
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
        TripPlanHeader(
            onBack = { viewModel.navigate(FeatureDestination.Back) },
            onHelp = viewModel::toggleHelp,
            subtitle = if (state.personalPlan) state.destination else
                state.chosenTask?.id?.let { if (it == "DL-204") stringResource(Res.string.ui_dl_204_kyyiv_lviv_7_zhovtnya) else it }
                    .orEmpty(),
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
            if (state.personalPlan) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(TripPlanCard)
                        .padding(14.dp)
                ) {
                    LabelText(stringResource(Res.string.ui_osobystyy_rezhym), ApexMuted, 11.sp)
                    LabelText(state.destination, ApexText, 20.sp, weight = FontWeight.SemiBold)
                }
            } else {
                TripPlanSummaryCard()
                TripPlanSectionHeading()
                TripPlanStopCard(
                    icon = Res.drawable.trip_plan_check,
                    title = stringResource(Res.string.ui_zavantazhennya_6_avto),
                    place = stringResource(Res.string.ui_kyyiv_terminal_pivnich),
                    time = stringResource(Res.string.ui_08_00_09_00_vyyizd_09_00),
                    contact = stringResource(Res.string.ui_ihor_melnyk_380_67_410_22_10),
                    timeColor = ApexText,
                )
                TripPlanStopCard(
                    icon = Res.drawable.trip_plan_check,
                    title = stringResource(Res.string.ui_vidpochynok_45_khv),
                    place = stringResource(Res.string.ui_stoyanka_m_06_rivne),
                    time = stringResource(Res.string.ui_13_15_14_00_zaplanovana_pererva),
                    contact = stringResource(Res.string.ui_cherhovyy_380_67_410_22_20),
                    timeColor = ApexText,
                )
                TripPlanStopCard(
                    icon = Res.drawable.trip_plan_pin,
                    title = stringResource(Res.string.ui_rozvantazhennya_sklad_zakhid),
                    place = state.destination,
                    time = stringResource(Res.string.ui_trip_stop_time_eta),
                    contact = stringResource(Res.string.ui_nataliya_boyko_380_67_410_22_30),
                    timeColor = TripPlanGold,
                    iconTint = TripPlanRed,
                )
                TripPlanDelayNotice()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF20262E))
                    .clickable(enabled = !state.isSubmitting, onClick = viewModel::addStop),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.trip_plan_plus),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    colorFilter = ColorFilter.tint(ApexText),
                )
                LabelText(
                    stringResource(Res.string.ui_dodaty_zupynku),
                    ApexText,
                    14.sp,
                    weight = FontWeight.SemiBold
                )
            }
            state.statusMessage?.let {
                LabelText(stringResource(it), TripPlanGold, 10.sp, lineHeight = 14.sp)
            }
            if (state.showHelp) {
                LabelText(
                    stringResource(Res.string.ui_chasovi_vikna_y_poryadok_zupynok_navedeni_yak_pryklad_planu_reys),
                    ApexMuted,
                    10.sp,
                    lineHeight = 14.sp,
                )
            } else {
                LabelText(
                    stringResource(Res.string.ui_chasovyy_plan_oriyentyr_ne_harantiya_prybuttya),
                    ApexSubtle,
                    10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
        TripPlanHomeIndicator(bottomInset)
    }
}

@Composable
private fun TripPlanHeader(onBack: () -> Unit, onHelp: () -> Unit, subtitle: String) {
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
            LabelText(
                stringResource(Res.string.ui_plan_reysu),
                ApexText,
                26.sp,
                weight = FontWeight.SemiBold,
                condensed = true
            )
            LabelText(subtitle, ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.offline_help),
            contentDescription = stringResource(Res.string.ui_dovidka_pro_plan_reysu),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onHelp),
        )
    }
}

@Composable
private fun TripPlanSummaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TripPlanCard)
            .border(BorderStroke(1.dp, TripPlanCard), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(
                stringResource(Res.string.ui_552_km),
                ApexText,
                27.sp,
                weight = FontWeight.Bold,
                condensed = true
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF20262E))
                    .padding(horizontal = 9.dp, vertical = 6.dp),
            ) {
                LabelText(
                    stringResource(Res.string.ui_3_zupynky),
                    ApexMuted,
                    10.sp,
                    weight = FontWeight.SemiBold
                )
            }
        }
        LabelText(
            stringResource(Res.string.ui_man_tgx_aa_4821_kkh_z_vantazhem_36_8_t),
            ApexMuted,
            11.sp
        )
    }
}

@Composable
private fun TripPlanSectionHeading() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(
            stringResource(Res.string.ui_chasovi_vikna),
            ApexMuted,
            11.sp,
            weight = FontWeight.SemiBold
        )
        LabelText(stringResource(Res.string.ui_poryadok_uzhodzhuye_dyspetcher), ApexSubtle, 10.sp)
    }
}

@Composable
private fun TripPlanStopCard(
    icon: DrawableResource,
    title: String,
    place: String,
    time: String,
    contact: String,
    timeColor: Color,
    iconTint: Color = TripPlanGreen,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TripPlanCard)
            .border(BorderStroke(1.dp, TripPlanCard), RoundedCornerShape(16.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(iconTint),
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                LabelText(title, ApexText, 13.sp, weight = FontWeight.SemiBold)
                LabelText(place, ApexMuted, 11.sp)
            }
        }
        LabelText(time, timeColor, 12.sp)
        LabelText(contact, ApexMuted, 10.sp)
    }
}

@Composable
private fun TripPlanDelayNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TripPlanWarning)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.trip_plan_alert),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(TripPlanGold),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(
                stringResource(Res.string.ui_prybuttya_20_khv_do_planu),
                TripPlanGold,
                12.sp,
                weight = FontWeight.SemiBold
            )
            LabelText(
                stringResource(Res.string.ui_eta_17_30_zalyshayetsya_u_vikni_pid_yizd_ostannikh_4_2_km_shche),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun TripPlanHomeIndicator(bottomInset: Dp) {
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
