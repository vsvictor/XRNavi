package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.domain.DrivingContext
import com.mobilespace.xrnavi.domain.VehicleId
import com.mobilespace.xrnavi.domain.WorkTask

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.apex_navigation
import xrnavi.shared.generated.resources.apex_user
import xrnavi.shared.generated.resources.map_origin
import xrnavi.shared.generated.resources.map_river
import xrnavi.shared.generated.resources.map_road_casing
import xrnavi.shared.generated.resources.map_roads
import xrnavi.shared.generated.resources.map_route
import xrnavi.shared.generated.resources.map_route_halo
import xrnavi.shared.generated.resources.map_layers
import xrnavi.shared.generated.resources.map_locate
import xrnavi.shared.generated.resources.nearby_filter
import xrnavi.shared.generated.resources.mustang_thumbnail
import xrnavi.shared.generated.resources.nav_car
import xrnavi.shared.generated.resources.nav_map
import xrnavi.shared.generated.resources.nav_profile
import xrnavi.shared.generated.resources.nav_settings

private val MapBackground = Color(0xFF151C24)
private val MapGreen = Color(0xFF1E332F)
private val MapRed = Color(0xFFF45151)
private val MapGold = Color(0xFFE9BA76)
private val CategorySelected = Color(0xFF341B21)
private val CategoryIdle = Color(0xFF20262E)

@Composable
internal fun RouteScreen(
    viewModel: RouteViewModel,
    onOpenSettings: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenProfile: () -> Unit,
    onStartRoute: () -> Unit,
    onOpenParkingStop: () -> Unit,
    onOpenWorkTrip: () -> Unit,
    onOpenNearbyStops: () -> Unit,
    onOpenAddressSearch: () -> Unit,
    onOpenTripPlan: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NavigationEffects(viewModel) {
        when (it) {
            NavigationAction.Settings -> onOpenSettings()
            NavigationAction.Garage -> onOpenGarage()
            NavigationAction.Preferences -> onOpenPreferences()
            NavigationAction.Profile -> onOpenProfile()
            NavigationAction.StartPreview -> onStartRoute()
            NavigationAction.Parking -> onOpenParkingStop()
            NavigationAction.WorkTrip -> onOpenWorkTrip()
            NavigationAction.NearbyStops -> onOpenNearbyStops()
            NavigationAction.AddressSearch -> onOpenAddressSearch()
            NavigationAction.TripPlan -> onOpenTripPlan()
            else -> Unit
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
        MapHeader(state.context)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            RegionalRouteMap(
                origin = stringResource(Res.string.ui_kyyiv_moye_mistseznakhodzhennya),
                destination = state.destination,
                mapLayer = state.mapLayer,
                onSwap = viewModel::swap,
                onClearDestination = viewModel::clearDestination,
                onToggleLayers = viewModel::toggleLayers,
                onLocate = viewModel::locate,
                onParkingStop = { viewModel.navigate(NavigationAction.Parking) },
                onNearbyStops = { viewModel.navigate(NavigationAction.NearbyStops) },
                onEditDestination = { viewModel.navigate(NavigationAction.AddressSearch) },
            )
        }
        RouteSheet(
            vehicleType = state.vehicleType,
            vehicle = state.vehicle,
            workTask = state.workTask,
            selectedRoute = state.routingSettings.selectedRoute,
            onVehicleType = viewModel::selectVehicle,
            message = navigationNotice(state.notice),
            onStartRoute = viewModel::start,
            onRouteSettings = { viewModel.navigate(NavigationAction.Settings) },
            onOpenWorkTrip = { viewModel.navigate(NavigationAction.WorkTrip) },
            onOpenTripPlan = { viewModel.navigate(NavigationAction.TripPlan) },
        )
        BottomNavigation(
            selectedTab = 0,
            bottomInset = bottomInset,
            onSelect = {
                when (it) {
                    1 -> viewModel.navigate(NavigationAction.Garage)
                    2 -> viewModel.navigate(NavigationAction.Preferences)
                    3 -> viewModel.navigate(NavigationAction.Profile)
                }
            },
        )
    }
}

