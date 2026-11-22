package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun RoutePreferenceCard(
    preferences: RoutePreferences,
    hasPendingChanges: Boolean,
    onPreferencesChange: (RoutePreferences) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Переваги маршруту",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (hasPendingChanges) {
                Text(
                    "Зміни ще не застосовано",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            RoutePreferenceSwitch(
                label = "Уникати платних доріг",
                checked = preferences.avoidTolls,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidTolls = it)) }
            )
            RoutePreferenceSwitch(
                label = "Уникати ґрунтових доріг",
                checked = preferences.avoidUnpavedRoads,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidUnpavedRoads = it)) }
            )
            RoutePreferenceSwitch(
                label = "Уникати поромів",
                checked = preferences.avoidFerries,
                onCheckedChange = { onPreferencesChange(preferences.copy(avoidFerries = it)) }
            )
            val enabledPreferences = listOfNotNull(
                "платні дороги".takeIf { preferences.avoidTolls },
                "ґрунтові дороги".takeIf { preferences.avoidUnpavedRoads },
                "пороми".takeIf { preferences.avoidFerries }
            )
            Text(
                text = if (enabledPreferences.isEmpty()) {
                    "Обмеження не вибрано"
                } else {
                    "Увімкнено: ${enabledPreferences.joinToString()}"
                },
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
