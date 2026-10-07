package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.runtime.LaunchedEffect
import com.mobilespace.xrnavi.presentation.*

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SettingsCard = Color(0xFF20262E)
private val SettingsSelected = Color(0xFF341B21)
private val SettingsGold = Color(0xFFE9BA76)
private val SettingsPolicy = Color(0xFF30291F)

@Composable
internal fun RouteSettingsScreen(
    viewModel: RouteSettingsViewModel,
    onBack: () -> Unit,
    onApply: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.beginEditing() }
    NavigationEffects(viewModel) {
        when (it) {
            NavigationAction.Back -> onBack()
            NavigationAction.Apply -> onApply()
            else -> Unit
        }
    }
    var showHelp by remember { mutableStateOf(false) }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(
                "‹",
                ApexText,
                30.sp,
                modifier = Modifier.clickable { viewModel.navigate(NavigationAction.Back) },
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                LabelText(
                    stringResource(Res.string.ui_parametry_marshrutu),
                    ApexText,
                    26.sp,
                    weight = FontWeight.Bold,
                    condensed = true,
                    lineHeight = 30.sp,
                )
                LabelText(stringResource(Res.string.ui_kyyiv_lviv_korporatyvnyy_reys), ApexMuted, 11.sp)
            }
            LabelText(
                "ⓘ",
                ApexMuted,
                20.sp,
                modifier = Modifier.clickable { showHelp = !showHelp },
            )
        }
        if (showHelp) {
            LabelText(
                stringResource(Res.string.ui_parametry_korporatyvnoho_transportu_zatverdzheni_orhanizatsiyeyu),
                ApexMuted,
                12.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            VehiclePresets(
                selected = state.draft.vehicleCategory,
                onSelected = viewModel::selectVehicle,
            )
            FleetVehicleCard(state.draft.vehicleCategory)
            if (state.draft.vehicleCategory == 1) {
                DimensionsAndLoad()
                TowingConfiguration(
                    trailerIndex = state.draft.trailerIndex,
                    onTrailer = viewModel::nextTrailer,
                )
                InheritedSafetyPolicy()
            }
            TruckRouteAlternatives(
                selectedRoute = state.draft.selectedRoute,
                onSelect = viewModel::selectRoute,
            )
            RestStopCard(
                enabled = state.draft.restStopEnabled,
                onToggle = viewModel::toggleRestStop,
            )
            navigationNotice(state.notice)?.let { LabelText(it, ApexLinkRed, 11.sp) }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ApexBackground)
                .padding(horizontal = 20.dp)
                .padding(bottom = maxOf(bottomInset, 26.dp) + 23.dp),
        ) {
            SettingsActionButton(onClick = viewModel::apply)
        }
    }
}

private val TRAILER_OPTIONS = listOf(Res.string.ui_napivprychip, Res.string.ui_prychip, Res.string.ui_bez_prychepa)

@Composable
private fun VehiclePresets(selected: Int, onSelected: (Int) -> Unit) {
    val options = listOf(
        stringResource(Res.string.ui_lehkove),
        stringResource(Res.string.ui_vantazhne),
        stringResource(Res.string.ui_spetstekhnika),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ApexSurface)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEachIndexed { index, option ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected == index) SettingsSelected else Color.Transparent)
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center,
            ) {
                LabelText(
                    option,
                    when {
                        selected == index -> ApexLinkRed
                        else -> ApexMuted
                    },
                    11.sp,
                    weight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun FleetVehicleCard(category: Int) {
    val vehicleName = when (category) {
        0 -> stringResource(Res.string.ui_ford_mustang_gt_osobystyy)
        1 -> stringResource(Res.string.ui_man_tgx_avtovoz)
        else -> stringResource(Res.string.ui_jcb_540_170_teleskopichnyy)
    }
    val vehicleInfo = when (category) {
        0 -> stringResource(Res.string.ui_aa_0001_ar_osobystyy_avtomobil)
        1 -> stringResource(Res.string.ui_aa_4821_kkh_dnipro_logistics)
        else -> stringResource(Res.string.ui_aa_7720_ts_dnipro_logistics)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ApexSurface)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 13.dp)
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SettingsCard),
            contentAlignment = Alignment.Center,
        ) {
            LabelText(if (category == 0) "▱" else if (category == 1) "▰" else "⚒", ApexMuted, 23.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(vehicleName, ApexText, 15.sp, weight = FontWeight.SemiBold)
            LabelText(vehicleInfo, ApexMuted, 10.sp)
        }
        LabelText("⌄", ApexMuted, 18.sp, modifier = Modifier.padding(end = 13.dp))
    }
}

