package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DemoGarageScreen(
    layout: AccountLayout,
    currentVehicleId: String,
    filter: GarageFilter,
    onFilterChange: (GarageFilter) -> Unit,
    onSelectVehicle: (DemoVehicleProfile) -> Unit,
    onOpenVehicleProfile: (DemoVehicleProfile) -> Unit,
    unavailable: (String) -> Unit
) {
    val vehicles = garageVehicles(filter)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GarageFilterBar(filter, onFilterChange)
        GarageSectionHeading(
            text = if (filter == GarageFilter.All) "Доступні демопрофілі"
            else "${filter.label} транспорт"
        )
        if (vehicles.isEmpty()) {
            GarageEmptyState(filter)
        } else {
            vehicles.forEach { vehicle ->
                GarageVehicleCard(
                    vehicle = vehicle,
                    layout = layout,
                    selected = currentVehicleId == vehicle.id,
                    onSelect = { onSelectVehicle(vehicle) },
                    onDetails = { onOpenVehicleProfile(vehicle) }
                )
            }
        }
        Button(
            onClick = { unavailable(garageAddUnavailableExplanation) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Додати транспорт · не підключено", fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        AccountBody(garageCorporateUnavailableExplanation)
    }
}

@Composable
private fun GarageFilterBar(
    selected: GarageFilter,
    onSelected: (GarageFilter) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .selectableGroup()
            .clip(RoundedCornerShape(10.dp))
            .background(accountSurfaceColor())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GarageFilter.entries.forEach { filter ->
            val active = filter == selected
            Box(
                Modifier.weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .semantics {
                        contentDescription = filter.label
                        this.selected = active
                        stateDescription = if (active) "Вибрано" else "Не вибрано"
                    }
                    .selectable(
                        selected = active,
                        role = Role.Tab,
                        onClick = { onSelected(filter) }
                    )
                    .testTag("garage-filter-${filter.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    filter.label,
                    modifier = Modifier.padding(horizontal = 2.dp),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (active) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun GarageSectionHeading(text: String) {
    Text(
        text,
        fontFamily = rememberRobotoCondensedFontFamily(),
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun GarageEmptyState(filter: GarageFilter) {
    AccountCard(tonal = true) {
        CardHeading("Немає сумісних демопрофілів")
        AccountBody(
            "Для категорії «${filter.label}» локальних демопрофілів немає. " +
                "Профіль іншої категорії не відображається як сумісний транспорт."
        )
    }
}

@Composable
private fun GarageVehicleCard(
    vehicle: DemoVehicleProfile,
    layout: AccountLayout,
    selected: Boolean,
    onSelect: () -> Unit,
    onDetails: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("garage-profile-card"),
        shape = RoundedCornerShape(12.dp),
        color = accountSurfaceColor(),
        border = BorderStroke(
            width = if (selected) 1.dp else 0.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column {
            Box {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(
                        when (layout) {
                            AccountLayout.PhonePortrait -> 1.8f
                            AccountLayout.PhoneLandscape -> 1.9f
                            AccountLayout.TabletPortrait -> 2.45f
                            AccountLayout.TabletLandscape -> 1.85f
                        }
                    ).background(
                        if (vehicle.artwork == DemoVehicleArtwork.Mustang) Color.Transparent
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    DemoVehicleImage(
                        vehicle = vehicle,
                        modifier = if (vehicle.artwork == DemoVehicleArtwork.Mustang) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.size(112.dp)
                        },
                        contentDescription = "Демонстраційне зображення ${vehicle.name}"
                    )
                }
                Box(
                    Modifier.fillMaxWidth().heightIn(min = 82.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            vehicle.name,
                            color = Color.White,
                            fontFamily = rememberRobotoCondensedFontFamily(),
                            fontSize = if (layout.tablet) 26.sp else 22.sp,
                            lineHeight = if (layout.tablet) 32.sp else 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Демо-профіль · характеристики не перевірені",
                            color = Color.White.copy(alpha = 0.84f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Surface(
                    Modifier.align(Alignment.TopStart).padding(10.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                ) {
                    Text(
                        if (selected) "ОБРАНО ДЛЯ ДЕМОМАРШРУТУ" else "ДОСТУПНИЙ ДЕМОПРОФІЛЬ",
                        Modifier.padding(horizontal = 8.dp, vertical = 5.dp).semantics {
                            contentDescription = "${vehicle.name} · " +
                                if (selected) "обрано для демомаршруту"
                                else "доступний демопрофіль"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountBody("Категорія: ${vehicle.category}")
                AccountBody("Демо-характеристики, не перевірені: ${vehicle.specification}")
                TextButton(
                    onClick = onDetails,
                    modifier = Modifier.align(Alignment.End).heightIn(min = 44.dp)
                        .semantics { contentDescription = "Переглянути профіль ${vehicle.name}" }
                ) {
                    Text("Переглянути профіль")
                }
                Button(
                    onClick = onSelect,
                    enabled = !selected,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("garage-select-profile"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        if (selected) "Обрано для демомаршруту"
                        else "Обрати ${vehicle.name}",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
