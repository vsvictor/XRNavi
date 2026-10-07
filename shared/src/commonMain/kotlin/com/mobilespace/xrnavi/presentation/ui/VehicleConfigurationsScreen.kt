package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
import com.mobilespace.xrnavi.domain.ConfigurationId
import com.mobilespace.xrnavi.presentation.VehicleConfigurationsViewModel
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
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.company_check
import xrnavi.shared.generated.resources.company_lock
import xrnavi.shared.generated.resources.company_lock_alt
import xrnavi.shared.generated.resources.offline_back
import xrnavi.shared.generated.resources.offline_help

private val ConfigurationCard = Color(0xFF14191F)
private val ConfigurationSelected = Color(0xFF341B21)
private val ConfigurationPolicy = Color(0xFF30291F)
private val ConfigurationGold = Color(0xFFE9BA76)

@Composable
internal fun VehicleConfigurationsScreen(
    viewModel: VehicleConfigurationsViewModel,
    onBack: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val statusMessage = vehicleMessage(state.message)
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        VehicleConfigurationsHeader(
            subtitle = if (state.isPersonal) fordProfileTitle(state.ford)
                else stringResource(Res.string.ui_man_tgx_aa_4821_kkh_dnipro_logistics),
            onBack = onBack,
            onHelp = {
                if (viewModel.toggleHelp()) onOpenHelp()
            },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = maxOf(20.dp, bottomInset)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isPersonal) {
                ConfigurationCard(
                    title = fordProfileTitle(state.ford),
                    measurements = "${state.ford.dimensions.height} × ${state.ford.dimensions.width} × ${state.ford.dimensions.length} м · ${state.ford.dimensions.mass} т",
                    description = stringResource(Res.string.ui_zobrazhennya_ne_pidtverdzhuye_komplektatsiyu_parametry_zvirte_z),
                    selected = true,
                    onClick = {},
                )
            } else {
            ConfigurationSectionHeading(stringResource(Res.string.ui_stan_transportu),
                stringResource(Res.string.ui_kataloh_obmezhenyy))
            state.configurations.forEachIndexed { index, configuration ->
                ConfigurationCard(
                    title = stringResource(when (configuration.id) {
                        ConfigurationId.Empty -> Res.string.ui_bez_vantazhu
                        ConfigurationId.Loaded -> Res.string.ui_z_vantazhem_obrano
                        ConfigurationId.Recovery -> Res.string.ui_buksyruvannya_na_platformi
                    }),
                    status = when (configuration.id) {
                        ConfigurationId.Empty -> stringResource(Res.string.ui_zatverdzheno)
                        ConfigurationId.Loaded -> stringResource(Res.string.ui_robochyy)
                        ConfigurationId.Recovery -> null
                    },
                    measurements = "${configuration.dimensions.height} × ${configuration.dimensions.width} × ${configuration.dimensions.length} м · ${configuration.dimensions.mass} т · ${stringResource(Res.string.ui_na_vis)} ${configuration.axleMass} т",
                    description = stringResource(when (configuration.id) {
                        ConfigurationId.Empty -> Res.string.ui_napivprychip_avtovoza_vantazh_vidsutniy
                        ConfigurationId.Loaded -> Res.string.ui_napivprychip_6_avtomobiliv_ne_adr
                        ConfigurationId.Recovery -> Res.string.ui_iveco_daily_ae_7310_rs_bez_prychepa
                    }),
                    extra = if (configuration.id == ConfigurationId.Recovery)
                        stringResource(Res.string.ui_odne_avto_limit_7_2_t_buksyr_na_trosi_zaboroneno) else null,
                    selected = state.selected == configuration.id,
                    approved = configuration.id == ConfigurationId.Empty,
                    onClick = { viewModel.selectConfiguration(index) },
                )
            }
            ConfigurationSectionHeading(stringResource(Res.string.ui_zablokovani_limity), stringResource(Res.string.ui_dlya_obranoho_profilyu))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LockedLimitCard(
                    title = stringResource(Res.string.ui_povna_masa),
                    value = "${state.selectedConfiguration?.maximumMass ?: 0.0} ${stringResource(Res.string.ui_t)}",
                    modifier = Modifier.weight(1f),
                )
                LockedLimitCard(
                    title = stringResource(Res.string.ui_na_vis),
                    value = "${state.selectedConfiguration?.maximumAxleMass ?: 0.0} ${stringResource(Res.string.ui_t)}",
                    modifier = Modifier.weight(1f),
                )
            }
            InheritedPolicyNotice()
            LabelText(
                stringResource(Res.string.ui_habaryty_vklyuchayut_tyahach_prychip_i_vantazh_faktychni_masy_zi),
                ApexMuted,
                10.sp,
                lineHeight = 14.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexRed)
                    .clickable(enabled = !state.isSubmitting, onClick = viewModel::applyConfiguration),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.company_check),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    colorFilter = ColorFilter.tint(ApexText),
                )
                LabelText(stringResource(Res.string.ui_zastosuvaty_konfihuratsiyu), ApexText, 14.sp, weight = FontWeight.SemiBold)
            }
            LabelText(
                stringResource(Res.string.ui_zaprosyty_zminu_v_dyspetchera),
                ApexMuted,
                11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !state.isSubmitting, onClick = viewModel::requestDispatcherChange)
                    .padding(vertical = 3.dp),
                textAlign = TextAlign.Center,
            )
            }
            statusMessage?.let {
                LabelText(it, ConfigurationGold, 10.sp, lineHeight = 14.sp)
            }
            if (state.helpVisible) {
                LabelText(
                    stringResource(if (state.isPersonal) Res.string.ui_zobrazhennya_ne_pidtverdzhuye_komplektatsiyu_parametry_zvirte_z
                        else Res.string.ui_tsey_ekran_pokazuye_pryklady_konfihuratsiy_zatverdzheni_habaryty),
                    ApexMuted,
                    10.sp,
                    lineHeight = 14.sp,
                )
            }
        }
        ConfigurationHomeIndicator(bottomInset = bottomInset)
    }
}

