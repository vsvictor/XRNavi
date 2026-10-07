package com.mobilespace.xrnavi.presentation.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobilespace.xrnavi.domain.OfflineMapsAction
import com.mobilespace.xrnavi.presentation.OfflineMapsViewModel
import com.mobilespace.xrnavi.presentation.FeatureDestination
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.offline_alert
import xrnavi.shared.generated.resources.offline_back
import xrnavi.shared.generated.resources.offline_bottom_car
import xrnavi.shared.generated.resources.offline_bottom_map
import xrnavi.shared.generated.resources.offline_bottom_profile
import xrnavi.shared.generated.resources.offline_bottom_settings
import xrnavi.shared.generated.resources.offline_chevron
import xrnavi.shared.generated.resources.offline_download
import xrnavi.shared.generated.resources.offline_help
import xrnavi.shared.generated.resources.offline_map
import xrnavi.shared.generated.resources.offline_route

private val OfflineMapsCard = Color(0xFF14191F)
private val OfflineMapsTrack = Color(0xFF20262E)
private val OfflineMapsWarning = Color(0xFF30291F)
private val OfflineMapsGold = Color(0xFFE9BA76)
private val OfflineMapsGreen = Color(0xFF85C6AC)
private val OfflineMapsRed = Color(0xFFF45151)

@Composable
internal fun OfflineMapsScreen(
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: OfflineMapsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onBack, onOpenMap, onOpenGarage, onOpenSettings, onOpenProfile) {
        viewModel.effects.collect {
            when (it.destination) {
                FeatureDestination.Back -> onBack()
                FeatureDestination.Map -> onOpenMap()
                FeatureDestination.Garage -> onOpenGarage()
                FeatureDestination.Settings -> onOpenSettings()
                FeatureDestination.Profile -> onOpenProfile()
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
        OfflineMapsHeader(
            onBack = { viewModel.navigate(FeatureDestination.Back) },
            onHelp = viewModel::toggleHelp,
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
            OfflineStorageCard()
            OfflineSectionHeading()
            SavedRegionCard()
            StaleRegionCard(onUpdate = { viewModel.perform(OfflineMapsAction.UpdateRegion) })
            ActiveDownloadCard(
                onPause = { viewModel.perform(OfflineMapsAction.PauseDownload) },
                onCancel = { viewModel.perform(OfflineMapsAction.CancelDownload) },
                enabled = !state.isSubmitting,
            )
            OfflineMapsNotice(showHelp = state.showHelp)
            CachedRouteCard(onClick = { viewModel.perform(OfflineMapsAction.OpenCachedRoute) })
            state.statusMessage?.let {
                LabelText(stringResource(it), OfflineMapsGold, 10.sp, lineHeight = 14.sp)
            }
            DownloadRegionButton(
                enabled = !state.isSubmitting,
                onClick = { viewModel.perform(OfflineMapsAction.DownloadRegion) },
            )
        }
        OfflineMapsBottomNavigation(
            bottomInset = bottomInset,
            onOpenMap = { viewModel.navigate(FeatureDestination.Map) },
            onOpenGarage = { viewModel.navigate(FeatureDestination.Garage) },
            onOpenSettings = { viewModel.navigate(FeatureDestination.Settings) },
            onOpenProfile = { viewModel.navigate(FeatureDestination.Profile) },
        )
    }
}

@Composable
private fun OfflineMapsHeader(onBack: () -> Unit, onHelp: () -> Unit) {
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
            LabelText(stringResource(Res.string.ui_oflayn_mapy), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_zberezheni_rehiony_ta_kesh_marshrutu), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.offline_help),
            contentDescription = stringResource(Res.string.ui_dovidka_pro_oflayn_mapy),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onHelp),
        )
    }
}

@Composable
private fun OfflineStorageCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OfflineMapsCard)
            .border(BorderStroke(1.dp, OfflineMapsCard), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(stringResource(Res.string.ui_mapy_na_prystroyi), ApexMuted, 12.sp)
            LabelText(stringResource(Res.string.ui_1_01_hb_8_hb), ApexText, 14.sp, weight = FontWeight.SemiBold)
        }
        ProgressTrack(progress = 0.125f)
        LabelText(stringResource(Res.string.ui_vilno_na_prystroyi_6_99_hb_dlya_map), ApexMuted, 10.sp)
    }
}

@Composable
private fun OfflineSectionHeading() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(stringResource(Res.string.ui_rehiony), ApexMuted, 11.sp, weight = FontWeight.SemiBold)
        LabelText(stringResource(Res.string.ui_wi_fi_dlya_novykh_zavantazhen), ApexSubtle, 10.sp)
    }
}

