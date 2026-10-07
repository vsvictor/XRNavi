package com.mobilespace.xrnavi.presentation.ui

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
import androidx.compose.foundation.layout.defaultMinSize
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
import com.mobilespace.xrnavi.presentation.OrganizationInvitationViewModel
import com.mobilespace.xrnavi.presentation.FeatureDestination
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.invitation_back
import xrnavi.shared.generated.resources.invitation_check
import xrnavi.shared.generated.resources.invitation_company
import xrnavi.shared.generated.resources.invitation_join
import xrnavi.shared.generated.resources.invitation_policy
import xrnavi.shared.generated.resources.invitation_private
import xrnavi.shared.generated.resources.invitation_settings
import xrnavi.shared.generated.resources.invitation_shield
import xrnavi.shared.generated.resources.invitation_verified

private val InvitationSurface = Color(0xFF14191F)
private val InvitationField = Color(0xFF20262E)
private val InvitationGold = Color(0xFFE9BA76)
private val InvitationGreen = Color(0xFF85C6AC)

@Composable
internal fun OrganizationInvitationScreen(
    onBack: () -> Unit,
    onDecline: () -> Unit,
    onAccepted: () -> Unit,
    viewModel: OrganizationInvitationViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onBack, onDecline, onAccepted) {
        viewModel.effects.collect {
            when (it.destination) {
                FeatureDestination.Back -> onBack()
                FeatureDestination.Decline -> onDecline()
                FeatureDestination.Accepted -> onAccepted()
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
        InvitationHeader(
            onBack = { viewModel.navigate(FeatureDestination.Back) },
            onPrivacy = viewModel::showPrivacy,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = maxOf(20.dp, bottomInset + 16.dp)),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            InvitationCodeCard()
            OrganizationInvitationCard()
            InvitationSectionHeading()
            InvitationOwnershipCard(
                icon = Res.drawable.invitation_policy,
                title = stringResource(Res.string.ui_orhanizatsiya_keruye),
                description =
                    stringResource(Res.string.ui_habarytamy_robochoyi_tekhniky_dozvolenymy_dorohamy_ta_limitom_90),
                background = Color(0xFF30291F),
                titleColor = InvitationGold,
            )
            InvitationOwnershipCard(
                icon = Res.drawable.invitation_settings,
                title = stringResource(Res.string.ui_vy_nalashtovuyete),
                description =
                    stringResource(Res.string.ui_movu_holosovi_pidkazky_vyhlyad_mapy_ta_spovishchennya_mustang_i),
                background = InvitationSurface,
                titleColor = ApexText,
            )
            InvitationPrivacyCard()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = viewModel::toggleConsent)
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (state.consentGiven) ApexRed else InvitationField)
                        .then(
                            if (state.consentGiven) Modifier
                            else Modifier.border(1.dp, ApexBorder, RoundedCornerShape(6.dp)),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.consentGiven) {
                        Image(
                            painter = painterResource(Res.drawable.invitation_check),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                LabelText(
                    stringResource(Res.string.ui_oznayomyvsya_z_pravylamy_orhanizatsiyi_ta_umovamy_peredachi_dany),
                    ApexMuted,
                    11.sp,
                    modifier = Modifier.weight(1f),
                    lineHeight = 15.5.sp,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexRed)
                    .clickable(enabled = !state.isSubmitting, onClick = viewModel::accept),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.invitation_join),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
                LabelText(
                    if (state.isSubmitting) stringResource(Res.string.ui_obrobka_zaproshennya) else stringResource(Res.string.ui_pryynyaty_zaproshennya),
                    ApexText,
                    15.sp,
                    weight = FontWeight.SemiBold,
                )
            }
            state.statusMessage?.let {
                LabelText(stringResource(it), ApexMuted, 11.sp, lineHeight = 15.sp)
            }
            LabelText(
                stringResource(Res.string.ui_ne_zaraz_zalyshytysya_v_osobystomu_rezhymi),
                ApexMuted,
                12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigate(FeatureDestination.Decline) }
                    .padding(vertical = 2.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InvitationHeader(onBack: () -> Unit, onPrivacy: () -> Unit) {
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
            Image(
                painter = painterResource(Res.drawable.invitation_back),
                contentDescription = stringResource(Res.string.ui_nazad),
                modifier = Modifier.size(22.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            LabelText(
                stringResource(Res.string.ui_pryyednatysya_do_komandy),
                ApexText,
                26.sp,
                weight = FontWeight.Bold,
                condensed = true,
                lineHeight = 29.sp,
            )
            LabelText(stringResource(Res.string.ui_robochi_reysy_vash_osobystyy_prostir), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.invitation_shield),
            contentDescription = stringResource(Res.string.ui_informatsiya_pro_pryvatnist),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onPrivacy),
        )
    }
}

@Composable
private fun InvitationCodeCard() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(stringResource(Res.string.ui_kod_zaproshennya), ApexMuted, 11.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_vid_administratora), ApexSubtle, 10.sp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(InvitationField)
                .border(1.dp, ApexBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(stringResource(Res.string.ui_dnl_4821), ApexText, 17.sp, weight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(InvitationField)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.invitation_verified),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
                LabelText(stringResource(Res.string.ui_perevireno), InvitationGreen, 10.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun OrganizationInvitationCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(InvitationSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(InvitationField),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.invitation_company),
                    contentDescription = stringResource(Res.string.ui_dnipro_logistics),
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                LabelText(
                    stringResource(Res.string.ui_dnipro_logistics),
                    ApexText,
                    25.sp,
                    weight = FontWeight.Bold,
                    condensed = true,
                    lineHeight = 28.sp,
                )
                LabelText(stringResource(Res.string.ui_zaproshennya_na_rol_vodiy), ApexMuted, 11.sp)
            }
        }
        LabelText(
            stringResource(
                Res.string.ui_vas_zaproshuye_olena_shevchenko_dyspetcher_zaproshennya_dlya_1_s,
                stringResource(Res.string.ui_andrii_ukr_net),
            ),
            ApexMuted,
            11.sp,
            lineHeight = 15.5.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(InvitationField)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
            ) {
                LabelText(stringResource(Res.string.ui_polityka_avtoparku_3_2), ApexMuted, 10.sp, weight = FontWeight.SemiBold)
            }
            LabelText(stringResource(Res.string.ui_diye_do_09_10_2026), ApexSubtle, 10.sp)
        }
    }
}

@Composable
private fun InvitationSectionHeading() {
    LabelText(stringResource(Res.string.ui_shcho_zminytsya_pislya_pryyednannya), ApexMuted, 11.sp, weight = FontWeight.SemiBold)
}

@Composable
private fun InvitationOwnershipCard(
    icon: DrawableResource,
    title: String,
    description: String,
    background: Color,
    titleColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 74.dp)
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
            LabelText(description, ApexMuted, 11.sp, lineHeight = 15.5.sp)
        }
    }
}

@Composable
private fun InvitationPrivacyCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 69.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(InvitationSurface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.invitation_private),
            contentDescription = null,
            modifier = Modifier.size(17.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_osobysti_marshruty_pryvatni), ApexText, 11.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_kompaniya_bachyt_heolokatsiyu_ta_zvity_lyshe_pid_chas_robochoho),
                ApexMuted,
                10.sp,
                lineHeight = 14.sp,
            )
        }
    }
}
