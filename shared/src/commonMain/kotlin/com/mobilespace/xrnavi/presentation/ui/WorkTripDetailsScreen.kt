package com.mobilespace.xrnavi.presentation.ui

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.mobilespace.xrnavi.domain.WorkTripAction
import com.mobilespace.xrnavi.presentation.WorkTripDetailsViewModel
import com.mobilespace.xrnavi.presentation.FeatureDestination
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.job_alert
import xrnavi.shared.generated.resources.job_back
import xrnavi.shared.generated.resources.job_check
import xrnavi.shared.generated.resources.job_chevron
import xrnavi.shared.generated.resources.job_clock
import xrnavi.shared.generated.resources.job_clock_delay
import xrnavi.shared.generated.resources.job_help
import xrnavi.shared.generated.resources.job_map_pin
import xrnavi.shared.generated.resources.job_message
import xrnavi.shared.generated.resources.job_phone

private val WorkTripCard = Color(0xFF14191F)
private val WorkTripMetric = Color(0xFF20262E)
private val WorkTripWarning = Color(0xFF30291F)
private val WorkTripGold = Color(0xFFE9BA76)
private val WorkTripRed = Color(0xFFF45151)

@Composable
internal fun WorkTripDetailsScreen(
    onBack: () -> Unit,
    onOpenTripPlan: () -> Unit,
    viewModel: WorkTripDetailsViewModel,
    onOpenTaskRoute: (String?) -> Unit,
    onOpenTripPlanWithId: ((String?) -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onBack, onOpenTripPlan, onOpenTripPlanWithId, onOpenTaskRoute) {
        viewModel.effects.collect {
            when (it.destination) {
                FeatureDestination.Back -> onBack()
                FeatureDestination.TripPlan ->
                    if (onOpenTripPlanWithId != null) onOpenTripPlanWithId(it.taskId) else onOpenTripPlan()
                FeatureDestination.Map -> onOpenTaskRoute(it.taskId)
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
        WorkTripHeader(
            onBack = { viewModel.navigate(FeatureDestination.Back) },
            onHelp = viewModel::showHelp,
            taskId = state.chosenTask?.id,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF341B21))
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                ) {
                    LabelText(stringResource(Res.string.ui_u_dorozi), WorkTripRed, 10.sp, weight = FontWeight.SemiBold)
                }
                LabelText(stringResource(Res.string.ui_7_zhovtnya_14_20), ApexMuted, 10.sp)
            }
            WorkTripRouteCard(destination = state.chosenTask?.destination)
            PrimaryButton(stringResource(Res.string.ui_plan_task_route), enabled = true, onClick = viewModel::planRoute)
            WorkTripPlanAction(onClick = viewModel::openTripPlan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WorkTripMetric(stringResource(Res.string.ui_prybuttya_eta), stringResource(Res.string.ui_17_30), Modifier.weight(1f))
                WorkTripMetric(stringResource(Res.string.ui_vikno_pryymannya), stringResource(Res.string.ui_17_00_18_00), Modifier.weight(1f))
            }
            WorkTripNotice(
                icon = Res.drawable.job_clock_delay,
                title = stringResource(Res.string.ui_zatrymka_20_khv),
                description =
                    stringResource(Res.string.ui_plan_17_10_povidomlennya_pro_zatrymku_nadislano_oleni_shevchenko),
                background = WorkTripWarning,
                titleColor = WorkTripGold,
            )
            WorkTripContact(
                icon = Res.drawable.job_phone,
                title = stringResource(Res.string.ui_nataliya_boyko_sklad),
                subtitle = stringResource(Res.string.ui_380_67_410_22_30_kontakt_pryymannya),
                onClick = { viewModel.perform(WorkTripAction.ContactReceiver) },
            )
            WorkTripContact(
                icon = Res.drawable.job_message,
                title = stringResource(Res.string.ui_olena_shevchenko_dyspetcher),
                subtitle = stringResource(Res.string.ui_fleet_dnipro_logistics_ua),
                onClick = { viewModel.perform(WorkTripAction.MessageDispatcher) },
            )
            WorkTripNotice(
                icon = Res.drawable.job_alert,
                title = stringResource(Res.string.ui_pid_yizd_potrebuye_perevirky),
                description =
                    stringResource(Res.string.ui_ostanni_4_2_km_ne_perevireni_dlya_avtovoza_uzhodte_pid_yizd_iz_d),
                background = WorkTripWarning,
                titleColor = WorkTripGold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WorkTripActionButton(
                    icon = Res.drawable.job_map_pin,
                    label = stringResource(Res.string.ui_ya_prybuv),
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSubmitting,
                    onClick = { viewModel.perform(WorkTripAction.ReportArrival) },
                )
                WorkTripActionButton(
                    icon = Res.drawable.job_check,
                    label = stringResource(Res.string.ui_zavershyty),
                    modifier = Modifier.weight(1f),
                    enabled = false,
                    onClick = {},
                    disabled = true,
                )
            }
            LabelText(
                stringResource(Res.string.ui_zavershennya_dostupne_pislya_prybuttya_y_pidtverdzhennya_rozvant),
                ApexMuted,
                10.sp,
                lineHeight = 13.5.sp,
            )
            WorkTripContact(
                icon = Res.drawable.job_clock,
                title = stringResource(Res.string.ui_povidomyty_novu_zatrymku),
                subtitle = stringResource(Res.string.ui_prychyna_novyy_ochikuvanyy_chas),
                onClick = { viewModel.perform(WorkTripAction.ReportDelay) },
            )
            state.statusMessage?.let {
                LabelText(stringResource(it), ApexMuted, 11.sp, lineHeight = 15.sp)
            }
            LabelText(
                stringResource(Res.string.ui_pozytsiya_y_eta_dostupni_dyspetcheru_lyshe_v_tsomu_robochomu_rey),
                Color(0xFF85C6AC),
                10.sp,
                lineHeight = 13.5.sp,
            )
        }
        WorkTripHomeIndicator(bottomInset)
    }
}

