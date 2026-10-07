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
import com.mobilespace.xrnavi.presentation.FordConfigurationViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.garage_mustang
import xrnavi.shared.generated.resources.nav_car
import xrnavi.shared.generated.resources.nav_map
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.nav_settings

private val FordConfigurationCard = Color(0xFF14191F)
private val FordConfigurationOption = Color(0xFF341B21)
private val FordConfigurationGold = Color(0xFFE9BA76)

@Composable
internal fun FordConfigurationScreen(
    viewModel: FordConfigurationViewModel,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onValidateParameters: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message = vehicleMessage(state.message)
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val models = listOf(stringResource(Res.string.ui_mustang), stringResource(Res.string.ui_focus), stringResource(Res.string.ui_explorer))
    val years = listOf(stringResource(Res.string.ui_2022), stringResource(Res.string.ui_2023), stringResource(Res.string.ui_2024), stringResource(Res.string.ui_2025))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        FordConfigurationHeader(
            onBack = onBack,
            onHelp = viewModel::showHelp,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FordModelImage(modelName = models[state.modelIndex])
            FordSectionHeading(stringResource(Res.string.ui_model))
            FordOptionSelector(
                options = models,
                selectedIndex = state.modelIndex,
                onSelect = viewModel::selectModel,
            )
            FordSectionHeading(stringResource(Res.string.ui_rik_vypusku))
            FordOptionSelector(
                options = years,
                selectedIndex = state.yearIndex,
                onSelect = viewModel::selectYear,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FordSectionHeading(stringResource(Res.string.ui_komplektatsiya))
                LabelText(stringResource(Res.string.ui_obrano), ApexSubtle, 10.sp)
            }
            SelectedTrimCard(
                trimIndex = state.selectedTrim,
                catalogAvailable = state.catalogAvailable,
            )
            OtherTrimRow(
                trimIndex = state.selectedTrim,
                catalogAvailable = state.catalogAvailable,
                onClick = viewModel::nextTrim,
            )
            FordVerificationNotice()
            message?.let {
                LabelText(it, FordConfigurationGold, 10.sp, lineHeight = 14.sp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexRed)
                    .clickable {
                        if (viewModel.validateParameters()) onValidateParameters()
                    },
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabelText("→", ApexText, 19.sp, weight = FontWeight.SemiBold)
                LabelText(
                    stringResource(Res.string.ui_dali_pereviryty_parametry),
                    ApexText,
                    14.sp,
                    weight = FontWeight.SemiBold,
                )
            }
        }
        FordConfigurationBottomNavigation(
            bottomInset = bottomInset,
            onOpenMap = onOpenMap,
            onOpenSettings = onOpenSettings,
            onOpenProfile = onOpenProfile,
        )
    }
}

@Composable
private fun FordConfigurationHeader(onBack: () -> Unit, onHelp: () -> Unit) {
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
                .size(width = 22.dp, height = 44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart,
        ) {
            LabelText("‹", ApexText, 32.sp)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            LabelText(stringResource(Res.string.ui_vash_ford), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_lehkovyy_transport_krok_2_iz_3), ApexMuted, 11.sp)
        }
        LabelText(
            "ⓘ",
            ApexMuted,
            20.sp,
            modifier = Modifier.clickable(onClick = onHelp),
        )
    }
}

@Composable
private fun FordModelImage(modelName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        Image(
            painter = painterResource(Res.drawable.garage_mustang),
            contentDescription = stringResource(Res.string.ui_tymchasove_foto_ford_mustang_iz_harazha_vybrana_model_ford_1_s, modelName),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x44090C10),
                        0.5f to Color.Transparent,
                        1f to Color(0xE8090C10),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF20262E))
                .padding(horizontal = 9.dp, vertical = 6.dp),
        ) {
            LabelText(
                stringResource(Res.string.ui_foto_z_maketa_nedostupne_tymchasove_foto_z_harazha),
                ApexMuted,
                9.sp,
                weight = FontWeight.SemiBold,
            )
        }
        LabelText(
            if (modelName == stringResource(Res.string.ui_mustang)) {
                stringResource(Res.string.ui_ford_model_gt, modelName)
            } else {
                stringResource(Res.string.ui_ford_model, modelName)
            },
            ApexText,
            28.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            weight = FontWeight.SemiBold,
            condensed = true,
        )
    }
}

