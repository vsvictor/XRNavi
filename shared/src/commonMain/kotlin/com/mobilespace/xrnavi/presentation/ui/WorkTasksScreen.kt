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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobilespace.xrnavi.presentation.WorkTasksViewModel
import com.mobilespace.xrnavi.presentation.FeatureDestination
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
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.task_back
import xrnavi.shared.generated.resources.task_car
import xrnavi.shared.generated.resources.task_check
import xrnavi.shared.generated.resources.task_chevron
import xrnavi.shared.generated.resources.task_done
import xrnavi.shared.generated.resources.task_help
import xrnavi.shared.generated.resources.task_lock
import xrnavi.shared.generated.resources.task_map
import xrnavi.shared.generated.resources.task_radio
import xrnavi.shared.generated.resources.task_sliders
import xrnavi.shared.generated.resources.task_truck

private val TaskSurface = Color(0xFF14191F)
private val TaskField = Color(0xFF20262E)
private val TaskSelected = Color(0xFF341B21)
private val TaskGold = Color(0xFFE9BA76)
private val TaskGreen = Color(0xFF85C6AC)

@Composable
internal fun WorkTasksScreen(
    onBack: () -> Unit,
    onOpenTask: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: WorkTasksViewModel,
    onOpenTaskWithId: ((String?) -> Unit)? = null,
) {
    val localized_ui_ostannye_zavershene = stringResource(Res.string.ui_ostannye_zavershene)
    val localized_ui_zaversheni_zavdannya = stringResource(Res.string.ui_zaversheni_zavdannya)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, onBack, onOpenTask, onOpenTaskWithId, onOpenMap, onOpenGarage, onOpenSettings, onOpenProfile) {
        viewModel.effects.collect {
            when (it.destination) {
                FeatureDestination.Back -> onBack()
                FeatureDestination.Task -> if (onOpenTaskWithId != null) onOpenTaskWithId(it.taskId) else onOpenTask()
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
        Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        WorkTasksHeader(
            onBack = { viewModel.navigate(FeatureDestination.Back) },
            onHelp = viewModel::showHelp,
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
            WorkTaskTabs(selectedTab = state.selectedTab, onSelect = viewModel::selectTab)
            AssignedVehicleCard()

            if (state.selectedTab == 0) {
                ActiveTaskCard(
                    taskId = state.chosenTask?.id,
                    destination = state.chosenTask?.destination,
                    onOpenTask = viewModel::openTask,
                )
                NewTaskCard(
                    isSubmitting = state.isSubmitting,
                    onAccept = viewModel::acceptTask,
                    taskId = state.offeredTask?.id,
                    destination = state.offeredTask?.destination,
                )
                TaskSectionHeading(localized_ui_ostannye_zavershene)
                CompletedTaskRow(onClick = viewModel::openCompletedTask)
            } else {
                TaskSectionHeading(localized_ui_zaversheni_zavdannya)
                CompletedTaskRow(onClick = viewModel::openCompletedTask)
            }

            WorkLocationNotice()
            state.statusMessage?.let {
                LabelText(stringResource(it), ApexLinkRed, 11.sp, lineHeight = 15.sp)
            }
            LabelText(
                stringResource(Res.string.ui_dnipro_logistics_znimok_14_20_zminy_pislya_bezpechnoyi_zupynky),
                ApexSubtle,
                10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        WorkTasksBottomNavigation(
            bottomInset,
            { viewModel.navigate(FeatureDestination.Map) },
            { viewModel.navigate(FeatureDestination.Garage) },
            { viewModel.navigate(FeatureDestination.Settings) },
            { viewModel.navigate(FeatureDestination.Profile) },
        )
    }
}

@Composable
private fun WorkTasksHeader(onBack: () -> Unit, onHelp: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskIcon(Res.drawable.task_back, stringResource(Res.string.ui_nazad), Modifier.size(22.dp), onBack)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LabelText(stringResource(Res.string.ui_moyi_zavdannya), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
            LabelText(stringResource(Res.string.ui_andriy_koval_vodiy_7_zhovtnya), ApexMuted, 11.sp)
        }
        TaskIcon(Res.drawable.task_help, stringResource(Res.string.ui_dovidka), Modifier.size(20.dp), onHelp)
    }
}

@Composable
private fun WorkTaskTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TaskSurface).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        listOf(stringResource(Res.string.ui_aktyvni_2), stringResource(Res.string.ui_zaversheni_1)).forEachIndexed { index, label ->
            Box(
                Modifier.weight(1f).height(35.dp).clip(RoundedCornerShape(6.dp))
                    .background(if (selectedTab == index) TaskSelected else TaskSurface)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                LabelText(
                    label,
                    if (selectedTab == index) ApexLinkRed else ApexMuted,
                    11.sp,
                    weight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun AssignedVehicleCard() {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TaskSurface)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskIcon(Res.drawable.task_truck, null, Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_man_tgx_avtovoz), ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_aa_4821_kkh_pryznachenyy_transport), ApexMuted, 10.sp)
        }
        TaskIcon(Res.drawable.task_lock, stringResource(Res.string.ui_zakriplenyy_transport), Modifier.size(15.dp))
    }
}

