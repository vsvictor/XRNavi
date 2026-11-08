package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun RecoveryPhoto(modifier: Modifier) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    Image(
        painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(16.dp))
    )
}
