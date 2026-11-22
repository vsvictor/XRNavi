package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import kotlin.math.abs
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

@Composable
internal fun RouteBottomSheet(
    preview: Boolean,
    origin: String,
    destination: String,
    routeStops: List<RouteStop>,
    geoStatus: GeoStatus,
    notice: String,
    level: SheetLevel,
    showInlineAttribution: Boolean,
    vehicleClass: String,
    selectedVehicle: DemoVehicleProfile?,
    onVehicleClassChange: (String) -> Unit,
    sheetHeightDp: Dp,
    scrollConnection: NestedScrollConnection,
    scrollState: androidx.compose.foundation.ScrollState,
    minHeightPx: Float,
    partialHeightPx: Float,
    maxHeightPx: Float,
    currentHeightPx: () -> Float,
    updateHeight: (Float) -> Unit,
    onLevelChange: (SheetLevel) -> Unit,
    onOpenSearch: (SearchTarget) -> Unit,
    onOpenRouteStops: () -> Unit,
    onPreview: () -> Unit,
    onStartRoute: () -> Unit,
    onOpenRouteSettings: () -> Unit,
    sheetContent: (@Composable ColumnScope.() -> Unit)? = null,
    fixedPane: Boolean = false,
    modifier: Modifier
) {
    val dark = isDarkXrnaviTheme()
    val navigationUnavailable =
        preview && notice.startsWith("Маршрут і навігацію не підключено")
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeightDp)
            .exportTestTags()
            .then(if (fixedPane) Modifier else Modifier.nestedScroll(scrollConnection)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = if (fixedPane || sheetContent != null) 16.dp else 0.dp)
        ) {
            if (sheetContent == null && !fixedPane) Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .semantics {
                        contentDescription = "Панель маршруту"
                        stateDescription = when (level) {
                            SheetLevel.Collapsed -> "Згорнута"
                            SheetLevel.Partial -> "Частково розгорнута"
                            SheetLevel.Expanded -> "Розгорнута"
                        }
                    }
                    .clickable(role = Role.Button) {
                        onLevelChange(nextSheetLevel(level))
                    }
                    .pointerInput(minHeightPx, maxHeightPx) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                updateHeight(
                                    (currentHeightPx() - dragAmount)
                                        .coerceIn(minHeightPx, maxHeightPx)
                                )
                            },
                            onDragEnd = {
                                val anchors = listOf(
                                    SheetLevel.Collapsed to minHeightPx,
                                    SheetLevel.Partial to partialHeightPx,
                                    SheetLevel.Expanded to maxHeightPx
                                )
                                val level = anchors.minByOrNull {
                                    abs(it.second - currentHeightPx())
                                }?.first ?: SheetLevel.Collapsed
                                onLevelChange(level)
                            }
                        )
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(width = 44.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            if (showInlineAttribution && level == SheetLevel.Collapsed) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Демо-мапа · Mapbox / OpenStreetMap не підключені",
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription =
                                    "Демонстраційна мапа. Mapbox і OpenStreetMap не підключені."
                            },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (level == SheetLevel.Expanded) {
                        Text(
                            "Параметри маршруту та альтернативи ›",
                            modifier = Modifier
                                .clickable(role = Role.Button, onClick = onOpenRouteSettings)
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (sheetContent == null && level == SheetLevel.Expanded && !showInlineAttribution) {
                Text(
                    "Параметри маршруту та альтернативи ›",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(role = Role.Button, onClick = onOpenRouteSettings)
                        .padding(vertical = 4.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("route-sheet-content")
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showInlineAttribution && level != SheetLevel.Collapsed) {
                    Text(
                        "Демо-мапа · Mapbox / OpenStreetMap не підключені",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (sheetContent == null && level == SheetLevel.Expanded) {
                        Text(
                            "Параметри маршруту та альтернативи ›",
                            modifier = Modifier.clickable(role = Role.Button, onClick = onOpenRouteSettings)
                                .padding(vertical = 8.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                when {
                    sheetContent != null -> sheetContent()
                    routeStops.isNotEmpty() -> {
                        StatusCard(
                            title = "План збережено локально",
                            message = "Багатоточковий перегляд ще не підключено. " +
                                "Збережено проміжних зупинок: ${routeStops.size}. " +
                                "Лінія маршруту, відстань і час для цього плану не розраховуються."
                        )
                    }

                    !preview -> {
                        Text(
                            text = if (origin.isBlank() || destination.isBlank()) {
                                "Маршрут не вибрано"
                            } else {
                                "Точки маршруту вибрано"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (origin.isBlank()) {
                                "Оберіть початкову адресу або введіть її вручну."
                            } else if (destination.isBlank()) {
                                "Оберіть адресу призначення для демо-перегляду."
                            } else {
                                "Адреси збережено. Попередній перегляд не розраховувався."
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (notice.isNotBlank()) {
                            StatusCard(
                                title = if (notice == geoStatusMessage(geoStatus)) {
                                    "Геолокація не підключена"
                                } else if (vehicleClassUnavailableMessage(vehicleClass) == notice) {
                                    "Профіль транспорту недоступний"
                                } else {
                                    "Стан сценарію"
                                },
                                message = notice
                            )
                        }
                        if (level != SheetLevel.Collapsed) {
                            if (demoVehicleForClass(vehicleClass) != null) {
                                if (selectedVehicle != null) {
                                    VehicleSummaryCard(selectedVehicle)
                                }
                            }
                            VehicleClassSelector(
                                selected = vehicleClass,
                                onSelected = onVehicleClassChange
                            )
                        }
                    }

                    level == SheetLevel.Collapsed -> {
                        if (vehicleClassUnavailableMessage(vehicleClass) == null) Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "6 год 20 хв",
                                modifier = Modifier.weight(1f),
                                fontFamily = rememberRobotoCondensedFontFamily(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Column(
                                modifier = Modifier.width(100.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    "540 км",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    selectedVehicle?.name ?: "Транспорт",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "Профіль для $vehicleClass недоступний",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        if (notice.isNotBlank()) {
                            StatusCard(title = "Стан маршруту", message = notice)
                        }
                        if (vehicleClassUnavailableMessage(vehicleClass) == null) {
                            if (selectedVehicle != null) {
                                VehicleSummaryCard(selectedVehicle)
                            }
                        } else {
                            StatusCard(
                                title = "Профіль транспорту недоступний",
                                message = vehicleClassUnavailableMessage(vehicleClass).orEmpty()
                            )
                        }
                        VehicleClassSelector(
                            selected = vehicleClass,
                            onSelected = onVehicleClassChange
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "6 год 20 хв",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "540 км",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "М-06 · орієнтовні дані демо-фікстури",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (sheetContent == null) XrnaviActionButton(
                text = when {
                    navigationUnavailable -> "Маршрут і навігацію не підключено · лише демо"
                    preview -> "Почати маршрут"
                    routeStops.isNotEmpty() -> "Редагувати зупинки"
                    origin.isBlank() -> "Вибрати початкову точку"
                    destination.isBlank() -> "Знайти адресу"
                    vehicleClassUnavailableMessage(vehicleClass) != null -> "Профіль недоступний"
                    else -> "Переглянути маршрут"
                },
                icon = if (preview && !navigationUnavailable) {
                    Res.drawable.route_navigation_dark
                } else {
                    null
                },
                onClick = when {
                    navigationUnavailable -> ({})
                    preview -> onStartRoute
                    routeStops.isNotEmpty() -> onOpenRouteStops
                    origin.isBlank() -> ({ onOpenSearch(SearchTarget.Origin) })
                    destination.isBlank() -> ({ onOpenSearch(SearchTarget.Destination) })
                    else -> onPreview
                },
                height = if (level == SheetLevel.Collapsed) 44.dp else 52.dp,
                cornerRadius = if (level == SheetLevel.Collapsed) 10.dp else 14.dp,
                modifier = Modifier.padding(top = 7.dp, bottom = 12.dp),
                compact = true,
                enabled = !navigationUnavailable
            )
        }
    }
}