@Composable
private fun FordSectionHeading(title: String) {
    LabelText(title, ApexMuted, 11.sp, weight = FontWeight.SemiBold)
}

@Composable
private fun FordOptionSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FordConfigurationCard)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEachIndexed { index, option ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(35.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (index == selectedIndex) FordConfigurationOption else FordConfigurationCard)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                LabelText(
                    option,
                    if (index == selectedIndex) ApexLinkRed else ApexMuted,
                    11.sp,
                    weight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SelectedTrimCard(trimIndex: Int, catalogAvailable: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FordConfigurationOption)
            .border(BorderStroke(1.dp, ApexRed), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LabelText(
            if (!catalogAvailable) {
                stringResource(Res.string.ui_komplektatsiyu_ne_pidtverdzheno)
            } else if (trimIndex == 0) {
                stringResource(Res.string.ui_gt_5_0_v8_avtomatychna)
            } else {
                stringResource(Res.string.ui_ecoboost_2_3)
            },
            ApexText,
            13.sp,
            weight = FontWeight.SemiBold,
        )
        LabelText(
            if (catalogAvailable) {
                stringResource(Res.string.ui_kupe_benzyn_zadniy_pryvid)
            } else {
                stringResource(Res.string.ui_zvirte_dani_z_tekhpasportom)
            },
            ApexMuted,
            11.sp,
        )
    }
}

@Composable
private fun OtherTrimRow(
    trimIndex: Int,
    catalogAvailable: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(FordConfigurationCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(
                if (!catalogAvailable) {
                    stringResource(Res.string.ui_dani_komplektatsiy_nedostupni)
                } else if (trimIndex == 0) {
                    stringResource(Res.string.ui_ecoboost_2_3)
                } else {
                    stringResource(Res.string.ui_gt_5_0_v8_avtomatychna)
                },
                ApexText,
                13.sp,
                weight = FontWeight.SemiBold,
            )
            LabelText(
                if (catalogAvailable) stringResource(Res.string.ui_insha_komplektatsiya) else stringResource(Res.string.ui_kataloh_ne_pidklyucheno),
                ApexMuted,
                10.sp,
            )
        }
        LabelText("›", ApexSubtle, 20.sp)
    }
}

@Composable
private fun FordVerificationNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FordConfigurationCard)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        LabelText("ⓘ", ApexMuted, 18.sp)
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_perevirte_pered_zberezhennyam), ApexText, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_zobrazhennya_ne_pidtverdzhuye_komplektatsiyu_parametry_zvirte_z),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun FordConfigurationBottomNavigation(
    bottomInset: Dp,
    onOpenMap: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val destinations = listOf(
        Res.drawable.nav_map to stringResource(Res.string.ui_mapa),
        Res.drawable.nav_car to stringResource(Res.string.ui_harazh),
        Res.drawable.nav_settings to stringResource(Res.string.ui_nalashtuvannya),
        Res.drawable.nav_profile to stringResource(Res.string.ui_profil),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FordConfigurationCard)
            .border(BorderStroke(0.5.dp, ApexBorder)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(61.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEachIndexed { index, destination ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            when (index) {
                                0 -> onOpenMap()
                                2 -> onOpenSettings()
                                3 -> onOpenProfile()
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Image(
                        painter = painterResource(destination.first),
                        contentDescription = destination.second,
                        modifier = Modifier.size(21.dp),
                    )
                    LabelText(
                        destination.second,
                        if (index == 1) ApexText else ApexMuted,
                        9.sp,
                        weight = if (index == 1) FontWeight.SemiBold else FontWeight.Normal,
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
