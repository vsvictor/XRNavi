package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AuthenticatedGarageVehicleCard(
    vehicle: GarageVehicle,
    active: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    onDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("garage-vehicle-${vehicle.id}"),
        shape = RoundedCornerShape(16.dp),
        color = accountSurfaceColor(),
        border = androidx.compose.foundation.BorderStroke(
            if (active) 1.5.dp else 1.dp,
            if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        AccountIcon(
                            AccountIcon.Driver,
                            Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Транспорт",
                        )
                    }
                }
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        vehicle.name,
                        fontFamily = rememberRobotoCondensedFontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${garageCategoryLabel(vehicle.category)} · ${vehicle.verification.label()}",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (active) {
                    Text(
                        "Активне",
                        modifier = Modifier.testTag("garage-active-${vehicle.id}"),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (vehicle.needsVerification) {
                Text(
                    "Потрібне підтвердження профілю для використання обмежень маршруту.",
                    modifier = Modifier.testTag("garage-verification-required"),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GarageVehicleActionButton(
                    text = "Профіль",
                    onClick = onDetails,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).testTag("garage-details-${vehicle.id}"),
                )
                GarageVehicleActionButton(
                    text = if (active) "Обрано" else "Обрати",
                    onClick = onSelect,
                    enabled = enabled && !active,
                    modifier = Modifier.weight(1f).testTag("garage-select-${vehicle.id}"),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GarageVehicleActionButton(
                    text = "Редагувати",
                    onClick = onEdit, enabled = enabled,
                    modifier = Modifier.weight(1f).testTag("garage-edit-${vehicle.id}"),
                )
                GarageVehicleActionButton(
                    text = "Видалити",
                    onClick = onDelete, enabled = enabled,
                    modifier = Modifier.weight(1f).testTag("garage-delete-${vehicle.id}"),
                )
            }
        }
    }
}