@Composable
private fun ActiveTaskCard(taskId: String?, destination: String?, onOpenTask: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(TaskSelected)
            .border(1.dp, ApexRed, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenTask).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabelText(taskId ?: stringResource(Res.string.ui_dl_204_sohodni), ApexText, 12.sp, weight = FontWeight.SemiBold)
            TaskBadge(stringResource(Res.string.ui_u_dorozi_2), ApexRed, TaskSelected)
        }
        LabelText(destination ?: stringResource(Res.string.ui_kyyiv_lviv), ApexText, 26.sp, weight = FontWeight.SemiBold, condensed = true)
        LabelText(stringResource(Res.string.ui_6_avto_sklad_zakhid_17_00_18_00), ApexMuted, 11.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabelText(stringResource(Res.string.ui_eta_17_30), ApexText, 14.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_20_khv_do_planu), TaskGold, 11.sp)
        }
        LabelText(stringResource(Res.string.ui_vidkryty_zavdannya), ApexLinkRed, 11.sp)
    }
}

@Composable
private fun NewTaskCard(
    isSubmitting: Boolean,
    onAccept: () -> Unit,
    taskId: String?,
    destination: String?,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(TaskSurface)
            .border(1.dp, TaskSurface, RoundedCornerShape(16.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabelText(taskId ?: stringResource(Res.string.ui_dl_205_8_zhovtnya), ApexText, 12.sp, weight = FontWeight.SemiBold)
            TaskBadge(stringResource(Res.string.ui_nove), TaskGold, Color(0xFF30291F))
        }
        LabelText(destination ?: stringResource(Res.string.ui_lviv_kyyiv), ApexText, 24.sp, weight = FontWeight.SemiBold, condensed = true)
        LabelText(stringResource(Res.string.ui_4_avto_man_tgx_aa_4821_kkh), ApexMuted, 11.sp)
        LabelText(stringResource(Res.string.ui_zavantazhennya_08_00_09_00_terminal_zakhid), ApexText, 11.sp)
        Row(
            Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp))
                .background(if (isSubmitting) ApexSubtle else ApexRed)
                .clickable(enabled = !isSubmitting, onClick = onAccept),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TaskIcon(Res.drawable.task_check, null, Modifier.size(19.dp))
            Spacer(Modifier.width(10.dp))
            LabelText(
                if (isSubmitting) stringResource(Res.string.ui_pereviryayemo) else stringResource(Res.string.ui_pryynyaty_zavdannya),
                ApexText,
                14.sp,
                weight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TaskBadge(label: String, textColor: Color, background: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(background)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        LabelText(label, textColor, 10.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun TaskSectionHeading(label: String) {
    LabelText(label, ApexMuted, 11.sp, weight = FontWeight.SemiBold)
}

@Composable
private fun CompletedTaskRow(onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TaskSurface)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskIcon(Res.drawable.task_done, stringResource(Res.string.ui_zavershene_zavdannya), Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_dl_198_kyyiv_zhytomyr), ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_5_zhovtnya_vykonano_o_15_40_man_tgx), ApexMuted, 10.sp)
        }
        LabelText(stringResource(Res.string.ui_vykonano), ApexMuted, 12.sp, weight = FontWeight.SemiBold)
        TaskIcon(Res.drawable.task_chevron, null, Modifier.size(15.dp))
    }
}

@Composable
private fun WorkLocationNotice() {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TaskSurface).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        TaskIcon(Res.drawable.task_radio, null, Modifier.size(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(stringResource(Res.string.ui_robocha_heolokatsiya_peredayetsya), TaskGreen, 12.sp, weight = FontWeight.SemiBold)
            LabelText(
                stringResource(Res.string.ui_dlya_aktyvnoho_dl_204_dyspetcher_bachyt_pozytsiyu_ta_eta_nove_za),
                ApexMuted,
                11.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun WorkTasksBottomNavigation(
    bottomInset: Dp,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val items = listOf(
        Triple(Res.drawable.task_map, stringResource(Res.string.ui_mapa), onOpenMap),
        Triple(Res.drawable.task_car, stringResource(Res.string.ui_harazh), onOpenGarage),
        Triple(Res.drawable.task_sliders, stringResource(Res.string.ui_nalashtuvannya), onOpenSettings),
        Triple(Res.drawable.nav_profile, stringResource(Res.string.ui_profil), onOpenProfile),
    )
    Column(Modifier.fillMaxWidth().background(TaskSurface).border(0.5.dp, ApexBorder)) {
        Row(
            Modifier.fillMaxWidth().height(61.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, (icon, title, onClick) ->
                Column(
                    Modifier.weight(1f).clickable(onClick = onClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    TaskIcon(icon, title, Modifier.size(21.dp))
                    LabelText(
                        title,
                        if (index == 3) ApexText else ApexMuted,
                        9.sp,
                    )
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().height(maxOf(26.dp, bottomInset)),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(width = 132.dp, height = 4.dp).clip(RoundedCornerShape(100.dp)).background(ApexText))
        }
    }
}

@Composable
private fun TaskIcon(
    resource: DrawableResource,
    description: String?,
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = description,
        modifier = modifier.then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)),
    )
}
