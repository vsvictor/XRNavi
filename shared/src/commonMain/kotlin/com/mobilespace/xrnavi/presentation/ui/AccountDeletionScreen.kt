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
import com.mobilespace.xrnavi.presentation.AccountDeletionViewModel
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
import xrnavi.shared.generated.resources.account_check
import xrnavi.shared.generated.resources.account_chevron
import xrnavi.shared.generated.resources.account_clipboard
import xrnavi.shared.generated.resources.account_company
import xrnavi.shared.generated.resources.account_delete_user
import xrnavi.shared.generated.resources.account_download
import xrnavi.shared.generated.resources.account_trash
import xrnavi.shared.generated.resources.stop_back

private val DeletionCard = Color(0xFF14191F)
private val DeletionField = Color(0xFF20262E)
private val DeletionWarning = Color(0xFF30291F)
private val DeletionGold = Color(0xFFE9BA76)

@Composable
internal fun DeleteAccountScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AccountDeletionViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onBack, onCancel) {
        viewModel.effects.collect {
            when (it.destination) {
                FeatureDestination.Back -> onBack()
                FeatureDestination.Cancel -> onCancel()
                else -> Unit
            }
        }
    }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        DeletionHeader { viewModel.navigate(FeatureDestination.Back) }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DeletionIntro()
            PersonalDataLossCard()
            OrganizationDataCard()
            ExportDataRow(
                onClick = viewModel::exportAccountData,
            )
            ActiveTripWarning()
            Acknowledgement(
                checked = state.acknowledged,
                onToggle = viewModel::toggleAcknowledgement,
            )
            state.statusMessage?.let {
                LabelText(stringResource(it), ApexLinkRed, 12.sp, lineHeight = 16.sp)
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(ApexBackground)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = maxOf(12.dp, bottomInset)),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DeletionButton(
                label = if (state.isSubmitting) stringResource(Res.string.ui_nadsylannya) else stringResource(Res.string.ui_pidtverdyty_zapyt_na_vydalennya),
                destructive = true,
                enabled = state.acknowledged && !state.isSubmitting,
                onClick = viewModel::deleteAccount,
            )
            DeletionButton(
                label = stringResource(Res.string.ui_skasuvaty),
                destructive = false,
                enabled = !state.isSubmitting,
                onClick = { viewModel.navigate(FeatureDestination.Cancel) },
            )
            Box(
                Modifier.fillMaxWidth().height(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(width = 132.dp, height = 4.dp)
                        .clip(RoundedCornerShape(100.dp)).background(ApexText),
                )
            }
        }
    }
}

@Composable
private fun DeletionHeader(onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.stop_back),
            contentDescription = stringResource(Res.string.ui_nazad),
            modifier = Modifier.size(22.dp).clickable(onClick = onBack),
        )
    }
}

@Composable
private fun DeletionIntro() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF301D20))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            DeletionIcon(Res.drawable.account_delete_user, Modifier.size(17.dp))
            LabelText(stringResource(Res.string.ui_nezvorotna_diya), ApexLinkRed, 10.sp, weight = FontWeight.SemiBold)
        }
        LabelText(
            stringResource(Res.string.ui_vydalyty_akaunt),
            ApexText,
            29.sp,
            weight = FontWeight.SemiBold,
            condensed = true,
            lineHeight = 34.sp,
        )
        LabelText(
            stringResource(Res.string.ui_vy_vtratyte_dostup_do_apex_perevirte_yaki_dani_bude_vydaleno_per),
            ApexMuted,
            13.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun PersonalDataLossCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DeletionCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        LabelText(stringResource(Res.string.ui_osobystyy_akaunt), ApexMuted, 10.sp, weight = FontWeight.SemiBold)
        LabelText(stringResource(Res.string.ui_bude_vydaleno), ApexText, 15.sp, weight = FontWeight.SemiBold)
        DeletionConsequence(stringResource(Res.string.ui_profil_ta_osobysti_nalashtuvannya))
        DeletionConsequence(stringResource(Res.string.ui_istoriya_osobystykh_poyizdok_i_zberezheni_marshruty))
        DeletionConsequence(stringResource(Res.string.ui_aktyvni_seansy_na_vsikh_prystroyakh))
    }
}

@Composable
private fun DeletionConsequence(text: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeletionIcon(Res.drawable.account_trash, Modifier.size(17.dp))
        LabelText(text, ApexMuted, 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun OrganizationDataCard() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DeletionField)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        DeletionIcon(Res.drawable.account_company, Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_robochi_dani_zalyshatsya), DeletionGold, 13.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_zapysy_dnipro_logistics_ne_vydalyayutsya_razom_z_osobystym_akaun),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun ExportDataRow(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeletionCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeletionIcon(Res.drawable.account_download, Modifier.size(19.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            LabelText(stringResource(Res.string.ui_spershu_eksportuyte_svoyi_dani), ApexText, 12.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_otrymayte_kopiyu_osobystoyi_istoriyi), ApexMuted, 10.sp)
        }
        DeletionIcon(Res.drawable.account_chevron, Modifier.size(15.dp))
    }
}

@Composable
private fun ActiveTripWarning() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeletionWarning)
            .padding(11.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        DeletionIcon(Res.drawable.account_clipboard, Modifier.size(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_aktyvnyy_reys_dl_204), DeletionGold, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_vydalennya_akaunta_ne_skasuye_roboche_zavdannya_uzhodte_yoho_zav),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun Acknowledgement(checked: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (checked) ApexRed else Color.Transparent)
                .border(1.dp, if (checked) ApexRed else ApexBorder, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) DeletionIcon(Res.drawable.account_check, Modifier.size(14.dp))
        }
        LabelText(
            stringResource(Res.string.ui_ya_rozumiyu_naslidky_ta_khochu_nadislaty_zapyt_na_vydalennya_aka),
            ApexMuted,
            11.sp,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun DeletionButton(
    label: String,
    destructive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    !enabled -> ApexBorder
                    destructive -> ApexRed
                    else -> DeletionCard
                },
            )
            .then(if (destructive) Modifier else Modifier.border(1.dp, ApexBorder, RoundedCornerShape(10.dp)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        LabelText(
            label,
            if (enabled || !destructive) ApexText else ApexMuted,
            13.sp,
            weight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DeletionIcon(
    resource: DrawableResource,
    modifier: Modifier,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        modifier = modifier,
    )
}
