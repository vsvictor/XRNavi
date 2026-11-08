package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalCard(
    selected: Boolean = false,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = when {
            destructive -> colors.error.copy(alpha = 0.14f)
            selected -> colors.surfaceVariant
            colors.background.red < 0.5f -> Color(0xFF080F19)
            else -> colors.surface
        },
        border = if (selected) BorderStroke(1.dp, colors.primary) else null
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}
