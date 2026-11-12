package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfileAccountTabs(
    selected: ProfileAccountTab,
    onSelect: (ProfileAccountTab) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val measurer = rememberTextMeasurer()
        val style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        )
        val availableWidth = with(LocalDensity.current) {
            (maxWidth / ProfileAccountTab.entries.size - 32.dp).toPx().coerceAtLeast(1f)
        }
        val widestLabel = ProfileAccountTab.entries.maxOf {
            measurer.measure(it.label, style = style, softWrap = false, maxLines = 1).size.width
        }
        val fontSize = 14.sp * (availableWidth / widestLabel.coerceAtLeast(1)).coerceAtMost(1f)
        PrimaryTabRow(
            selectedTabIndex = selected.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            ProfileAccountTab.entries.forEach { tab ->
                Tab(
                    selected = selected == tab,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.testTag("profile-tab-${tab.name.lowercase()}"),
                    text = {
                        Text(
                            tab.label,
                            style = style.copy(fontSize = fontSize),
                            maxLines = 1,
                            softWrap = false,
                        )
                    },
                )
            }
        }
    }
}