@Composable
private fun MapHeader(context: DrivingContext) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ApexBackground)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ApexRed),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.apex_navigation),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            LabelText(stringResource(Res.string.ui_apex), ApexText, 21.sp, weight = FontWeight.Bold, condensed = true)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.apex_user),
                contentDescription = null,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(7.dp))
            LabelText(stringResource(if (context == DrivingContext.Personal) Res.string.ui_osobystyy_rezhym else Res.string.ui_robochyy_rezhym_rol_vodiy), ApexMuted, 11.sp)
        }
    }
}

@Composable
private fun RegionalRouteMap(
    origin: String,
    destination: String,
    mapLayer: Int,
    onSwap: () -> Unit,
    onClearDestination: () -> Unit,
    onToggleLayers: () -> Unit,
    onLocate: () -> Unit,
    onParkingStop: () -> Unit,
    onNearbyStops: () -> Unit,
    onEditDestination: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (mapLayer == 1) Color(0xFF18232D) else MapBackground),
    ) {
        val sx = maxWidth / 402f
        val sy = maxHeight / 380f

        MapGraphic(Res.drawable.map_river, 315, 0, 84, 620, sx, sy)
        ParkBlock(15, 155, 110, 48, sx, sy)
        ParkBlock(100, 370, 82, 85, sx, sy)
        ParkBlock(275, 40, 55, 85, sx, sy)
        ParkBlock(265, 470, 75, 65, sx, sy)
        MapGraphic(Res.drawable.map_road_casing, 60, 100, 262.4f, 296.25f, sx, sy)
        MapGraphic(Res.drawable.map_roads, 60, 100, 262.4f, 296.25f, sx, sy)
        MapGraphic(Res.drawable.map_route_halo, 39, 164, 321, 194, sx, sy)
        MapGraphic(Res.drawable.map_route, 42.5f, 167.5f, 314, 187, sx, sy)
        RouteDurationBadge(sx, sy)

        MapLabel(stringResource(Res.string.ui_kyyiv), ApexText, 15.sp, 307, 137, sx, sy, FontWeight.Bold)
        MapLabel(stringResource(Res.string.ui_zhytomyr), ApexMuted, 12.sp, 223, 235, sx, sy)
        MapLabel(stringResource(Res.string.ui_rivne), ApexMuted, 12.sp, 110, 272, sx, sy)
        MapLabel(stringResource(Res.string.ui_lviv), ApexText, 15.sp, 17, 315, sx, sy, FontWeight.Bold)
        MapLabel(stringResource(Res.string.ui_m_06_e40_2), MapGold, 10.sp, 227, 290, sx, sy, FontWeight.SemiBold)

        Image(
            painter = painterResource(Res.drawable.map_origin),
            contentDescription = stringResource(Res.string.ui_pochatok_marshrutu),
            modifier = Modifier
                .offset(sx * 347, sy * 163)
                .size(sx * 14, sy * 14),
        )
        DestinationPin(
            modifier = Modifier
                .offset(sx * 36, sy * 343)
                .size(sx * 18, sy * 18),
        )
        Box(
            modifier = Modifier
                .offset(sx * 176, sy * 230)
                .size(sx * 36, sy * 36)
                .clip(RoundedCornerShape(9.dp))
                .background(ApexRed)
                .clickable(onClick = onParkingStop),
            contentAlignment = Alignment.Center,
        ) {
            LabelText("P", ApexText, 19.sp, weight = FontWeight.Bold)
        }

        RouteEntryCard(
            origin = origin,
            destination = destination,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(97.dp)
                .offset(y = sy * 10),
            onSwap = onSwap,
            onClearDestination = onClearDestination,
            onEditDestination = onEditDestination,
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapControl(Res.drawable.nearby_filter, onNearbyStops)
            MapControl(Res.drawable.map_layers, onToggleLayers)
            MapControl(Res.drawable.map_locate, onLocate)
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText("⊙", ApexText, 12.sp, weight = FontWeight.Bold)
            LabelText(stringResource(Res.string.ui_mapbox_brand_with_leading_space), ApexText, 10.sp, weight = FontWeight.SemiBold)
        }
        LabelText(
            stringResource(Res.string.ui_mapbox_openstreetmap),
            ApexMuted,
            8.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 7.dp),
        )
    }
}

