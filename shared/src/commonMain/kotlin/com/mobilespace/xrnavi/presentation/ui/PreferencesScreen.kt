package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.domain.*

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.diagnostics_settings
import xrnavi.shared.generated.resources.nav_car
import xrnavi.shared.generated.resources.nav_map
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.nav_settings
import xrnavi.shared.generated.resources.settings_building
import xrnavi.shared.generated.resources.settings_download
import xrnavi.shared.generated.resources.settings_gauge
import xrnavi.shared.generated.resources.settings_languages
import xrnavi.shared.generated.resources.settings_lock
import xrnavi.shared.generated.resources.settings_moon
import xrnavi.shared.generated.resources.settings_route
import xrnavi.shared.generated.resources.settings_scan
import xrnavi.shared.generated.resources.settings_volume
import xrnavi.shared.generated.resources.vehicle_search

private val PreferencesCard = Color(0xFF14191F)
private val PreferencesDivider = Color(0xFF303741)
private val PreferencesGold = Color(0xFFE9BA76)
private val PreferencesValueBackground = Color(0xFF20262E)

@Composable
internal fun PreferencesScreen(
    viewModel: PreferencesViewModel,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenOrganization: () -> Unit,
    onOpenOfflineMaps: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenDeviceDiagnostics: () -> Unit,
) {
    val localized_ui_ukrayinska = stringResource(Res.string.ui_ukrayinska)
    val localized_ui_temnyy = stringResource(Res.string.ui_temnyy)
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val voiceEnabled = uiState.preferences.voiceEnabled
    val xrEnabled = uiState.preferences.xrEnabled
    val statusMessage = uiState.message?.let { stringResource(it) }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        PreferencesHeader(
            onBack = onBack,
            onSearch = {
viewModel.search()
            },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            statusMessage?.let {
                LabelText(it, ApexMuted, 11.sp)
            }
            OrganizationCard(
                onClick = onOpenOrganization,
            )
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                PreferencesSectionHeading(stringResource(Res.string.ui_osobysti), stringResource(Res.string.ui_mozhna_zminyuvaty))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PreferencesCard),
                ) {
                    PersonalPreferenceRow(
                        icon = Res.drawable.settings_languages,
                        title = stringResource(Res.string.ui_mova_interfeysu),
                        description = stringResource(Res.string.ui_mova_mapy_ta_pidkazok),
                        value = localized_ui_ukrayinska,
                        onClick = {
                            viewModel.language()
                        },
                    )
                    PersonalPreferenceRow(
                        icon = Res.drawable.settings_moon,
                        title = stringResource(Res.string.ui_vyhlyad_mapy),
                        description = stringResource(Res.string.ui_temnyy_rezhym_yak_u_mustang),
                        value = localized_ui_temnyy,
                        onClick = {
                            viewModel.theme()
                        },
                    )
                    PersonalPreferenceRow(
                        icon = Res.drawable.settings_volume,
                        title = stringResource(Res.string.ui_holosovi_pidkazky),
                        description = stringResource(Res.string.ui_ukrayinska_olena),
                        toggle = voiceEnabled,
                        onClick = {
viewModel.toggleVoice()
                        },
                    )
                    PersonalPreferenceRow(
                        icon = Res.drawable.settings_scan,
                        title = stringResource(Res.string.ui_xr_navihatsiya),
                        description = stringResource(Res.string.ui_lyshe_z_telefonom_u_trymachi),
                        toggle = xrEnabled,
                        onClick = {
viewModel.toggleXr()
                        },
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                PreferencesSectionHeading(stringResource(Res.string.ui_vid_orhanizatsiyi), stringResource(Res.string.ui_lyshe_dlya_robochykh_reysiv))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PreferencesCard),
                ) {
                    OrganizationPreferenceRow(
                        icon = Res.drawable.settings_route,
                        title = stringResource(Res.string.ui_unykaty_gruntovykh_dorih),
                        description = stringResource(Res.string.ui_uvimkneno_polityka_avtoparku),
                    )
                    OrganizationPreferenceRow(
                        icon = Res.drawable.settings_gauge,
                        title = stringResource(Res.string.ui_limit_shvydkosti_avtoparku),
                        description = stringResource(Res.string.ui_90_km_hod_dorozhniy_limit_maye_priorytet),
                    )
                }
            }
            OfflineMapsCard(
                onClick = onOpenOfflineMaps,
            )
            PersonalPreferenceRow(
                icon = Res.drawable.diagnostics_settings,
                title = stringResource(Res.string.ui_prystroyi_ta_diahnostyka),
                description = stringResource(Res.string.ui_intehratsiyi_fonovi_dozvoly_gps),
                onClick = onOpenDeviceDiagnostics,
            )
            PersonalPreferenceRow(
                icon = Res.drawable.settings_lock,
                title = stringResource(Res.string.ui_tsentr_pryvatnosti),
                description = stringResource(Res.string.ui_osobysti_ta_robochi_dani),
                onClick = onOpenPrivacy,
            )
            LabelText(
                stringResource(Res.string.ui_apex_drive_1_0_kontsept_interfeysu),
                ApexSubtle,
                10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        PreferencesBottomNavigation(
            bottomInset = bottomInset,
            onOpenMap = onOpenMap,
            onOpenGarage = onOpenGarage,
            onOpenProfile = onOpenProfile,
            onUnavailableTab = { },
        )
    }
}

