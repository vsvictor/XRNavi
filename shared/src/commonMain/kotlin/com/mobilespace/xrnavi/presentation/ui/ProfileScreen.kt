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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.garage_mustang
import xrnavi.shared.generated.resources.nav_car
import xrnavi.shared.generated.resources.nav_map
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.nav_settings
import xrnavi.shared.generated.resources.profile_company
import xrnavi.shared.generated.resources.profile_edit
import xrnavi.shared.generated.resources.profile_portrait
import xrnavi.shared.generated.resources.profile_security
import xrnavi.shared.generated.resources.profile_verified

private val ProfileCardBackground = Color(0xFF14191F)
private val ProfileGreen = Color(0xFF85C6AC)
private val ProfileOrganizationText = Color(0xFFE9BA76)

@Composable
internal fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenOrganization: () -> Unit,
    onOpenTripHistory: () -> Unit,
    onOpenWorkTasks: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenInvitation: () -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val drivingContext = if (uiState.context == DrivingContext.Work) 1 else 0
    val message = uiState.message?.let { stringResource(it) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { onOpenInvitation() }
    }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        ProfileHeader(
            onBack = onBack,
            onEdit = {
                viewModel.edit()
            },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ProfileIdentity()
            DrivingContextSelector(
                selected = drivingContext,
                onSelect = {
viewModel.selectContext(it)
                },
            )
            message?.let {
                LabelText(it, ApexMuted, 11.sp)
            }
            PersonalGarageSection(
                isCorporate = drivingContext == 1,
                onManageGarage = onOpenGarage,
            )
            DrivingStatistics(
                onClick = onOpenTripHistory,
            )
            OrganizationSection(onClick = onOpenOrganization, member = uiState.organizationMember)
            if (drivingContext == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ProfileCardBackground)
                        .clickable(onClick = onOpenWorkTasks)
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabelText(stringResource(Res.string.ui_moyi_zavdannya_vodiya), ApexText, 13.sp, weight = FontWeight.SemiBold)
                    LabelText("›", ApexMuted, 20.sp)
                }
            }
            SecurityAction(
                onClick = onOpenPrivacy,
            )
        }
        ProfileBottomNavigation(
            bottomInset = bottomInset,
            onOpenMap = onOpenMap,
            onOpenGarage = onOpenGarage,
            onOpenPreferences = onOpenPreferences,
            onOpenProfile = { },
        )
    }
}

@Composable
private fun ProfileHeader(onBack: () -> Unit, onEdit: () -> Unit) {
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
            LabelText(stringResource(Res.string.ui_miy_profil), ApexText, 26.sp, weight = FontWeight.Bold, condensed = true)
            LabelText(stringResource(Res.string.ui_vodiy_potsinovuvach_dorohy), ApexMuted, 11.sp)
        }
        Image(
            painter = painterResource(Res.drawable.profile_edit),
            contentDescription = stringResource(Res.string.ui_redahuvaty_profil),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onEdit),
            colorFilter = ColorFilter.tint(ApexMuted),
        )
    }
}