@Composable
private fun RouteDurationBadge(sx: Dp, sy: Dp) {
    Box(
        modifier = Modifier
            .offset(sx * 165, sy * 310)
            .clip(RoundedCornerShape(8.dp))
            .background(ApexBackground)
            .border(1.dp, ApexRed, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        LabelText(stringResource(Res.string.ui_6_hod_20_khv), ApexText, 13.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun MapGraphic(
    resource: DrawableResource,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    sx: Dp,
    sy: Dp,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .offset(sx * x.toFloat(), sy * y.toFloat())
            .size(sx * width.toFloat(), sy * height.toFloat()),
    )
}

@Composable
private fun ParkBlock(x: Int, y: Int, width: Int, height: Int, sx: Dp, sy: Dp) {
    Box(
        modifier = Modifier
            .offset(sx * x, sy * y)
            .size(sx * width, sy * height)
            .clip(RoundedCornerShape(18.dp))
            .background(MapGreen),
    )
}

@Composable
private fun MapLabel(
    text: String,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    x: Int,
    y: Int,
    sx: Dp,
    sy: Dp,
    weight: FontWeight = FontWeight.Normal,
) {
    LabelText(
        text,
        color,
        size,
        modifier = Modifier.offset(sx * x, sy * y),
        weight = weight,
    )
}

@Composable
private fun DestinationPin(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MapRed)
            .padding(3.dp)
            .clip(CircleShape)
            .background(ApexText)
            .padding(3.dp)
            .clip(CircleShape)
            .background(MapRed),
    )
}

@Composable
private fun RouteEntryCard(
    origin: String,
    destination: String,
    modifier: Modifier,
    onSwap: () -> Unit,
    onClearDestination: () -> Unit,
    onEditDestination: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ApexSurface)
            .border(1.dp, ApexBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .border(2.dp, ApexText, CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            LabelText(
                origin,
                ApexText,
                13.sp,
                modifier = Modifier.weight(1f),
                weight = FontWeight.Normal,
            )
            LabelText("↕", ApexMuted, 18.sp, modifier = Modifier.clickable(onClick = onSwap))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(ApexBorder))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText("⌖", MapRed, 14.sp)
            Spacer(Modifier.width(10.dp))
            LabelText(
                if (destination.isBlank()) stringResource(Res.string.ui_kudy_pryamuyemo) else destination,
                if (destination.isBlank()) ApexMuted else ApexText,
                13.sp,
                modifier = Modifier.weight(1f).clickable(onClick = onEditDestination),
                weight = if (destination.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
            )
            if (destination.isNotBlank()) {
                LabelText("×", ApexSubtle, 18.sp, modifier = Modifier.clickable(onClick = onClearDestination))
            }
        }
    }
}

