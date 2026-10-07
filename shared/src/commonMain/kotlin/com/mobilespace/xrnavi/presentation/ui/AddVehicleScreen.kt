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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mobilespace.xrnavi.domain.MakeId
import com.mobilespace.xrnavi.domain.VehicleMake
import com.mobilespace.xrnavi.presentation.AddVehicleViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.garage_truck
import xrnavi.shared.generated.resources.nav_car
import xrnavi.shared.generated.resources.nav_map
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.nav_settings
import xrnavi.shared.generated.resources.profile_help
import xrnavi.shared.generated.resources.vehicle_chevron
import xrnavi.shared.generated.resources.vehicle_pencil
import xrnavi.shared.generated.resources.vehicle_search

private val AddVehiclePanel = Color(0xFF14191F)
private val AddVehicleField = Color(0xFF20262E)
private val AddVehicleSelected = Color(0xFF341B21)

@Composable
internal fun AddVehicleScreen(
    viewModel: AddVehicleViewModel,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onSelectFord: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val statusMessage = vehicleMessage(state.message)
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeInset = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        AddVehicleHeader(
            onBack = onBack,
            onHelp = viewModel::toggleHelp,
        )
        if (state.helpVisible) {
            LabelText(
                stringResource(Res.string.ui_tse_obmezhenyy_pryklad_katalohu_pered_dodavannyam_zvirte_marku_m),
                ApexMuted,
                11.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = maxOf(20.dp, imeInset)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VehicleSearchField(
                value = state.query,
                onValueChange = viewModel::search,
            )
            AddVehicleSectionLabel(stringResource(Res.string.ui_klas_transportu))
            VehicleClassSelector(
                selected = state.vehicleClass,
                onSelect = viewModel::selectClass,
            )
            SpecialVehicleClasses(
                selected = state.specialClass,
                onSelect = viewModel::selectSpecialClass,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AddVehicleSectionLabel(stringResource(Res.string.ui_oberit_marku))
                LabelText(
                    if (state.vehicleClass == 0) stringResource(Res.string.ui_lehkovi_pryklady_marok) else stringResource(Res.string.ui_kataloh_obmezhenyy),
                    ApexSubtle,
                    10.sp,
                )
            }
            if (state.makes.isNotEmpty()) {
                VehicleMakeList(
                    makes = state.makes,
                    selectedMake = state.selectedMake,
                    onChoose = { brand ->
                        if (viewModel.chooseMake(brand)) onSelectFord()
                    },
                )
            } else {
                AddVehicleEmptyState(
                    text = if (state.vehicleClass == 0) {
                        stringResource(Res.string.ui_za_tsym_zapytom_marky_abo_modeli_ne_znaydeno_sered_prykladiv_kat)
                    } else {
                        stringResource(Res.string.ui_kataloh_vantazhnoho_ta_spetsialnoho_transportu_ne_pidklyucheno_d)
                    },
                )
            }
            VehicleCatalogueNotice()
            ManualVehicleEntry(
                onClick = viewModel::manualEntry,
            )
            LabelText(
                statusMessage ?: stringResource(Res.string.ui_marky_modeli_y_parametry_potribno_zviryty_z_dokumentamy_pidklyuc),
                if (statusMessage == null) ApexSubtle else ApexMuted,
                10.sp,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 13.5.sp,
                textAlign = TextAlign.Center,
            )
        }
        AddVehicleBottomNavigation(
            bottomInset = bottomInset,
            onOpenMap = onOpenMap,
            onOpenGarage = onOpenGarage,
            onOpenSettings = onOpenSettings,
            onOpenProfile = onOpenProfile,
        )
    }
}

@Composable
private fun AddVehicleHeader(onBack: () -> Unit, onHelp: () -> Unit) {
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            LabelText(stringResource(Res.string.ui_dodaty_transport), ApexText, 23.sp, weight = FontWeight.Bold, condensed = true)
            LabelText(stringResource(Res.string.ui_harazh_krok_1_iz_3), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.profile_help),
            contentDescription = stringResource(Res.string.ui_dovidka),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onHelp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
    }
}

@Composable
private fun VehicleSearchField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AddVehicleField)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.vehicle_search),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                LabelText(stringResource(Res.string.ui_poshuk_marky_abo_modeli), ApexMuted, 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                textStyle = TextStyle(color = ApexText, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(ApexRed),
            )
        }
    }
}

@Composable
private fun AddVehicleSectionLabel(text: String) {
    LabelText(text, ApexMuted, 11.sp, weight = FontWeight.SemiBold)
}

@Composable
private fun VehicleClassSelector(selected: Int, onSelect: (Int) -> Unit) {
    val classes = listOf(stringResource(Res.string.ui_lehkove), stringResource(Res.string.ui_vantazhne), stringResource(Res.string.ui_spetstekhnika))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AddVehiclePanel)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        classes.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(35.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected == index) AddVehicleSelected else Color.Transparent)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                LabelText(
                    label,
                    if (selected == index) ApexLinkRed else ApexMuted,
                    11.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SpecialVehicleClasses(selected: Int, onSelect: (Int) -> Unit) {
    val types = listOf(stringResource(Res.string.ui_evakuator), stringResource(Res.string.ui_avtovoz), stringResource(Res.string.ui_tyahach))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        types.forEachIndexed { index, label ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected == index) AddVehicleSelected else AddVehiclePanel)
                    .clickable { onSelect(index) },
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(1.dp))
                Image(
                    painter = painterResource(Res.drawable.garage_truck),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(if (selected == index) ApexLinkRed else ApexMuted),
                )
                LabelText(
                    label,
                    if (selected == index) ApexLinkRed else ApexMuted,
                    10.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun VehicleMakeList(
    makes: List<VehicleMake>,
    selectedMake: MakeId?,
    onChoose: (MakeId) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AddVehiclePanel)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        makes.forEach { make ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onChoose(make.id) }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    LabelText(
                        vehicleMakeName(make.id),
                        if (make.id == MakeId.Ford || selectedMake == make.id) ApexLinkRed else ApexText,
                        13.sp,
                        weight = FontWeight.SemiBold,
                    )
                    LabelText(vehicleMakeModels(make.id), ApexMuted, 10.sp)
                }
                if (make.id == MakeId.Ford) {
                    LabelText(stringResource(Res.string.ui_obraty), ApexMuted, 12.sp, weight = FontWeight.SemiBold)
                }
                Image(
                    painter = painterResource(Res.drawable.vehicle_chevron),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    colorFilter = ColorFilter.tint(ApexSubtle),
                )
            }
        }
    }
}

@Composable
private fun AddVehicleEmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AddVehiclePanel)
            .padding(horizontal = 14.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        LabelText(text, ApexMuted, 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun VehicleCatalogueNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AddVehiclePanel)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(Res.drawable.profile_help),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_ne_znayshly_sviy_transport), ApexText, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_dovidnyk_maye_obmezhene_pokryttya_vantazhnu_ta_spetsialnu_tekhni),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun ManualVehicleEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AddVehicleField)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.vehicle_pencil),
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            colorFilter = ColorFilter.tint(ApexText),
        )
        Spacer(Modifier.width(10.dp))
        LabelText(stringResource(Res.string.ui_vvesty_transport_vruchnu), ApexText, 14.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun AddVehicleBottomNavigation(
    bottomInset: androidx.compose.ui.unit.Dp,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
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
