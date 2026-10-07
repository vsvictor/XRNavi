package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.domain.*

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.nav_car
import xrnavi.shared.generated.resources.nav_map
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.nav_settings
import xrnavi.shared.generated.resources.privacy_chevron
import xrnavi.shared.generated.resources.privacy_download
import xrnavi.shared.generated.resources.privacy_lock
import xrnavi.shared.generated.resources.privacy_radio
import xrnavi.shared.generated.resources.privacy_trash
import xrnavi.shared.generated.resources.privacy_user_x
import xrnavi.shared.generated.resources.stop_help
import xrnavi.shared.generated.resources.stop_back

private val PrivacyCard = Color(0xFF14191F)
private val PrivacyField = Color(0xFF20262E)
private val PrivacyWarning = Color(0xFF30291F)
private val PrivacyGreen = Color(0xFF85C6AC)
private val PrivacyGold = Color(0xFFE9BA76)

@Composable
internal fun PrivacyScreen(
    viewModel: PrivacyViewModel,
    onBack: () -> Unit,
    onDeleteAccount: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val exportDescription = stringResource(Res.string.ui_profil_i_vlasni_poyizdky_otrymaty_kopiyu)
    val deleteHistoryDescription = stringResource(Res.string.ui_ne_vydalyaye_robochi_zapysy_orhanizatsiyi)
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val statusMessage = uiState.message?.let { stringResource(it) }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        PrivacyHeader(
            onBack = onBack,
            onHelp = {
                viewModel.help()
            },
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PersonalSpaceCard()
            PrivacySectionHeading(stringResource(Res.string.ui_robochi_reysy), stringResource(Res.string.ui_polityka_3_2))
            WorkLocationSetting()
            WorkPolicyNotice()
            PrivacySectionHeading(stringResource(Res.string.ui_zberihannya_danykh))
            RetentionCard()
            PrivacyActionRow(
                icon = Res.drawable.privacy_download,
                title = stringResource(Res.string.ui_eksportuvaty_osobysti_dani),
                description = exportDescription,
                onClick = {
                    viewModel.perform(PersonalDataAction.Export)
                },
            )
            PrivacyActionRow(
                icon = Res.drawable.privacy_trash,
                title = stringResource(Res.string.ui_vydalyty_osobystu_istoriyu),
                description = deleteHistoryDescription,
                onClick = {
                    viewModel.perform(PersonalDataAction.DeleteHistory)
                },
            )
            PrivacyActionRow(
                icon = Res.drawable.privacy_user_x,
                title = stringResource(Res.string.ui_vydalennya_akaunta),
                description = stringResource(Res.string.ui_naslidky_ta_pidtverdzhennya_na_nastupnomu_krotsi),
                titleColor = ApexLinkRed,
                onClick = onDeleteAccount,
            )
            statusMessage?.let {
                LabelText(it, ApexLinkRed, 11.sp, lineHeight = 15.sp)
            }
            LabelText(
                stringResource(Res.string.ui_zapyt_shchodo_robochykh_danykh_fleet_dnipro_logistics_ua),
                ApexSubtle,
                10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            LabelText(
                stringResource(Res.string.ui_polityky_y_dani_na_tsomu_ekrani_navedeni_yak_demonstratsiynyy_pr),
                ApexSubtle,
                10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        PrivacyBottomNavigation(
            bottomInset = bottomInset,
            onOpenMap = onOpenMap,
            onOpenGarage = onOpenGarage,
            onOpenSettings = onOpenSettings,
            onOpenProfile = onOpenProfile,
        )
    }
}

@Composable
private fun PrivacyHeader(onBack: () -> Unit, onHelp: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.stop_back),
            contentDescription = stringResource(Res.string.ui_nazad),
            modifier = Modifier.size(22.dp).clickable(onClick = onBack),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            LabelText(stringResource(Res.string.ui_tsentr_pryvatnosti), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_vashi_dani_zrozumili_mezhi_dostupu), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.stop_help),
            contentDescription = stringResource(Res.string.ui_dovidka),
            modifier = Modifier.size(20.dp).clickable(onClick = onHelp),
        )
    }
}