@Composable
private fun VehicleConfigurationsHeader(subtitle: String, onBack: () -> Unit, onHelp: () -> Unit) {
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
            LabelText(stringResource(Res.string.ui_konfihuratsiyi), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(subtitle, ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.offline_help),
            contentDescription = stringResource(Res.string.ui_dovidka),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onHelp),
        )
    }
}

@Composable
private fun ConfigurationSectionHeading(title: String, trailing: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(title, ApexMuted, 11.sp, weight = FontWeight.SemiBold)
        LabelText(trailing, ApexSubtle, 10.sp)
    }
}

@Composable
private fun ConfigurationCard(
    title: String,
    measurements: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    status: String? = null,
    approved: Boolean = false,
    extra: String? = null,
) {
    val surface = if (selected) ConfigurationSelected else ConfigurationCard
    val outline = if (selected) ApexRed else ConfigurationCard
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surface)
            .border(BorderStroke(1.dp, outline), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(
                title,
                ApexText,
                13.sp,
                weight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            status?.let {
                if (approved) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ConfigurationPolicy)
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.company_lock),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            colorFilter = ColorFilter.tint(ConfigurationGold),
                        )
                        LabelText(it, ConfigurationGold, 10.sp, weight = FontWeight.SemiBold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ConfigurationSelected)
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                    ) {
                        LabelText(it, ApexLinkRed, 10.sp, weight = FontWeight.SemiBold)
                    }
                }
            }
        }
        LabelText(measurements, if (selected) ApexText else ApexMuted, 11.sp, lineHeight = 15.sp)
        LabelText(description, ApexMuted, 10.sp, lineHeight = 14.sp)
        extra?.let { LabelText(it, ApexMuted, 10.sp, lineHeight = 14.sp) }
    }
}

@Composable
private fun LockedLimitCard(title: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ConfigurationCard)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(title, ApexText, 13.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        LabelText(value, ConfigurationGold, 12.sp, weight = FontWeight.SemiBold)
        Image(
            painter = painterResource(Res.drawable.company_lock_alt),
            contentDescription = stringResource(Res.string.ui_limit_vstanovleno_orhanizatsiyeyu),
            modifier = Modifier.size(15.dp),
            colorFilter = ColorFilter.tint(ConfigurationGold),
        )
    }
}

@Composable
private fun InheritedPolicyNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ConfigurationPolicy)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.company_lock_alt),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(ConfigurationGold),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_polityka_3_2_uspadkovano), ConfigurationGold, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_habaryty_zatverdzheni_bez_gruntovykh_dorih_do_90_km_hod_nebezpec),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun ConfigurationHomeIndicator(bottomInset: Dp) {
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
