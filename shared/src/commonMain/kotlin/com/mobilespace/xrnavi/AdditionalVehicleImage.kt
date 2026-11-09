package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalVehicleImage() {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    AdditionalCard {
        Image(
            painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
            contentDescription = "Ілюстративне зображення Ford Mustang",
            modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop
        )
        AdditionalBody("Ілюстративне зображення · не підтверджує комплектацію")
    }
}
