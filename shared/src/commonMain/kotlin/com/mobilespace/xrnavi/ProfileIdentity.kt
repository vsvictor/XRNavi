package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfileIdentity(
    profile: DemoProfileState,
    layout: AccountLayout,
    unavailable: (String) -> Unit
) {
    Column(
        Modifier.testTag("profile-identity"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val edit: @Composable () -> Unit = {
            Button(
                onClick = { unavailable("Редагування акаунта не підключено. Цей демо-профіль не містить підтверджених особистих даних.") },
                shape = RoundedCornerShape(24.dp),
                modifier = (if (layout == AccountLayout.TabletPortrait) Modifier else Modifier.fillMaxWidth())
                    .heightIn(min = 44.dp)
            ) {
                Text(
                    "Редагувати профіль · не підключено",
                    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(
                    when (layout) {
                        AccountLayout.PhoneLandscape -> 44.dp
                        AccountLayout.TabletLandscape -> 80.dp
                        else -> 64.dp
                    }
                ),
                shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "MS", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        lineHeight = 24.sp, letterSpacing = 0.sp
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                SectionTitle(
                    profile.title,
                    fontSize = if (layout == AccountLayout.TabletLandscape) 32 else 24,
                    lineHeight = if (layout == AccountLayout.TabletLandscape) 40 else 30
                )
                AccountBody(profile.identity)
            }
            if (layout == AccountLayout.TabletPortrait) edit()
        }
        if (layout != AccountLayout.TabletPortrait) edit()
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Особистий", "Корпоративний").forEach { mode ->
                Surface(
                    Modifier.weight(1f).semantics {
                        selected = mode == profile.vehicleMode
                        contentDescription = "$mode · режим вибору транспорту на мапі"
                    },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline),
                    color = if (mode == profile.vehicleMode && MaterialTheme.colorScheme.background.red < 0.5f)
                        Color(0xFF3E465F)
                    else if (mode == profile.vehicleMode) MaterialTheme.colorScheme.surfaceVariant
                    else accountSurfaceColor()
                ) {
                    Text(
                        mode, Modifier.padding(vertical = 8.dp), fontSize = 14.sp,
                        lineHeight = 20.sp, letterSpacing = 0.1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        AccountBody("Режим мапи: ${profile.vehicleMode} · без членства в компанії.")
    }
}
