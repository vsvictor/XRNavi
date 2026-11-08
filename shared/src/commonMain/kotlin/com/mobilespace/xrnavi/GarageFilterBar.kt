package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun GarageFilterBar(
    selected: GarageFilter,
    onSelected: (GarageFilter) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .selectableGroup()
            .clip(RoundedCornerShape(10.dp))
            .background(accountSurfaceColor())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GarageFilter.entries.forEach { filter ->
            val active = filter == selected
            Box(
                Modifier.weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .semantics {
                        contentDescription = filter.label
                        this.selected = active
                        stateDescription = if (active) "Вибрано" else "Не вибрано"
                    }
                    .selectable(
                        selected = active,
                        role = Role.Tab,
                        onClick = { onSelected(filter) }
                    )
                    .testTag("garage-filter-${filter.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    filter.label,
                    modifier = Modifier.padding(horizontal = 2.dp),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (active) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
