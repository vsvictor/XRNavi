package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AuthenticatedProfileVehicle(
    state: GarageState,
    onOpenGarage: () -> Unit,
    onOpenVehicleDetails: (String) -> Unit,
) {
    val activeVehicle = state.active
    Column(
        Modifier.fillMaxWidth().testTag("authenticated-profile-garage"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Мій гараж",
                fontFamily = rememberRobotoCondensedFontFamily(),
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            TextButton(onClick = onOpenGarage, modifier = Modifier.testTag("profile-open-garage")) {
                Text("Керувати гаражем", fontSize = 12.sp)
                AccountIcon(
                    AccountIcon.ChevronRight, Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Surface(
            color = accountSurfaceColor(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Box(
                    Modifier.fillMaxWidth().height(104.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    ProfileVehicleIllustration(Modifier.matchParentSize())
                    Surface(
                        modifier = Modifier.padding(14.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = accountSurfaceColor(),
                    ) {
                        Text(
                            if (activeVehicle != null) "ОСНОВНЕ АВТО" else "МІЙ ГАРАЖ",
                            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (activeVehicle != null) Text(
                        activeVehicle.name,
                        Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = rememberRobotoCondensedFontFamily(),
                        fontSize = 22.sp, lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (activeVehicle != null) {
                        TextButton(
                            onClick = { onOpenVehicleDetails(activeVehicle.id) },
                            modifier = Modifier.testTag("profile-open-vehicle-details"),
                        ) {
                            Text("Деталі авто", fontSize = 12.sp)
                            AccountIcon(
                                AccountIcon.ChevronRight, Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                Column(
                    Modifier.fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when {
                        state.loading || (!state.loaded && !state.loadFailed && state.error.isEmpty()) -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.testTag("profile-garage-loading"),
                        ) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                            Text("Завантажуємо ваші авто…", fontSize = 14.sp, lineHeight = 20.sp)
                        }

                        state.loadFailed || state.error.isNotEmpty() -> {
                            TextButton(onClick = onOpenGarage) { Text("Перейти до гаража") }
                        }

                        activeVehicle == null -> {
                            Text(
                                if (state.vehicles.isEmpty()) "Ваш гараж ще порожній"
                                else "Основне авто ще не вибрано",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp, lineHeight = 22.sp,
                                modifier = Modifier.testTag(
                                    if (state.vehicles.isEmpty()) "profile-garage-empty"
                                    else "profile-garage-no-active",
                                ),
                            )
                            Text(
                                if (state.vehicles.isEmpty()) {
                                    "Додайте автомобіль у гаражі, щоб його дані були під рукою."
                                } else {
                                    "Авто в гаражі: ${state.vehicles.size}. Виберіть основне в гаражі."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp, lineHeight = 18.sp,
                            )
                            Button(
                                onClick = onOpenGarage,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            ) { Text("Відкрити гараж") }
                        }

                        else -> {
                            Text(
                                "${activeVehicle.category} · ${activeVehicle.verification.label()}",
                                modifier = Modifier.testTag("profile-vehicle-card"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp, lineHeight = 18.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