@Composable
private fun SavedRegionCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OfflineMapsCard)
            .border(BorderStroke(1.dp, OfflineMapsCard), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.offline_map),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            LabelText(stringResource(Res.string.ui_kyyivska_oblast), ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_248_mb_onovleno_06_10_2026_08_50), ApexMuted, 10.sp)
        }
        OfflineStatusBadge(
            title = stringResource(Res.string.ui_hotovo),
            foreground = OfflineMapsGreen,
            background = OfflineMapsTrack,
        )
    }
}

@Composable
private fun StaleRegionCard(onUpdate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OfflineMapsCard)
            .border(BorderStroke(1.dp, OfflineMapsCard), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(
                stringResource(Res.string.ui_korydor_kyyiv_lviv),
                ApexText,
                13.sp,
                weight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            OfflineStatusBadge(
                title = stringResource(Res.string.ui_zastarilo),
                foreground = OfflineMapsGold,
                background = OfflineMapsWarning,
            )
        }
        LabelText(stringResource(Res.string.ui_612_mb_20_09_2026_onovlennya_86_mb), ApexMuted, 10.sp)
        LabelText(
            stringResource(Res.string.ui_onovyty_rehion),
            OfflineMapsRed,
            11.sp,
            modifier = Modifier.clickable(onClick = onUpdate),
        )
    }
}

@Composable
private fun ActiveDownloadCard(
    onPause: () -> Unit,
    onCancel: () -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OfflineMapsCard)
            .border(BorderStroke(1.dp, OfflineMapsCard), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(stringResource(Res.string.ui_lvivska_oblast), ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_42_2), OfflineMapsRed, 12.sp)
        }
        ProgressTrack(progress = 0.42f)
        LabelText(stringResource(Res.string.ui_151_iz_360_mb_pryblyzno_3_khv), ApexMuted, 10.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(
                stringResource(Res.string.ui_pryzupynyty),
                ApexText,
                11.sp,
                modifier = Modifier.clickable(enabled = enabled, onClick = onPause),
            )
            LabelText(
                stringResource(Res.string.ui_skasuvaty),
                ApexMuted,
                11.sp,
                modifier = Modifier.clickable(enabled = enabled, onClick = onCancel),
            )
        }
    }
}

@Composable
private fun ProgressTrack(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(OfflineMapsTrack),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ApexRed),
        )
    }
}

@Composable
private fun OfflineMapsNotice(showHelp: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(OfflineMapsWarning)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.offline_alert),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(OfflineMapsGold),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_mapa_oflayn_perebudova), OfflineMapsGold, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                if (showHelp) {
                    stringResource(Res.string.ui_zberezhenyy_marshrut_mozhna_perehlyadaty_novyy_marshrut_i_perebu_2)
                } else {
                    stringResource(Res.string.ui_zberezhenyy_marshrut_mozhna_perehlyadaty_novyy_marshrut_i_perebu)
                },
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun CachedRouteCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(OfflineMapsCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.offline_route),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(stringResource(Res.string.ui_kesh_aktyvnoho_marshrutu), ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_ford_mustang_kyyiv_lviv_6_mb_09_38), ApexMuted, 10.sp)
        }
        Image(
            painter = painterResource(Res.drawable.offline_chevron),
            contentDescription = stringResource(Res.string.ui_perehlyanuty_kesh_marshrutu),
            modifier = Modifier.size(15.dp),
            colorFilter = ColorFilter.tint(ApexSubtle),
        )
    }
}

@Composable
private fun DownloadRegionButton(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(OfflineMapsTrack)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.offline_download),
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            colorFilter = ColorFilter.tint(ApexText),
        )
        LabelText(stringResource(Res.string.ui_zavantazhyty_inshyy_rehion), ApexText, 14.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun OfflineStatusBadge(title: String, foreground: Color, background: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        LabelText(title, foreground, 10.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun OfflineMapsBottomNavigation(
    bottomInset: Dp,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val tabs: List<Triple<DrawableResource, String, () -> Unit>> = listOf(
        Triple(Res.drawable.offline_bottom_map, stringResource(Res.string.ui_mapa), onOpenMap),
        Triple(Res.drawable.offline_bottom_car, stringResource(Res.string.ui_harazh), onOpenGarage),
        Triple(Res.drawable.offline_bottom_settings, stringResource(Res.string.ui_nalashtuvannya), onOpenSettings),
        Triple(Res.drawable.offline_bottom_profile, stringResource(Res.string.ui_profil), onOpenProfile),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OfflineMapsCard)
            .border(BorderStroke(0.5.dp, ApexBorder)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(61.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, (icon, title, onClick) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = title,
                        modifier = Modifier.size(21.dp),
                        colorFilter = ColorFilter.tint(if (index == 2) ApexRed else ApexMuted),
                    )
                    LabelText(
                        title,
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