@Composable
private fun PreferencesHeader(onBack: () -> Unit, onSearch: () -> Unit) {
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
            LabelText("‹", ApexText, 32.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LabelText(stringResource(Res.string.ui_nalashtuvannya), ApexText, 26.sp, weight = FontWeight.Bold, condensed = true)
            LabelText(stringResource(Res.string.ui_vash_komfort_spilni_pravyla), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.vehicle_search),
            contentDescription = stringResource(Res.string.ui_poshuk_nalashtuvan),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onSearch),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
    }
}

@Composable
private fun OrganizationCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PreferencesCard)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.settings_building),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(stringResource(Res.string.ui_dnipro_logistics), ApexText, 14.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_robochyy_rezhym_rol_vodiy), ApexMuted, 10.sp)
        }
        LabelText("›", ApexMuted, 22.sp)
    }
}

@Composable
private fun PreferencesSectionHeading(title: String, trailing: String) {
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
private fun PersonalPreferenceRow(
    icon: DrawableResource,
    title: String,
    description: String,
    value: String? = null,
    toggle: Boolean? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(title, ApexText, 13.sp, weight = FontWeight.Medium)
            LabelText(description, ApexMuted, 10.sp)
        }
        if (value != null) {
            LabelText(value, ApexMuted, 11.sp)
            LabelText("›", ApexSubtle, 18.sp)
        }
        toggle?.let { PreferenceSwitch(enabled = it) }
    }
    if (title != stringResource(Res.string.ui_xr_navihatsiya)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 45.dp)
                .height(0.5.dp)
                .background(PreferencesDivider),
        )
    }
}

@Composable
private fun PreferenceSwitch(enabled: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 37.dp, height = 22.dp)
            .clip(CircleShape)
            .background(if (enabled) ApexRed else Color(0xFF303741)),
        contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(ApexText),
        )
    }
}

@Composable
private fun OrganizationPreferenceRow(
    icon: DrawableResource,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            colorFilter = ColorFilter.tint(PreferencesGold),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(title, ApexText, 13.sp, weight = FontWeight.Medium)
            LabelText(description, ApexMuted, 10.sp)
        }
        Image(
            painter = painterResource(Res.drawable.settings_lock),
            contentDescription = stringResource(Res.string.ui_vstanovleno_orhanizatsiyeyu),
            modifier = Modifier.size(15.dp),
            colorFilter = ColorFilter.tint(PreferencesGold),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 45.dp)
            .height(0.5.dp)
            .background(PreferencesDivider),
    )
}

@Composable
private fun OfflineMapsCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(61.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PreferencesCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.settings_download),
            contentDescription = null,
            modifier = Modifier.size(21.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_oflayn_mapy), ApexText, 13.sp, weight = FontWeight.Medium)
            LabelText(stringResource(Res.string.ui_kyyivska_oblast_248_mb), ApexMuted, 10.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PreferencesValueBackground)
                .padding(horizontal = 9.dp, vertical = 6.dp),
        ) {
            LabelText(stringResource(Res.string.ui_ne_pidklyucheno), ApexMuted, 10.sp, weight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PreferencesBottomNavigation(
    bottomInset: Dp,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenProfile: () -> Unit,
    onUnavailableTab: (String) -> Unit,
) {
    val tabs: List<Pair<DrawableResource, String>> = listOf(
        Res.drawable.nav_map to stringResource(Res.string.ui_mapa),
        Res.drawable.nav_car to stringResource(Res.string.ui_harazh),
        Res.drawable.nav_settings to stringResource(Res.string.ui_nalashtuvannya),
        Res.drawable.nav_profile to stringResource(Res.string.ui_profil),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ApexSurface)
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
                                2 -> Unit
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
                        colorFilter = ColorFilter.tint(if (index == 2) ApexRed else ApexMuted),
                    )
                    LabelText(
                        tab.second,
                        if (index == 2) ApexText else ApexMuted,
                        9.sp,
                        weight = if (index == 2) FontWeight.SemiBold else FontWeight.Normal,
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
