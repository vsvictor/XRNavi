package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun ResponsiveSections(
    columns: Boolean,
    firstWeight: Float = 0.5f,
    gap: Int = 16,
    first: @Composable ColumnScope.() -> Unit,
    second: @Composable ColumnScope.() -> Unit
) {
    if (columns) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap.dp)) {
            Column(
                Modifier.weight(firstWeight).testTag("account-sections-first"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = first
            )
            Column(
                Modifier.weight(1f - firstWeight).testTag("account-sections-second"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = second
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            first()
            second()
        }
    }
}
