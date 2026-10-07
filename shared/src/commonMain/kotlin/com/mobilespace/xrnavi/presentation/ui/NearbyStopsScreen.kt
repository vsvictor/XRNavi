package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.domain.StopCategory

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.nearby_border
import xrnavi.shared.generated.resources.nearby_charge
import xrnavi.shared.generated.resources.nearby_chevron
import xrnavi.shared.generated.resources.nearby_filter
import xrnavi.shared.generated.resources.nearby_fuel
import xrnavi.shared.generated.resources.nearby_nav_map
import xrnavi.shared.generated.resources.nearby_nav_profile
import xrnavi.shared.generated.resources.nearby_nav_settings
import xrnavi.shared.generated.resources.nearby_parking
import xrnavi.shared.generated.resources.nearby_position
import xrnavi.shared.generated.resources.nearby_rest
import xrnavi.shared.generated.resources.nearby_river
import xrnavi.shared.generated.resources.nearby_road_casing
import xrnavi.shared.generated.resources.nearby_road_network
import xrnavi.shared.generated.resources.nearby_route
import xrnavi.shared.generated.resources.nearby_route_halo
import xrnavi.shared.generated.resources.nearby_service
import xrnavi.shared.generated.resources.nearby_weight
import xrnavi.shared.generated.resources.nav_car

private val StopsMapBackground = Color(0xFF202D2D)
private val StopsMapGreen = Color(0xFF24413A)

@Composable
fun NearbyStopsScreen(
    viewModel: NearbyStopsViewModel,
    onBack: () -> Unit,
    onOpenParkingDetails: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NavigationEffects(viewModel) {
        when (it) {
            NavigationAction.Back -> onBack()
            NavigationAction.Parking -> onOpenParkingDetails()
            NavigationAction.Settings -> onOpenFilters()
            NavigationAction.Map -> onOpenMap()
            NavigationAction.Garage -> onOpenGarage()
            NavigationAction.Preferences -> onOpenSettings()
            NavigationAction.Profile -> onOpenProfile()
            else -> Unit
        }
    }
    val categories = listOf(
        stringResource(Res.string.ui_usi) to Res.drawable.nearby_parking,
        stringResource(Res.string.ui_vidpochynok) to Res.drawable.nearby_rest,
        stringResource(Res.string.ui_palne) to Res.drawable.nearby_fuel,
        stringResource(Res.string.ui_zaryadky) to Res.drawable.nearby_charge,
        stringResource(Res.string.ui_servis) to Res.drawable.nearby_service,
        stringResource(Res.string.ui_vahovi) to Res.drawable.nearby_weight,
        stringResource(Res.string.ui_kordon) to Res.drawable.nearby_border,
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("‹", color = ApexText, fontSize = 32.sp, modifier = Modifier.clickable { viewModel.navigate(NavigationAction.Back) })
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                LabelText(stringResource(Res.string.ui_zupynky_na_marshruti), ApexText, 20.sp, weight = FontWeight.Bold)
                LabelText(stringResource(Res.string.ui_znaydit_potribne_poruch_iz_marshrutom), ApexMuted, 12.sp)
            }
            Image(
                painter = painterResource(Res.drawable.nearby_filter),
                contentDescription = stringResource(Res.string.ui_filtry),
                modifier = Modifier.size(28.dp).clickable { viewModel.navigate(NavigationAction.Settings) },
            )
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            categories.forEachIndexed { index, (title, icon) ->
                StopCategoryChip(title, icon, state.category == StopCategory.entries[index]) {
                    viewModel.selectCategory(StopCategory.entries[index])
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StopQuickFilter(stringResource(Res.string.ui_avtopoyizd_16_5_m), state.truckFilter, viewModel::toggleTruckFilter)
            StopQuickFilter(stringResource(Res.string.ui_do_5_km), state.distanceFilter, viewModel::toggleDistanceFilter)
        }
        Spacer(Modifier.height(12.dp))

        Box(Modifier.weight(1f).fillMaxWidth()) {
            StopsMap(state.stops.isNotEmpty()) { viewModel.navigate(NavigationAction.Parking) }
            Row(
                Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 14.dp)
                    .clip(RoundedCornerShape(12.dp)).background(ApexSurface).padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabelText(stringResource(Res.string.ui_m_06_e40), ApexText, 12.sp, weight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                LabelText(stringResource(Res.string.ui_rivne_2), ApexMuted, 11.sp)
            }
            Image(
                painter = painterResource(Res.drawable.nearby_position),
                contentDescription = stringResource(Res.string.ui_moye_mistseznakhodzhennya),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp, bottom = 110.dp)
                    .size(42.dp).clickable(onClick = viewModel::locate),
            )
            if (state.stops.isNotEmpty()) Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(ApexSurface).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelText(stringResource(Res.string.ui_nayblyzhcha_stoyanka), ApexText, 16.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(Res.drawable.nearby_chevron),
                        contentDescription = stringResource(Res.string.ui_vidkryty_detali_stoyanky),
                        modifier = Modifier.size(23.dp).clickable { viewModel.navigate(NavigationAction.Parking) },
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                            .background(ApexRed.copy(alpha = .17f)),
                        contentAlignment = Alignment.Center,
                    ) { LabelText("P", ApexRed, 20.sp, weight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        LabelText(stringResource(Res.string.ui_stoyanka_m_06), ApexText, 14.sp, weight = FontWeight.SemiBold)
                        LabelText(stringResource(Res.string.ui_bilya_rivnoho_2_4_km_vid_marshrutu), ApexMuted, 11.sp)
                    }
                    LabelText(stringResource(Res.string.ui_pryklad), ApexMuted, 10.sp)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(stringResource(Res.string.ui_16_5_m_2), stringResource(Res.string.ui_do_5_km), stringResource(Res.string.ui_bilya_marshrutu)).forEach { StopInfoPill(it) }
                }
                Spacer(Modifier.height(10.dp))
                LabelText(
                    stringResource(Res.string.ui_pryklady_ob_yektiv_faktychna_nayavnist_i_prydatnist_ne_perevirya),
                    ApexMuted,
                    10.sp,
                )
            }
            if (state.stops.isEmpty()) LabelText(
                stringResource(Res.string.ui_pryklady_ob_yektiv_faktychna_nayavnist_i_prydatnist_ne_perevirya),
                ApexMuted, 11.sp, modifier = Modifier.align(Alignment.BottomCenter).background(ApexSurface).padding(16.dp),
            )
            navigationNotice(state.notice)?.let {
                LabelText(it, ApexMuted, 11.sp, modifier = Modifier.align(Alignment.TopCenter).background(ApexSurface).padding(16.dp))
            }
        }

        Row(
            Modifier.fillMaxWidth().background(ApexSurface).padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StopBottomItem(Res.drawable.nearby_nav_map, stringResource(Res.string.ui_mapa), true) {
                viewModel.navigate(NavigationAction.Map)
            }
            StopBottomItem(Res.drawable.nav_car, stringResource(Res.string.ui_avto), false) {
                viewModel.navigate(NavigationAction.Garage)
            }
            StopBottomItem(Res.drawable.nearby_nav_settings, stringResource(Res.string.ui_nalashtuvannya), false) {
                viewModel.navigate(NavigationAction.Preferences)
            }
            StopBottomItem(Res.drawable.nearby_nav_profile, stringResource(Res.string.ui_profil), false) {
                viewModel.navigate(NavigationAction.Profile)
            }
        }
    }
}