@Composable
private fun MapControl(icon: DrawableResource, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ApexSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun RouteSheet(
    vehicleType: Int,
    vehicle: VehicleId,
    workTask: WorkTask?,
    selectedRoute: Int,
    onVehicleType: (Int) -> Unit,
    message: String?,
    onStartRoute: () -> Unit,
    onRouteSettings: () -> Unit,
    onOpenWorkTrip: () -> Unit,
    onOpenTripPlan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(303.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(ApexSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(if (vehicle == VehicleId.Mustang) Res.drawable.mustang_thumbnail else Res.drawable.garage_truck),
                contentDescription = stringResource(if (vehicle == VehicleId.Mustang) Res.string.ui_ford_mustang_gt else Res.string.ui_man_tgx_avtovoz),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 94.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                LabelText(
                    stringResource(if (vehicle == VehicleId.Mustang) Res.string.ui_ford_mustang_gt else Res.string.ui_man_tgx_avtovoz),
                    ApexText,
                    23.sp,
                    weight = FontWeight.Bold,
                    condensed = true,
                )
                LabelText(stringResource(if (vehicle == VehicleId.Mustang) Res.string.ui_lehkovyy_1_4_m_1_8_t else Res.string.ui_dlya_man_tgx_16_5_m), ApexMuted, 10.sp)
            }
            LabelText("⌄", ApexMuted, 18.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            VehicleCategory(
                icon = "♧",
                label = stringResource(Res.string.ui_lehkove),
                selected = vehicleType == 0,
                modifier = Modifier.weight(1f),
                onClick = { onVehicleType(0) },
            )
            VehicleCategory(
                icon = "▰",
                label = stringResource(Res.string.ui_vantazhne),
                selected = vehicleType == 1,
                modifier = Modifier.weight(1f),
                onClick = { onVehicleType(1) },
            )
            VehicleCategory(
                icon = "⚒",
                label = stringResource(Res.string.ui_spetstekhnika),
                selected = vehicleType == 2,
                modifier = Modifier.weight(1f),
                onClick = { onVehicleType(2) },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LabelText(stringResource(if (selectedRoute == 1) Res.string.ui_7_58 else Res.string.ui_6_hod_20_khv), ApexText, 28.sp, weight = FontWeight.Bold, condensed = true)
                LabelText(stringResource(if (selectedRoute == 1) Res.string.ui_cherez_ternopil_594_km_48_khv else Res.string.ui_540_km), ApexMuted, 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LabelText("⊙", Color(0xFF85C6AC), 13.sp)
                Spacer(Modifier.width(5.dp))
                LabelText(stringResource(if (selectedRoute == 1) Res.string.ui_cherez_ternopil_594_km_48_khv else Res.string.ui_m_06_nayshvydshyy_prybuttya_o_16_01), ApexMuted, 11.sp)
            }
        }
        RouteActionButton(stringResource(Res.string.ui_pochaty_marshrut), onStartRoute)
        LabelText(
            message ?: stringResource(Res.string.ui_nalashtuvannya_marshrutu_ta_alternatyvy),
            if (message == null) ApexSubtle else ApexMuted,
            10.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRouteSettings),
            textAlign = TextAlign.Center,
        )
        LabelText(
            stringResource(Res.string.ui_kilka_zupynok_plan_marshrutu),
            ApexMuted,
            10.sp,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenTripPlan),
            textAlign = TextAlign.Center,
        )
        LabelText(stringResource(Res.string.ui_pryklad), ApexSubtle, 10.sp)
        if (workTask != null) LabelText(
            "${workTask.id}: ${workTask.destination}",
            ApexLinkRed,
            10.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenWorkTrip),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VehicleCategory(
    icon: String,
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) CategorySelected else CategoryIdle)
            .border(
                if (selected) 1.dp else 0.dp,
                if (selected) ApexRed else Color.Transparent,
                RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        LabelText(icon, if (selected) ApexRed else ApexMuted, 13.sp)
        Spacer(Modifier.width(5.dp))
        LabelText(label, if (selected) ApexText else ApexMuted, 10.sp)
    }
}

@Composable
private fun RouteActionButton(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ApexRed)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText("➤", ApexText, 19.sp)
        Spacer(Modifier.width(10.dp))
        LabelText(label, ApexText, 15.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun BottomNavigation(
    selectedTab: Int,
    bottomInset: Dp,
    onSelect: (Int) -> Unit,
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
                        .clickable { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Image(
                        painter = painterResource(tab.first),
                        contentDescription = null,
                        modifier = Modifier.size(21.dp),
                        colorFilter = ColorFilter.tint(
                            if (index == selectedTab) MapRed else ApexMuted,
                        ),
                    )
                    LabelText(
                        tab.second,
                        if (index == selectedTab) ApexText else ApexMuted,
                        9.sp,
                        weight = if (index == selectedTab) FontWeight.SemiBold else FontWeight.Normal,
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
