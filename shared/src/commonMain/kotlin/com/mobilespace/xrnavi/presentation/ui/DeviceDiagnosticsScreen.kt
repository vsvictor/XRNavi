package com.mobilespace.xrnavi.presentation.ui

import com.mobilespace.xrnavi.DeviceSettingsResult
import com.mobilespace.xrnavi.rememberDeviceSettingsLauncher

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.domain.*

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.diagnostics_back
import xrnavi.shared.generated.resources.diagnostics_battery
import xrnavi.shared.generated.resources.diagnostics_bell
import xrnavi.shared.generated.resources.diagnostics_chevron
import xrnavi.shared.generated.resources.diagnostics_help
import xrnavi.shared.generated.resources.diagnostics_location
import xrnavi.shared.generated.resources.diagnostics_nav_car
import xrnavi.shared.generated.resources.diagnostics_nav_map
import xrnavi.shared.generated.resources.diagnostics_nav_profile
import xrnavi.shared.generated.resources.diagnostics_nav_settings
import xrnavi.shared.generated.resources.diagnostics_settings
import xrnavi.shared.generated.resources.diagnostics_volume
import xrnavi.shared.generated.resources.diagnostics_warning

private val DiagnosticsField = Color(0xFF20262E)
private val DiagnosticsWarning = Color(0xFF30291F)
private val DiagnosticsGold = Color(0xFFE9BA76)
private val DiagnosticsGreen = Color(0xFF85C6AC)

@Composable
internal fun DeviceDiagnosticsScreen(
    viewModel: DeviceDiagnosticsViewModel,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val localized_ui_statusy_dozvoliv_holosovykh_pidkazok_i_gps_pryklady_z_maketa_ne = stringResource(Res.string.ui_statusy_dozvoliv_holosovykh_pidkazok_i_gps_pryklady_z_maketa_ne)
    val localized_ui_servisy_diahnostyky_ta_holosovoyi_navihatsiyi_shche_ne_pidklyuch = stringResource(Res.string.ui_servisy_diahnostyky_ta_holosovoyi_navihatsiyi_shche_ne_pidklyuch)
    val localized_ui_uvimkneno = stringResource(Res.string.ui_uvimkneno)
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val statusMessage = if (uiState.showHelp) {
        localized_ui_statusy_dozvoliv_holosovykh_pidkazok_i_gps_pryklady_z_maketa_ne +
            localized_ui_servisy_diahnostyky_ta_holosovoyi_navihatsiyi_shche_ne_pidklyuch
    } else uiState.message?.let { stringResource(it) }
    val settingsLauncher = rememberDeviceSettingsLauncher()
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val openDeviceSettings: () -> Unit = viewModel::openSettings
    LaunchedEffect(viewModel, settingsLauncher) {
        viewModel.effects.collect {
            settingsLauncher.open { result -> viewModel.settingsResult(result == DeviceSettingsResult.Opened) }
        }
    }

    Column(
        Modifier.fillMaxSize().background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        DiagnosticsHeader(
            onBack = onBack,
            onHelp = {
viewModel.help()
            },
        )
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DiagnosticsSectionHeading(stringResource(Res.string.ui_ekran_avtomobilya))
            CarIntegrationsCard()
            DiagnosticsSectionHeading(stringResource(Res.string.ui_navihatsiya_u_foni))
            DiagnosticsSettingRow(
                Res.drawable.diagnostics_location,
                stringResource(Res.string.ui_fonova_heolokatsiya),
                stringResource(Res.string.ui_status_prykladu_dostup_nadano_u_nalashtuvannyakh_os),
                stringResource(Res.string.ui_dostupna),
                openDeviceSettings,
            )
            DiagnosticsSettingRow(
                Res.drawable.diagnostics_volume,
                stringResource(Res.string.ui_holosovi_pidkazky),
                stringResource(Res.string.ui_ukrayinska_olena_dynamik_telefona),
                localized_ui_uvimkneno,
                onClick = {
                    viewModel.voice()
                },
            )
            DiagnosticsSettingRow(
                Res.drawable.diagnostics_bell,
                stringResource(Res.string.ui_spovishchennya),
                stringResource(Res.string.ui_manevry_u_foni_novi_zavdannya_dyspetchera),
                stringResource(Res.string.ui_dozvoleno),
                openDeviceSettings,
            )
            DiagnosticsSettingRow(
                Res.drawable.diagnostics_battery,
                stringResource(Res.string.ui_enerhozberezhennya),
                stringResource(Res.string.ui_ne_rekomendovano_pid_chas_aktyvnoyi_navihatsiyi),
                stringResource(Res.string.ui_vymkneno),
                openDeviceSettings,
            )
            BatteryWarning()
            DiagnosticsSectionHeading(stringResource(Res.string.ui_diahnostyka_gps), stringResource(Res.string.ui_onovleno_09_41_20))
            GpsExampleCard()
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(16.dp)).background(DiagnosticsField)
                    .clickable(role = Role.Button, onClick = openDeviceSettings)
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DiagnosticsIcon(Res.drawable.diagnostics_settings, Modifier.size(19.dp))
                LabelText(
                    stringResource(Res.string.ui_vidkryty_nalashtuvannya_prystroyu),
                    ApexText,
                    14.sp,
                    Modifier.weight(1f, fill = false),
                    weight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            LabelText(
                stringResource(Res.string.ui_dozvoly_ta_gps_navedeni_yak_pryklad_dani_tsoho_prystroyu_ne_zchy),
                ApexSubtle,
                10.sp,
                Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            statusMessage?.let { LabelText(it, ApexLinkRed, 11.sp, lineHeight = 15.sp) }
        }
        DiagnosticsBottomNavigation(onOpenMap, onOpenGarage, onOpenSettings, onOpenProfile)
        Box(Modifier.fillMaxWidth().height(bottomInset).background(ApexSurface))
    }
}