@Composable
private fun StopCategoryChip(title: String, icon: DrawableResource, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(12.dp))
            .background(if (selected) ApexRed.copy(alpha = .15f) else ApexSurface)
            .border(1.dp, if (selected) ApexRed else ApexBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        LabelText(title, if (selected) ApexText else ApexMuted, 11.sp, weight = FontWeight.Medium)
    }
}

@Composable
private fun StopQuickFilter(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.clip(CircleShape).background(if (selected) ApexText else ApexSurface)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    ) { LabelText(title, if (selected) ApexBackground else ApexText, 11.sp, weight = FontWeight.Medium) }
}

@Composable
private fun StopInfoPill(text: String) {
    Box(Modifier.clip(CircleShape).background(ApexBackground).padding(horizontal = 9.dp, vertical = 6.dp)) {
        LabelText(text, ApexMuted, 10.sp)
    }
}

@Composable
private fun StopsMap(showParking: Boolean, onOpenParkingDetails: () -> Unit) {
    Box(Modifier.fillMaxSize().background(StopsMapBackground)) {
        Image(
            painter = painterResource(Res.drawable.nearby_river),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(4) {
                Box(Modifier.fillMaxSize().weight(1f).padding(5.dp).clip(RoundedCornerShape(20.dp)).background(StopsMapGreen))
            }
        }
        Image(
            painter = painterResource(Res.drawable.nearby_road_casing),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(Res.drawable.nearby_road_network),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(Res.drawable.nearby_route_halo),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(Res.drawable.nearby_route),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        StopMapLabel(stringResource(Res.string.ui_kyyiv), Alignment.TopEnd, 38.dp, 100.dp)
        StopMapLabel(stringResource(Res.string.ui_zhytomyr), Alignment.Center, 15.dp, 20.dp)
        StopMapLabel(stringResource(Res.string.ui_rivne), Alignment.CenterStart, 90.dp, 45.dp)
        StopMapLabel(stringResource(Res.string.ui_lviv), Alignment.BottomStart, 25.dp, 36.dp)
        if (showParking) StopMapPin(Modifier.align(Alignment.Center).offsetStops(x = 4.dp, y = 28.dp), onClick = onOpenParkingDetails)
    }
}

private fun Modifier.offsetStops(x: androidx.compose.ui.unit.Dp, y: androidx.compose.ui.unit.Dp): Modifier =
    this.then(Modifier.padding(start = x, top = y))

@Composable
private fun BoxScope.StopMapLabel(text: String, alignment: Alignment, horizontal: androidx.compose.ui.unit.Dp, vertical: androidx.compose.ui.unit.Dp) {
    LabelText(
        text,
        ApexText,
        12.sp,
        weight = FontWeight.SemiBold,
        modifier = Modifier.align(alignment).padding(horizontal = horizontal, vertical = vertical),
    )
}

@Composable
private fun StopMapPin(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(ApexRed)
            .border(2.dp, ApexText.copy(alpha = .85f), RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { LabelText("P", ApexText, 15.sp, weight = FontWeight.Bold) }
}

@Composable
private fun RowScope.StopBottomItem(icon: DrawableResource, title: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(21.dp))
        Spacer(Modifier.height(4.dp))
        LabelText(title, if (selected) ApexRed else ApexMuted, 10.sp)
    }
}
