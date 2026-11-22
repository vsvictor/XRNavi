package com.mobilespace.xrnavi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AuthenticatedGarageVehicleCard(
    vehicle: GarageVehicle,
    photo: VehiclePhoto? = null,
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
        Box {
            GarageVehiclePhoto(
                photo,
                Modifier.matchParentSize().alpha(0.25f)
                    .testTag("garage-photo-background-${vehicle.id}")
            )
            Column(
                Modifier.fillMaxWidth()                .heightIn(min = 200.dp)
                .testTag("garage-card-action-${vehicle.id}")
                .semantics { selected = active }
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = "Відкрити профіль ${vehicle.name}",
                    onClick = onDetails,
                )
                    .padding(16.dp),
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
                        GarageVehiclePhoto(
                            photo,
                            Modifier.fillMaxSize().testTag("garage-photo-thumbnail-${vehicle.id}")
                        )
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
                AccountSingleLineText(
                    text = "${garageCategoryLabel(vehicle.category)} · ${vehicle.verification.label()}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .testTag("garage-vehicle-status-${vehicle.id}"),
                )
                if (vehicle.needsVerification) {
                    Text(
                        "Потрібне підтвердження профілю для використання обмежень маршруту.",
                        modifier = Modifier.testTag("garage-verification-required"),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!active) {
                        TextButton(onClick = onSelect, enabled = enabled) {
                            Text("Зробити основним")
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    GarageVehicleIconButton(
                        action = GarageVehicleAction.Edit,
                        description = "Редагувати",
                        onClick = onEdit, enabled = enabled,
                        modifier = Modifier.testTag("garage-edit-${vehicle.id}"),
                    )
                    GarageVehicleIconButton(
                        action = GarageVehicleAction.Delete,
                        description = "Видалити",
                        onClick = onDelete, enabled = enabled,
                        modifier = Modifier.testTag("garage-delete-${vehicle.id}"),
                    )
                }
            }
        }
    }
}