@Composable
private fun ProfileIdentity() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.profile_portrait),
            contentDescription = stringResource(Res.string.ui_foto_profilyu_andriya_kovalya),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .border(BorderStroke(1.dp, ApexBorder), CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_andriy_koval), ApexText, 29.sp, weight = FontWeight.Bold, condensed = true)
            LabelText(stringResource(Res.string.ui_andrii_ukr_net), ApexMuted, 11.sp)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF20262E))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.profile_verified),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(ProfileGreen),
                )
                LabelText(stringResource(Res.string.ui_poshtu_pidtverdzheno), ProfileGreen, 10.sp, weight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DrivingContextSelector(selected: Int, onSelect: (Int) -> Unit) {
    val options: List<Pair<DrawableResource, String>> = listOf(
        Res.drawable.nav_profile to stringResource(Res.string.ui_osobystyy),
        Res.drawable.profile_company to stringResource(Res.string.ui_korporatyvnyy),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ProfileCardBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, (icon, label) ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (index == selected) Color(0xFF341B21) else Color.Transparent)
                    .clickable { onSelect(index) },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(1.dp))
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(if (index == selected) ApexRed else ApexMuted),
                )
                LabelText(
                    label,
                    if (index == selected) ApexText else ApexMuted,
                    11.sp,
                    weight = if (index == selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun PersonalGarageSection(
    isCorporate: Boolean,
    onManageGarage: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(if (isCorporate) stringResource(Res.string.ui_transport_orhanizatsiyi) else stringResource(Res.string.ui_miy_harazh), ApexMuted, 11.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_keruvaty),
                ApexSubtle,
                10.sp,
                modifier = Modifier.clickable(onClick = onManageGarage),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onManageGarage),
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
                            listOf(Color(0x33090C10), Color.Transparent, Color(0xF2090C10)),
                            startY = 0f,
                            endY = 700f,
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
                        if (isCorporate) stringResource(Res.string.ui_robochyy_transport) else stringResource(Res.string.ui_osnovne_avto),
                        ApexLinkRed,
                        10.sp,
                        weight = FontWeight.SemiBold,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LabelText(
                            if (isCorporate) stringResource(Res.string.ui_man_tgx_avtovoz) else stringResource(Res.string.ui_ford_mustang_gt),
                            ApexText,
                            26.sp,
                            weight = FontWeight.Bold,
                            condensed = true,
                        )
                        LabelText(
                            if (isCorporate) stringResource(Res.string.ui_aa_4821_kkh_dnipro_logistics) else stringResource(Res.string.ui_2024_5_0_v8_aa_1964_mt),
                            ApexMuted,
                            10.sp,
                        )
                    }
                    LabelText("›", ApexText, 25.sp)
                }
            }
        }
    }
}

@Composable
private fun DrivingStatistics(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(67.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ProfileCardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        DrivingMetric(stringResource(Res.string.ui_32), stringResource(Res.string.ui_poyizdky))
        DrivingMetric(stringResource(Res.string.ui_4_820), stringResource(Res.string.ui_km_za_misyats))
        DrivingMetric(stringResource(Res.string.ui_126), stringResource(Res.string.ui_hod_u_dorozi))
    }
}

@Composable
private fun DrivingMetric(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        LabelText(value, ApexText, 22.sp, weight = FontWeight.Bold, condensed = true)
        LabelText(label, ApexMuted, 9.sp)
    }
}

@Composable
private fun OrganizationSection(onClick: () -> Unit, member: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LabelText(stringResource(Res.string.ui_moya_orhanizatsiya), ApexMuted, 11.sp, weight = FontWeight.SemiBold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ProfileCardBackground)
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.profile_company),
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    colorFilter = ColorFilter.tint(ApexMuted),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    LabelText(stringResource(Res.string.ui_dnipro_logistics), ApexText, 13.sp, weight = FontWeight.SemiBold)
                    LabelText(
                        stringResource(if (member) Res.string.ui_vodiy_uchasnyk_z_bereznya_2025 else Res.string.ui_perehlyanuty_zaproshennya),
                        ApexMuted, 10.sp,
                    )
                }
                LabelText("›", ApexMuted, 22.sp)
            }
            LabelText(
                stringResource(Res.string.ui_pravyla_kompaniyi_diyut_lyshe_v_korporatyvnomu_rezhymi_osobysti),
                ProfileOrganizationText,
                10.sp,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun SecurityAction(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.profile_security),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(ApexMuted),
            )
            LabelText(stringResource(Res.string.ui_bezpeka_ta_osobysti_dani), ApexMuted, 12.sp)
        }
        LabelText("›", ApexMuted, 18.sp)
    }
}

@Composable
private fun ProfileBottomNavigation(
    bottomInset: Dp,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenPreferences: () -> Unit,
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
                                2 -> onOpenPreferences()
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
                        colorFilter = ColorFilter.tint(if (index == 3) ApexRed else ApexMuted),
                    )
                    LabelText(
                        tab.second,
                        if (index == 3) ApexText else ApexMuted,
                        9.sp,
                        weight = if (index == 3) FontWeight.SemiBold else FontWeight.Normal,
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
