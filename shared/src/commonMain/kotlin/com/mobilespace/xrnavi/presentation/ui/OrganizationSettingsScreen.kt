package com.mobilespace.xrnavi.presentation.ui

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.profile_company
import xrnavi.shared.generated.resources.profile_verified
import xrnavi.shared.generated.resources.settings_bell
import xrnavi.shared.generated.resources.settings_gauge
import xrnavi.shared.generated.resources.settings_lock
import xrnavi.shared.generated.resources.settings_route
import xrnavi.shared.generated.resources.settings_volume
import xrnavi.shared.generated.resources.team_dispatcher
import xrnavi.shared.generated.resources.team_message
import xrnavi.shared.generated.resources.team_send
import xrnavi.shared.generated.resources.garage_truck

private val CompanySettingsCard = Color(0xFF14191F)
private val CompanySettingsDivider = Color(0xFF303741)
private val CompanySettingsGold = Color(0xFFE9BA76)

@Composable
internal fun OrganizationSettingsScreen(
    viewModel: OrganizationSettingsViewModel, onBack: () -> Unit,
    onOpenWorkTasks: () -> Unit,
) {
    val localized_ui_profili_tekhniky_ta_habaryty =
        stringResource(Res.string.ui_profili_tekhniky_ta_habaryty)
    val localized_ui_man_tgx_4_0_2_55_16_5_m_40_t =
        stringResource(Res.string.ui_man_tgx_4_0_2_55_16_5_m_40_t)
    val localized_ui_dozvoleni_dorohy = stringResource(Res.string.ui_dozvoleni_dorohy)
    val localized_ui_bez_gruntovykh_dorih_obkhid_obmezhen =
        stringResource(Res.string.ui_bez_gruntovykh_dorih_obkhid_obmezhen)
    val localized_ui_maksymalna_shvydkist = stringResource(Res.string.ui_maksymalna_shvydkist)
    val localized_ui_90_km_hod_nyzhchyy_dorozhniy_limit_vazhlyvishyy =
        stringResource(Res.string.ui_90_km_hod_nyzhchyy_dorozhniy_limit_vazhlyvishyy)
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val dispatchNotifications = uiState.preferences.dispatchNotifications
    val voiceGuidance = uiState.preferences.voiceEnabled
    val statusMessage = uiState.message?.let { stringResource(it) }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        OrganizationSettingsHeader(onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OrganizationPolicySummary()
            if (uiState.workContext) {
                PrimaryButton(
                    stringResource(Res.string.ui_moyi_zavdannya_vodiya),
                    enabled = true,
                    onClick = onOpenWorkTasks
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OrganizationSettingsSectionHeading(
                    stringResource(Res.string.ui_pravyla_orhanizatsiyi),
                    stringResource(Res.string.ui_zminyuye_administrator)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CompanySettingsCard),
                ) {
                    LockedOrganizationSetting(
                        icon = Res.drawable.garage_truck,
                        title = localized_ui_profili_tekhniky_ta_habaryty,
                        description = localized_ui_man_tgx_4_0_2_55_16_5_m_40_t,
                        onClick = { viewModel.dimensions() },
                    )
                    LockedOrganizationSetting(
                        icon = Res.drawable.settings_route,
                        title = localized_ui_dozvoleni_dorohy,
                        description = localized_ui_bez_gruntovykh_dorih_obkhid_obmezhen,
                        onClick = { viewModel.roads() },
                    )
                    LockedOrganizationSetting(
                        icon = Res.drawable.settings_gauge,
                        title = localized_ui_maksymalna_shvydkist,
                        description = localized_ui_90_km_hod_nyzhchyy_dorozhniy_limit_vazhlyvishyy,
                        onClick = { viewModel.speed() },
                        showDivider = false,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OrganizationSettingsSectionHeading(
                    stringResource(Res.string.ui_vashi_nalashtuvannya),
                    stringResource(Res.string.ui_mozhna_zminyuvaty)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CompanySettingsCard),
                ) {
                    EditableOrganizationPreference(
                        icon = Res.drawable.settings_bell,
                        title = stringResource(Res.string.ui_spovishchennya_dyspetchera),
                        description = stringResource(Res.string.ui_novi_reysy_ta_zminy_zavdan),
                        enabled = dispatchNotifications,
                        onClick = {
                            viewModel.toggleNotifications()
                        },
                    )
                    EditableOrganizationPreference(
                        icon = Res.drawable.settings_volume,
                        title = stringResource(Res.string.ui_holosovi_pidkazky),
                        description = stringResource(Res.string.ui_ukrayinska_olena),
                        enabled = voiceGuidance,
                        onClick = {
                            viewModel.toggleVoice()
                        },
                        showDivider = false,
                    )
                }
            }
            DispatcherContact(
                onClick = {
                    viewModel.perform(OrganizationAction.MessageDispatcher)
                },
            )
            statusMessage?.let {
                LabelText(it, ApexMuted, 11.sp, lineHeight = 14.sp)
            }
            RequestPolicyChange(
                onClick = {
                    viewModel.perform(OrganizationAction.RequestPolicyChange)
                },
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LabelText(
                    stringResource(Res.string.ui_heolokatsiya_peredayetsya_lyshe_pid_chas_robochoho_reysu_osobyst),
                    ApexMuted,
                    10.sp,
                    lineHeight = 13.5.sp,
                    textAlign = TextAlign.Center,
                )
                LabelText(
                    stringResource(Res.string.ui_synkhronizovano_sohodni_o_09_38),
                    ApexSubtle,
                    9.sp
                )
            }
        }
    }
}