@Composable
private fun PersonalSpaceCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PrivacyCard).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.clip(RoundedCornerShape(8.dp)).background(PrivacyField)
                .padding(horizontal = 9.dp, vertical = 6.dp),
        ) {
            LabelText(stringResource(Res.string.ui_osobystyy_prostir), PrivacyGreen, 10.sp, weight = FontWeight.SemiBold)
        }
        LabelText(
            stringResource(Res.string.ui_osobysti_poyizdky_lyshe_vashi),
            ApexText,
            25.sp,
            weight = FontWeight.SemiBold,
            condensed = true,
            lineHeight = 31.sp,
        )
        LabelText(
            stringResource(Res.string.ui_mustang_istoriya_ta_marshruty_ne_peredayutsya_dnipro_logistics_r),
            ApexMuted,
            11.sp,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun PrivacySectionHeading(title: String, trailing: String? = null) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(title, ApexMuted, 11.sp, weight = FontWeight.SemiBold)
        trailing?.let { LabelText(it, ApexSubtle, 10.sp) }
    }
}

@Composable
private fun WorkLocationSetting() {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PrivacyCard)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrivacyIcon(Res.drawable.privacy_radio, null, Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_peredacha_robochoyi_heolokatsiyi), ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_lyshe_vid_startu_do_zavershennya_reysu), ApexMuted, 10.sp)
        }
        LabelText(stringResource(Res.string.ui_obov_yazkovo), PrivacyGold, 12.sp, weight = FontWeight.SemiBold)
        PrivacyIcon(Res.drawable.privacy_lock, stringResource(Res.string.ui_pravylo_zablokovano_politykoyu), Modifier.size(15.dp))
    }
}

@Composable
private fun WorkPolicyNotice() {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PrivacyWarning).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        PrivacyIcon(Res.drawable.privacy_lock, null, Modifier.size(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_tse_pravylo_orhanizatsiyi), PrivacyGold, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_pid_chas_dl_204_dyspetcher_bachyt_pozytsiyu_eta_ta_status_tut_ne),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun RetentionCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PrivacyCard).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LabelText(stringResource(Res.string.ui_robocha_heolokatsiya_30_dniv), ApexText, 13.sp, weight = FontWeight.SemiBold)
        LabelText(
            stringResource(Res.string.ui_za_nalashtuvannyam_dnipro_logistics_robochi_zvity_180_dniv_okrem),
            ApexMuted,
            11.sp,
            lineHeight = 15.sp,
        )
        LabelText(stringResource(Res.string.ui_osobysta_istoriya), ApexText, 13.sp, weight = FontWeight.SemiBold)
        LabelText(
            stringResource(Res.string.ui_zberihayetsya_u_vashomu_akaunti_doky_vy_yiyi_ne_vydalyte_oflayn),
            ApexMuted,
            11.sp,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun PrivacyActionRow(
    icon: DrawableResource,
    title: String,
    description: String,
    titleColor: Color = ApexText,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(PrivacyCard)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrivacyIcon(icon, null, Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(title, titleColor, 13.sp, weight = FontWeight.SemiBold)
            LabelText(description, ApexMuted, 10.sp)
        }
        PrivacyIcon(Res.drawable.privacy_chevron, null, Modifier.size(15.dp))
    }
}

@Composable
private fun PrivacyBottomNavigation(
    bottomInset: Dp,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val items = listOf(
        Triple(Res.drawable.nav_map, stringResource(Res.string.ui_mapa), onOpenMap),
        Triple(Res.drawable.nav_car, stringResource(Res.string.ui_harazh), onOpenGarage),
        Triple(Res.drawable.nav_settings, stringResource(Res.string.ui_nalashtuvannya), onOpenSettings),
        Triple(Res.drawable.nav_profile, stringResource(Res.string.ui_profil), onOpenProfile),
    )
    Column(
        Modifier.fillMaxWidth().background(PrivacyCard).border(0.5.dp, ApexBorder),
    ) {
        Row(
            Modifier.fillMaxWidth().height(61.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, (icon, label, action) ->
                Column(
                    Modifier.weight(1f).clickable(onClick = action),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = label,
                        modifier = Modifier.size(21.dp),
                    )
                    LabelText(label, if (index == 3) ApexText else ApexMuted, 9.sp)
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().height(maxOf(26.dp, bottomInset)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(width = 132.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp)).background(ApexText),
            )
        }
    }
}

@Composable
private fun PrivacyIcon(
    resource: DrawableResource,
    description: String?,
    modifier: Modifier,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = description,
        modifier = modifier,
    )
}
