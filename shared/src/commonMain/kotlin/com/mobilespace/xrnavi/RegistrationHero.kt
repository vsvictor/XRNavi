package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun RegistrationHero(modifier: Modifier, compact: Boolean) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    Box(modifier.clip(RoundedCornerShape(16.dp))) {
        Image(
            painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                if (compact) {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                }
            )
        )
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!compact) ProductWordmark()
            Column {
                Text(
                    "БІЛЬШЕ, НІЖ ДОРОГА.",
                    fontFamily = rememberRobotoCondensedFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 22.sp else 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Один профіль. Усі ваші автомобілі.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Mobile space · XRNavi",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