@Composable
private fun WorkTripPlanAction(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WorkTripMetric)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_plan_reysu_chasovi_vikna), ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_zupynky_zavantazhennya_ta_rozvantazhennya), ApexMuted, 10.sp)
        }
        LabelText("›", ApexMuted, 20.sp)
    }
}

@Composable
private fun WorkTripHeader(onBack: () -> Unit, onHelp: () -> Unit, taskId: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.job_back),
            contentDescription = stringResource(Res.string.ui_nazad),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onBack),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            LabelText(taskId ?: stringResource(Res.string.ui_reys_dl_204), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_andriy_koval_man_tgx_aa_4821_kkh), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.job_help),
            contentDescription = stringResource(Res.string.ui_dopomoha_shchodo_reysu),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onHelp),
        )
    }
}

@Composable
private fun WorkTripRouteCard(destination: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WorkTripCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LabelText(destination ?: stringResource(Res.string.ui_kyyiv_lviv), ApexText, 28.sp, weight = FontWeight.SemiBold, condensed = true)
        LabelText(stringResource(Res.string.ui_rozvantazhennya_sklad_zakhid), ApexText, 12.sp)
        LabelText(stringResource(Res.string.ui_vul_horodotska_359_vorota_2), ApexMuted, 11.sp)
        LabelText(stringResource(Res.string.ui_6_avtomobiliv_36_8_t_ne_adr), ApexMuted, 11.sp)
    }
}

@Composable
private fun WorkTripMetric(title: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(WorkTripMetric)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LabelText(title, ApexMuted, 10.sp)
        LabelText(value, ApexText, 24.sp, weight = FontWeight.SemiBold, condensed = true)
    }
}

@Composable
private fun WorkTripNotice(
    icon: DrawableResource,
    title: String,
    description: String,
    background: Color,
    titleColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(title, titleColor, 12.sp, weight = FontWeight.SemiBold)
            LabelText(description, ApexMuted, 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun WorkTripContact(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WorkTripCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(title, ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(subtitle, ApexMuted, 10.sp)
        }
        Image(
            painter = painterResource(Res.drawable.job_chevron),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
private fun WorkTripActionButton(
    icon: DrawableResource,
    label: String,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    disabled: Boolean = false,
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(WorkTripMetric)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(19.dp),
        )
        LabelText(
            label,
            if (disabled) ApexSubtle else ApexText,
            14.sp,
            weight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun WorkTripHomeIndicator(bottomInset: androidx.compose.ui.unit.Dp) {
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
