package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfileVehicle(
    profile: DemoProfileState,
    layout: AccountLayout,
    onOpenGarage: () -> Unit,
    onOpenVehicleProfile: () -> Unit
) {
    val vehicle = profile.selectedVehicle ?: demoVehicle
    Row(
        Modifier.fillMaxWidth().testTag("profile-garage-heading"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Мій гараж",
            Modifier.weight(1f),
            fontSize = if (layout.tablet) 20.sp else 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = if (layout.tablet) 28.sp else 22.sp
        )
        TextButton(
            onClick = onOpenGarage,
            modifier = Modifier.semantics {
                contentDescription = "Керувати гаражем"
            },
            contentPadding = PaddingValues(start = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Керувати гаражем", fontSize = 12.sp)
                AccountIcon(
                    AccountIcon.ChevronRight,
                    Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    Surface(
        modifier = Modifier.testTag("profile-vehicle-card"),
        shape = RoundedCornerShape(10.dp), color = accountSurfaceColor()
    ) {
        Column {
            Box {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(
                        when (layout) {
                            AccountLayout.TabletPortrait -> 3.93f
                            AccountLayout.TabletLandscape -> 1.95f
                            AccountLayout.PhoneLandscape -> 3f
                            AccountLayout.PhonePortrait -> 3.48f
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
                Row(
                    Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = accountSurfaceColor()) {
                        Text(
                            if (profile.selectedVehicle != null) "Демоавто" else "Несумісний профіль",
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = if (layout.tablet) 20.sp else 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp)
                            .clickable(role = Role.Button, onClick = onOpenVehicleProfile)
                            .semantics { contentDescription = "Переглянути профіль ${vehicle.name}" }
                    ) {
                        Surface(shape = RoundedCornerShape(4.dp), color = accountSurfaceColor()) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "Деталі авто",
                                    fontSize = if (layout.tablet) 13.sp else 11.sp,
                                    lineHeight = if (layout.tablet) 20.sp else 16.sp
                                )
                                AccountIcon(
                                    AccountIcon.ChevronRight,
                                    Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Column(
                Modifier.padding(horizontal = 12.dp, vertical = if (layout.tablet) 8.dp else 3.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    vehicle.name,
                    fontSize = if (layout.tablet) 32.sp else 22.sp,
                    lineHeight = if (layout.tablet) 40.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp,
                    fontFamily = rememberRobotoCondensedFontFamily()
                )
                AccountBody("${vehicle.category} · демонстраційні характеристики")
            }
        }
    }
    if (profile.selectedVehicle != null) {
        AccountBody("Демо · ${profile.vehicleMode} · ${profile.vehicleClass}")
    } else {
        AuthInfo("Активна категорія на мапі: ${profile.vehicleClass}. Сумісного профілю немає; Mustang не вибрано для цієї категорії.")
    }
}
