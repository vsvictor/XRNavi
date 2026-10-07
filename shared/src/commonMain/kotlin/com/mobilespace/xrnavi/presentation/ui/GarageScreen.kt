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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mobilespace.xrnavi.domain.VehicleId
import com.mobilespace.xrnavi.presentation.GarageViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.garage_add
import xrnavi.shared.generated.resources.garage_mustang
import xrnavi.shared.generated.resources.garage_shield_check
import xrnavi.shared.generated.resources.garage_truck
import xrnavi.shared.generated.resources.nav_car
import xrnavi.shared.generated.resources.nav_map
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.nav_settings

private val GarageChipBackground = Color(0xFF14191F)
private val GarageIconBackground = Color(0xFF20262E)
private val GarageGold = Color(0xFFE9BA76)

@Composable
internal fun GarageScreen(
    viewModel: GarageViewModel,
    onBackToMap: () -> Unit,
    onOpenVehicleProfile: () -> Unit,
    onAddVehicle: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
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
        GarageHeader(
            onBack = onBackToMap,
            onAddVehicle = onAddVehicle,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GarageCategories(
                selectedCategory = state.selectedCategory,
                onSelect = viewModel::selectCategory,
            )
            statusMessage?.let {
                LabelText(it, ApexMuted, 11.sp)
            }
            if (state.showPersonal) {
                GarageSectionHeading(stringResource(Res.string.ui_osobystyy_transport), stringResource(Res.string.ui_1_avto))
                SelectedMustangCard(
                    selected = state.selectedVehicle == VehicleId.Mustang,
                    onConfigure = {
                        if (viewModel.selectVehicle(VehicleId.Mustang)) onOpenVehicleProfile()
                    },
                )
            }
            if (state.showTruck || state.showSpecial) {
                GarageSectionHeading(stringResource(Res.string.ui_transport_orhanizatsiyi), stringResource(Res.string.ui_dnipro_logistics))
                if (state.showTruck) {
                    FleetVehicleCard(
                        type = stringResource(Res.string.ui_vantazhne_vid_orhanizatsiyi),
                        name = stringResource(Res.string.ui_man_tgx_avtovoz),
                        registration = stringResource(Res.string.ui_aa_4821_kkh_napivprychip),
                        specifications = stringResource(Res.string.ui_4_0_2_55_16_5_m_40_t),
                        selected = state.selectedVehicle == VehicleId.Man,
                        onClick = {
                            if (viewModel.selectVehicle(VehicleId.Man)) onOpenVehicleProfile()
                        },
                    )
                }
                if (state.showSpecial) {
                    FleetVehicleCard(
                        type = stringResource(Res.string.ui_spetstekhnika_vid_orhanizatsiyi),
                        name = stringResource(Res.string.ui_iveco_daily_evakuator),
                        registration = stringResource(Res.string.ui_ae_7310_rs_platforma),
                        specifications = stringResource(Res.string.ui_3_1_2_3_8_2_m_7_2_t),
                    )
                }
            }
            GaragePolicyNotice()
            PrimaryButton(stringResource(Res.string.ui_obraty_ford_mustang), enabled = true, onClick = {
                if (viewModel.selectVehicle(VehicleId.Mustang)) onBackToMap()
            })
        }
        GarageBottomNavigation(
            bottomInset = bottomInset,
            onSelectMap = onBackToMap,
            onOpenSettings = onOpenSettings,
            onOpenProfile = onOpenProfile,
        )
    }
}

@Composable
private fun GarageHeader(onBack: () -> Unit, onAddVehicle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart,
        ) {
            LabelText("‹", ApexText, 32.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            LabelText(stringResource(Res.string.ui_miy_harazh_2), ApexText, 26.sp, weight = FontWeight.Bold, condensed = true)
            LabelText(stringResource(Res.string.ui_oberit_transport_dlya_nastupnoyi_poyizdky), ApexMuted, 11.sp)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onAddVehicle),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.garage_add),
                contentDescription = stringResource(Res.string.ui_dodaty_transport),
                modifier = Modifier.size(19.dp),
                colorFilter = ColorFilter.tint(ApexText),
            )
        }
    }
}

@Composable
private fun GarageCategories(selectedCategory: Int, onSelect: (Int) -> Unit) {
    val categories = listOf(stringResource(Res.string.ui_usi), stringResource(Res.string.ui_lehkove), stringResource(Res.string.ui_vantazhne), stringResource(Res.string.ui_spetstekhnika))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GarageChipBackground)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        categories.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedCategory == index) Color(0xFF341B21) else GarageChipBackground)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                LabelText(
                    label,
                    if (selectedCategory == index) ApexLinkRed else ApexMuted,
                    10.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun GarageSectionHeading(title: String, trailing: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LabelText(title, ApexMuted, 11.sp, weight = FontWeight.SemiBold)
        LabelText(trailing, ApexSubtle, 10.sp)
    }
}

@Composable
private fun SelectedMustangCard(selected: Boolean, onConfigure: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, if (selected) ApexRed else ApexBorder), RoundedCornerShape(16.dp)),
    ) {
        Image(
            painter = painterResource(Res.drawable.garage_mustang),
            contentDescription = stringResource(Res.string.ui_ford_mustang_gt),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33090C10),
                            Color.Transparent,
                            Color(0xF2090C10),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF341B21))
                    .padding(horizontal = 9.dp, vertical = 6.dp),
            ) {
                LabelText(
                    stringResource(if (selected) Res.string.ui_obrano_osnovne_avto else Res.string.ui_osobystyy_transport),
                    ApexLinkRed, 10.sp, weight = FontWeight.SemiBold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                LabelText(stringResource(Res.string.ui_ford_mustang_gt), ApexText, 28.sp, weight = FontWeight.Bold, condensed = true)
                LabelText(stringResource(Res.string.ui_2024_5_0_v8_aa_1964_mt), ApexMuted, 10.sp)
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LabelText(stringResource(Res.string.ui_lehkovyy_1_4_m_1_8_t), ApexText, 11.sp)
                    LabelText(
                        stringResource(Res.string.ui_nalashtuvaty),
                        ApexMuted,
                        10.sp,
                        modifier = Modifier.clickable(onClick = onConfigure),
                    )
                }
            }
        }
    }
}

@Composable
private fun FleetVehicleCard(
    type: String,
    name: String,
    registration: String,
    specifications: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ApexSurface)
            .border(BorderStroke(1.dp, if (selected) ApexRed else ApexSurface), RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GarageIconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.garage_truck),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                colorFilter = ColorFilter.tint(GarageGold),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(type, GarageGold, 9.sp)
            LabelText(name, ApexText, 14.sp, weight = FontWeight.SemiBold)
            LabelText(registration, ApexMuted, 10.sp)
            LabelText(specifications, ApexMuted, 10.sp)
        }
    }
}

@Composable
private fun GaragePolicyNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ApexSurface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.garage_shield_check),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(17.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LabelText(stringResource(Res.string.ui_rezhym_zalezhyt_vid_transportu), ApexText, 11.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_mustang_osobysti_nalashtuvannya_tekhnika_kompaniyi_pravyla_avtop),
                ApexMuted,
                10.sp,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun GarageBottomNavigation(
    bottomInset: androidx.compose.ui.unit.Dp,
    onSelectMap: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val tabs = listOf(
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
                                0 -> onSelectMap()
                                1 -> Unit
                                2 -> onOpenSettings()
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
                        colorFilter = ColorFilter.tint(if (index == 1) ApexRed else ApexMuted),
                    )
                    LabelText(
                        tab.second,
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
