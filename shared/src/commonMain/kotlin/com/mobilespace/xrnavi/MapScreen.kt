package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import kotlin.math.abs
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MapScreen(
    preview: Boolean,
    demoMode: Boolean,
    origin: String,
    destination: String,
    routeStops: List<RouteStop>,
    sheetLevel: SheetLevel,
    geoStatus: GeoStatus,
    notice: String,
    vehicleClass: String,
    selectedVehicle: DemoVehicleProfile?,
    vehicleMode: String,
    onVehicleClassChange: (String) -> Unit,
    onOpenSearch: (SearchTarget) -> Unit,
    onOpenRouteStops: () -> Unit,
    onOpenStopPlaces: () -> Unit,
    onUseLocation: () -> Unit,
    onSheetLevelChange: (SheetLevel) -> Unit,
    onPreview: () -> Unit,
    onStartRoute: () -> Unit,
    onOpenRouteSettings: () -> Unit,
    onBack: () -> Unit,
    onSectionChange: (DemoSection) -> Unit,
    onToggleTheme: () -> Unit,
    onOpenAdditional: ((AdditionalScreen) -> Unit)? = null,
    sheetContent: (@Composable ColumnScope.() -> Unit)? = null,
    onMapLayers: () -> Unit = {}
) {
    val effectivePreview = preview && routeStops.isEmpty()
    val window = LocalWindowInfo.current.containerSize
    val landscape = window.width > window.height
    val tablet = with(LocalDensity.current) { minOf(window.width, window.height).toDp() >= 600.dp }
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        MapTopBar(
            demoMode = demoMode,
            preview = effectivePreview,
            vehicleMode = vehicleMode,
            onBack = onBack,
            onOpenStopPlaces = onOpenStopPlaces,
            onToggleTheme = onToggleTheme,
            onOpenExplanation = onOpenAdditional?.let { open ->
                { open(AdditionalScreen.RouteExplanation) }
            }
        )
        Row(Modifier.weight(1f)) {
            if (landscape) BottomNavigation(
                DemoSection.Map,
                onSectionChange,
                rail = true,
                compactRail = !tablet,
                accountStyle = true
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                val layoutWidth = maxWidth
                val layoutHeight = maxHeight
                val splitMap = landscape && layoutWidth >= 600.dp
                val showInlineAttribution =
                    layoutHeight < 500.dp && sheetLevel != SheetLevel.Collapsed
                val mapHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
                val density = LocalDensity.current
                val anchors = calculateMapSheetAnchors(
                    mapHeightPx = mapHeightPx,
                    density = density.density,
                    fontScale = density.fontScale,
                    inlineAttribution = showInlineAttribution
                )
                val minHeightPx = anchors.collapsed
                val partialHeightPx = anchors.partial
                val maxHeightPx = anchors.expanded
                var sheetHeightPx by remember(mapHeightPx) {
                    mutableFloatStateOf(anchors.heightFor(sheetLevel))
                }
                val sheetHeightDp =
                    if (splitMap) layoutHeight else with(density) { sheetHeightPx.toDp() }
                val sheetScrollState = rememberScrollState()

                LaunchedEffect(sheetLevel, minHeightPx, partialHeightPx, maxHeightPx) {
                    sheetHeightPx = anchors.heightFor(sheetLevel)
                }

                val scrollConnection =
                    remember(mapHeightPx, minHeightPx, partialHeightPx, maxHeightPx) {
                        object : NestedScrollConnection {
                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                if (available.y >= 0f || sheetHeightPx >= maxHeightPx) {
                                    return Offset.Zero
                                }
                                val consumed = minOf(-available.y, maxHeightPx - sheetHeightPx)
                                sheetHeightPx += consumed
                                return Offset(0f, -consumed)
                            }

                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                if (
                                    available.y <= 0f ||
                                    sheetScrollState.value != 0 ||
                                    sheetHeightPx <= minHeightPx
                                ) {
                                    return Offset.Zero
                                }
                                val used = minOf(available.y, sheetHeightPx - minHeightPx)
                                sheetHeightPx -= used
                                return Offset(0f, used)
                            }

                            override suspend fun onPreFling(available: Velocity): Velocity {
                                val anchors = listOf(
                                    SheetLevel.Collapsed to minHeightPx,
                                    SheetLevel.Partial to partialHeightPx,
                                    SheetLevel.Expanded to maxHeightPx
                                )
                                val target = when {
                                    available.y < -500f -> anchors.firstOrNull {
                                        it.second > sheetHeightPx + 1f
                                    }?.first

                                    available.y > 500f && sheetScrollState.value == 0 -> anchors
                                        .lastOrNull { it.second < sheetHeightPx - 1f }
                                        ?.first

                                    else -> anchors.minByOrNull {
                                        abs(it.second - sheetHeightPx)
                                    }?.first
                                }
                                if (target != null) {
                                    onSheetLevelChange(target)
                                    return Velocity(0f, available.y)
                                }
                                return Velocity.Zero
                            }
                        }
                    }

                Box(modifier = Modifier.fillMaxSize()) {
                    MapCanvas(
                        modifier = Modifier.fillMaxWidth(
                            if (splitMap) 0.54f else 1f
                        ).fillMaxHeight().then(
                            if (onOpenAdditional != null) Modifier.pointerInput(onOpenAdditional) {
                                detectTapGestures(onLongPress = {
                                    onOpenAdditional(AdditionalScreen.SelectedPoint)
                                })
                            } else Modifier
                        ),
                        preview = effectivePreview,
                        compact = splitMap
                    )
                    RouteEntryCard(
                        origin = origin,
                        destination = destination,
                        routeStopsCount = routeStops.size,
                        onOriginClick = { onOpenSearch(SearchTarget.Origin) },
                        onDestinationClick = { onOpenSearch(SearchTarget.Destination) },
                        onLocationClick = onUseLocation,
                        onRouteStopsClick = onOpenRouteStops,
                        modifier = Modifier
                            .align(if (splitMap) Alignment.TopStart else Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .widthIn(max = if (splitMap) layoutWidth * 0.54f else 620.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(
                                top = layoutHeight * 0.63f,
                                end = if (splitMap) layoutWidth * 0.46f + 12.dp else 20.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MapControlButton(
                            iconLight = Res.drawable.map_layers_light,
                            iconDark = Res.drawable.map_layers_dark,
                            label = "Шари мапи",
                            onClick = onMapLayers
                        )
                        MapControlButton(
                            iconLight = Res.drawable.map_locate_light,
                            iconDark = Res.drawable.map_locate_dark,
                            label = "Моє місцезнаходження",
                            onClick = onUseLocation
                        )
                    }
                    if (!showInlineAttribution) {
                        Text(
                            text = if (layoutWidth >= 600.dp) {
                                "Демо-мапа · Mapbox / OpenStreetMap не підключені"
                            } else {
                                "Демо-мапа · Mapbox /\nOpenStreetMap не підключені"
                            },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .widthIn(
                                    max = if (layoutWidth >= 600.dp) {
                                        minOf(480.dp, layoutWidth * 0.4f)
                                    } else {
                                        148.dp
                                    }
                                )
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    bottom = if (splitMap) 4.dp else sheetHeightDp + 4.dp
                                )
                                .semantics {
                                    contentDescription =
                                        "Демонстраційна мапа. Mapbox і OpenStreetMap не підключені."
                                },
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RouteBottomSheet(
                        preview = effectivePreview,
                        origin = origin,
                        destination = destination,
                        routeStops = routeStops,
                        geoStatus = geoStatus,
                        notice = notice,
                        level = sheetLevel,
                        showInlineAttribution = showInlineAttribution,
                        vehicleClass = vehicleClass,
                        selectedVehicle = selectedVehicle,
                        onVehicleClassChange = onVehicleClassChange,
                        sheetHeightDp = sheetHeightDp,
                        scrollConnection = scrollConnection,
                        scrollState = sheetScrollState,
                        minHeightPx = minHeightPx,
                        partialHeightPx = partialHeightPx,
                        maxHeightPx = maxHeightPx,
                        currentHeightPx = { sheetHeightPx },
                        updateHeight = { sheetHeightPx = it },
                        onLevelChange = onSheetLevelChange,
                        onOpenSearch = onOpenSearch,
                        onOpenRouteStops = onOpenRouteStops,
                        onPreview = onPreview,
                        onStartRoute = onStartRoute,
                        onOpenRouteSettings = onOpenRouteSettings,
                        sheetContent = sheetContent,
                        fixedPane = splitMap,
                        modifier = Modifier
                            .align(if (splitMap) Alignment.CenterEnd else Alignment.BottomCenter)
                            .widthIn(
                                max = if (splitMap) layoutWidth * 0.46f
                                else if (layoutWidth >= 600.dp && layoutHeight >= 600.dp) {
                                    680.dp
                                } else {
                                    layoutWidth
                                }
                            )
                    )
                }
            }
        }
        if (!landscape) {
            BottomNavigation(selectedSection = DemoSection.Map, onSectionChange = onSectionChange)
        }
    }
}