@Composable
private fun DiagnosticsHeader(onBack: () -> Unit, onHelp: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 70.dp).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(width = 44.dp, height = 48.dp).clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(Res.drawable.diagnostics_back), stringResource(Res.string.ui_nazad), Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            LabelText(stringResource(Res.string.ui_prystroyi_ta_diahnostyka), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_nalashtuvannya_android_ios_kontsept), ApexMuted, 11.sp)
        }
        Box(
            Modifier.size(width = 44.dp, height = 48.dp).clickable(role = Role.Button, onClick = onHelp),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(Res.drawable.diagnostics_help), stringResource(Res.string.ui_dovidka), Modifier.size(20.dp))
        }
    }
}

@Composable
private fun DiagnosticsSectionHeading(title: String, note: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LabelText(title, ApexMuted, 11.sp, Modifier.weight(1f), weight = FontWeight.SemiBold)
        note?.let { LabelText(it, ApexSubtle, 10.sp) }
    }
}

@Composable
private fun CarIntegrationsCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ApexSurface).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        listOf(
            stringResource(Res.string.ui_android_auto),
            stringResource(Res.string.ui_apple_carplay),
        ).forEach { name ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                LabelText(name, ApexText, 13.sp, Modifier.weight(1f), weight = FontWeight.SemiBold)
                DiagnosticsBadge(stringResource(Res.string.ui_zaplanovano), ApexMuted)
            }
            LabelText(stringResource(Res.string.ui_ne_pidklyucheno_intehratsiya_shche_ne_dostupna), ApexMuted, 10.sp)
        }
    }
}

@Composable
private fun DiagnosticsBadge(text: String, color: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(DiagnosticsField)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        LabelText(text, color, 10.sp, weight = FontWeight.SemiBold, lineHeight = 12.sp)
    }
}

@Composable
private fun DiagnosticsSettingRow(
    icon: DrawableResource,
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(ApexSurface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DiagnosticsIcon(icon, Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(title, ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(description, ApexMuted, 10.sp)
        }
        LabelText(value, ApexMuted, 12.sp, weight = FontWeight.SemiBold)
        DiagnosticsIcon(Res.drawable.diagnostics_chevron, Modifier.size(15.dp))
    }
}

@Composable
private fun BatteryWarning() {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DiagnosticsWarning).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DiagnosticsIcon(Res.drawable.diagnostics_warning, Modifier.size(18.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_menshe_batareyi_ridshe_onovlennya), DiagnosticsGold, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_enerhozberezhennya_os_mozhe_zatrymuvaty_gps_fonovi_pidkazky_y_pe) +
                    stringResource(Res.string.ui_dozvoly_zminyuyutsya_v_nalashtuvannyakh_prystroyu),
                ApexMuted,
                11.sp,
            )
        }
    }
}

@Composable
private fun GpsExampleCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ApexSurface).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            LabelText(stringResource(Res.string.ui_syhnal_nadiynyy), ApexText, 13.sp, Modifier.weight(1f), weight = FontWeight.SemiBold)
            DiagnosticsBadge(stringResource(Res.string.ui_12_m), DiagnosticsGreen)
        }
        LabelText(stringResource(Res.string.ui_vik_vymiru_2_s_dzherelo_heolokatsiya_os), ApexMuted, 11.sp)
        LabelText(
            stringResource(Res.string.ui_yakshcho_dani_zastariyut_navihatsiya_pokazhe_ostannyu_pozytsiyu),
            ApexMuted,
            10.sp,
        )
    }
}

@Composable
private fun DiagnosticsBottomNavigation(
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val tabs = listOf(
        Triple(Res.drawable.diagnostics_nav_map, stringResource(Res.string.ui_mapa), onOpenMap),
        Triple(Res.drawable.diagnostics_nav_car, stringResource(Res.string.ui_harazh), onOpenGarage),
        Triple(Res.drawable.diagnostics_nav_settings, stringResource(Res.string.ui_nalashtuvannya), onOpenSettings),
        Triple(Res.drawable.diagnostics_nav_profile, stringResource(Res.string.ui_profil), onOpenProfile),
    )
    Column(Modifier.fillMaxWidth().background(ApexSurface)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(ApexBorder))
        Row(Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
            tabs.forEachIndexed { index, (icon, title, onClick) ->
                Column(
                    Modifier.weight(1f).clickable(role = Role.Tab, onClick = onClick).padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    DiagnosticsIcon(icon, Modifier.size(21.dp))
                    LabelText(title, if (index == 2) ApexText else ApexMuted, 9.sp, lineHeight = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsIcon(resource: DrawableResource, modifier: Modifier) {
    Image(painterResource(resource), contentDescription = null, modifier = modifier)
}
