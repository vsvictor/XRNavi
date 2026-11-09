package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AccountCard(
    tonal: Boolean = false,
    minHeight: Int = 0,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (tonal) accountTonalColor() else accountSurfaceColor()
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = minHeight.dp).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