@Composable
private fun DimensionsAndLoad() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LabelText(stringResource(Res.string.ui_habaryty_ta_masa), ApexMuted, 11.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_z_urakhuvannyam_vantazhu), ApexSubtle, 10.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DimensionCard("↕", stringResource(Res.string.ui_vysota), stringResource(Res.string.ui_4_0), stringResource(Res.string.ui_m), Modifier.weight(1f))
            DimensionCard("↔", stringResource(Res.string.ui_shyryna), stringResource(Res.string.ui_2_55), stringResource(Res.string.ui_m), Modifier.weight(1f))
            DimensionCard("↔", stringResource(Res.string.ui_dovzhyna), stringResource(Res.string.ui_16_5), stringResource(Res.string.ui_m), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeightCard(stringResource(Res.string.ui_povna_masa), stringResource(Res.string.ui_40_t), Modifier.weight(1f))
            WeightCard(stringResource(Res.string.ui_na_vis), stringResource(Res.string.ui_11_5_t), Modifier.weight(1f))
        }
    }
}

@Composable
private fun DimensionCard(
    icon: String,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SettingsCard)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LabelText(icon, ApexMuted, 12.sp)
            Spacer(Modifier.width(5.dp))
            LabelText(label, ApexMuted, 10.sp)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            LabelText(value, ApexText, 22.sp, weight = FontWeight.Bold, condensed = true)
            Spacer(Modifier.width(5.dp))
            LabelText(unit, ApexMuted, 11.sp)
        }
    }
}

@Composable
private fun WeightCard(label: String, value: String, modifier: Modifier) {
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SettingsCard)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LabelText(label, ApexMuted, 11.sp)
        LabelText(value, ApexText, 14.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun TowingConfiguration(trailerIndex: Int, onTrailer: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LabelText("⬡", ApexMuted, 17.sp)
                Spacer(Modifier.width(8.dp))
                LabelText(stringResource(Res.string.ui_prychip_buksyruvannya_2), ApexText, 12.sp)
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SettingsCard)
                    .clickable(onClick = onTrailer)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabelText("⌄", ApexMuted, 12.sp)
                Spacer(Modifier.width(5.dp))
                LabelText(stringResource(TRAILER_OPTIONS[trailerIndex]), ApexMuted, 10.sp, weight = FontWeight.SemiBold)
            }
        }
        LabelText(
            stringResource(Res.string.ui_profili_evakuator_avtovoz_tyahach_spetstekhnika),
            ApexSubtle,
            10.sp,
        )
    }
}

@Composable
private fun InheritedSafetyPolicy() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SettingsPolicy)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        LabelText("♙", SettingsGold, 17.sp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(
                stringResource(Res.string.ui_profil_zatverdzheno_orhanizatsiyeyu),
                SettingsGold,
                11.sp,
                weight = FontWeight.SemiBold,
            )
            LabelText(
                stringResource(Res.string.ui_habaryty_y_zaborona_gruntovykh_dorih_zablokovani_zminy_cherez_dy),
                ApexMuted,
                10.sp,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun TruckRouteAlternatives(selectedRoute: Int, onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        LabelText(stringResource(Res.string.ui_marshruty_dlya_avtovoza), ApexMuted, 11.sp, weight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SettingsSelected)
                .border(
                    1.dp,
                    if (selectedRoute == 0) ApexRed else ApexBorder,
                    RoundedCornerShape(8.dp),
                )
                .clickable { onSelect(0) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(1.dp, MapRedSettings, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                LabelText("✓", MapRedSettings, 11.sp, weight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LabelText(stringResource(Res.string.ui_m_06_ob_yizd_tsentru_lvova), ApexText, 12.sp, weight = FontWeight.SemiBold)
                LabelText(stringResource(Res.string.ui_552_km_bez_mostu_z_limitom_3_8_m), ApexMuted, 10.sp)
            }
            LabelText(stringResource(Res.string.ui_7_10), ApexText, 17.sp, weight = FontWeight.Bold, condensed = true)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ApexSurface)
                .border(
                    1.dp,
                    if (selectedRoute == 1) ApexRed else Color.Transparent,
                    RoundedCornerShape(8.dp),
                )
                .clickable { onSelect(1) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LabelText(stringResource(Res.string.ui_cherez_ternopil_594_km_48_khv), ApexMuted, 11.sp)
            LabelText(stringResource(Res.string.ui_7_58), ApexMuted, 15.sp, weight = FontWeight.SemiBold, condensed = true)
        }
    }
}

private val MapRedSettings = Color(0xFFF45151)

@Composable
private fun RestStopCard(enabled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ApexSurface)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LabelText("Ⓟ", Color(0xFF85C6AC), 19.sp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            LabelText(stringResource(Res.string.ui_zupynka_bilya_rivnoho_cherez_320_km), ApexText, 11.sp, weight = FontWeight.Medium)
            LabelText(stringResource(Res.string.ui_azs_stoyanka_dlya_avtovoziv_vidpochynok), ApexMuted, 10.sp)
        }
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (enabled) ApexRed else ApexSurface)
                .border(1.dp, if (enabled) ApexRed else ApexMuted, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (enabled) LabelText("✓", ApexText, 12.sp, weight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsActionButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ApexRed)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText("☷", ApexText, 18.sp)
        Spacer(Modifier.width(10.dp))
        LabelText(stringResource(Res.string.ui_zastosuvaty_ta_pobuduvaty), ApexText, 15.sp, weight = FontWeight.SemiBold)
    }
}