@Composable
private fun OrganizationSettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 22.dp, height = 44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart,
        ) {
            LabelText("‹", ApexText, 32.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LabelText(
                stringResource(Res.string.ui_nalashtuvannya_kompaniyi),
                ApexText,
                24.sp,
                weight = FontWeight.Bold,
                condensed = true,
            )
            LabelText(stringResource(Res.string.ui_dnipro_logistics_rol_vodiy), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.settings_lock),
            contentDescription = stringResource(Res.string.ui_korporatyvna_polityka),
            modifier = Modifier.size(21.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
    }
}

@Composable
private fun OrganizationPolicySummary() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CompanySettingsCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.profile_company),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(ApexMuted),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                LabelText(
                    stringResource(Res.string.ui_dnipro_logistics),
                    ApexText,
                    14.sp,
                    weight = FontWeight.SemiBold
                )
                LabelText(
                    stringResource(Res.string.ui_polityka_avtoparku_versiya_3_2),
                    ApexMuted,
                    10.sp
                )
            }
            Image(
                painter = painterResource(Res.drawable.profile_verified),
                contentDescription = stringResource(Res.string.ui_polityku_pidtverdzheno),
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(Color(0xFF85C6AC)),
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF30291F))
                .padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.settings_lock),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                colorFilter = ColorFilter.tint(CompanySettingsGold),
            )
            LabelText(
                stringResource(Res.string.ui_uspadkovano_lyshe_dlya_chytannya),
                CompanySettingsGold,
                10.sp,
                weight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OrganizationSettingsSectionHeading(title: String, trailing: String) {
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
private fun LockedOrganizationSetting(
    icon: DrawableResource,
    title: String,
    description: String,
    onClick: () -> Unit,
    showDivider: Boolean = true,
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
            colorFilter = ColorFilter.tint(CompanySettingsGold),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(title, ApexText, 13.sp, weight = FontWeight.Medium)
            LabelText(description, ApexMuted, 10.sp)
        }
        Image(
            painter = painterResource(Res.drawable.settings_lock),
            contentDescription = stringResource(Res.string.ui_lyshe_administrator_mozhe_zminyty),
            modifier = Modifier.size(15.dp),
            colorFilter = ColorFilter.tint(CompanySettingsGold),
        )
    }
    if (showDivider) {
        SettingsRowDivider()
    }
}

@Composable
private fun EditableOrganizationPreference(
    icon: DrawableResource,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true,
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(title, ApexText, 13.sp, weight = FontWeight.Medium)
            LabelText(description, ApexMuted, 10.sp)
        }
        OrganizationSwitch(enabled)
    }
    if (showDivider) {
        SettingsRowDivider()
    }
}

@Composable
private fun OrganizationSwitch(enabled: Boolean) {
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
private fun SettingsRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 45.dp)
            .height(0.5.dp)
            .background(CompanySettingsDivider),
    )
}

@Composable
private fun DispatcherContact(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CompanySettingsCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.team_dispatcher),
            contentDescription = null,
            modifier = Modifier.size(21.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            LabelText(
                stringResource(Res.string.ui_olena_shevchenko_dyspetcher),
                ApexText,
                12.sp,
                weight = FontWeight.Medium
            )
            LabelText(stringResource(Res.string.ui_fleet_dnipro_logistics_ua), ApexMuted, 10.sp)
        }
        Image(
            painter = painterResource(Res.drawable.team_message),
            contentDescription = stringResource(Res.string.ui_napysaty_dyspetcheru),
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(ApexRed),
        )
    }
}

@Composable
private fun RequestPolicyChange(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF20262E))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.team_send),
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            colorFilter = ColorFilter.tint(ApexText),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.width(10.dp))
        LabelText(
            stringResource(Res.string.ui_zaprosyty_zminu_parametriv),
            ApexText,
            15.sp,
            weight = FontWeight.SemiBold
        )
    }
}
