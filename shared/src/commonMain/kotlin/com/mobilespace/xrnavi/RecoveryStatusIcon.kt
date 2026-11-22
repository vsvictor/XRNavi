package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun RecoveryStatusIcon(resource: DrawableResource, warning: Boolean = false) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (warning) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(resource),
                contentDescription = null,
                tint = if (warning) androidx.compose.ui.graphics.Color(
                    if (dark) 0xFFFFC95C else 0xFF8A5C00
                ) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
