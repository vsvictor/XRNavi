package com.mobilespace.xrnavi

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun GarageCategoryFilterBar(
    categories: List<String>,
    selectedCategory: String?,
    onSelected: (String?) -> Unit,
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        (listOf(null) + categories).forEach { category ->
            val selected = category == selectedCategory
            Surface(
                modifier = Modifier.selectable(
                    selected = selected,
                    role = Role.Tab,
                    onClick = { onSelected(category) },
                ).semantics { this.selected = selected }
                    .testTag("garage-category-${category ?: "all"}"),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else accountSurfaceColor(),
            ) {
                Text(
                    category?.let(::garageCategoryLabel) ?: "Усі",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

internal fun garageCategoryFilterKey(category: String): String? = when (
    category.trim().lowercase().replace('-', '_').replace(' ', '_')
) {
    "passenger", "car", "light", "light_vehicle", "легковий", "легкове", "легкові" -> "passenger"
    "truck", "cargo", "freight", "вантажний", "вантажне", "вантажні", "вантажівка" -> "truck"
    "special", "special_vehicle", "special_equipment", "спеціальний", "спецтехніка" -> "special"
    else -> null
}

internal fun garageCategoryLabel(category: String): String = when (garageCategoryFilterKey(category)) {
    "passenger" -> "Легкове"
    "truck" -> "Вантажне"
    "special" -> "Спецтехніка"
    else -> when (category.trim().lowercase()) {
        "bus", "автобус", "автобуси" -> "Автобуси"
        else -> category.trim().ifBlank { "Категорію не вказано" }
    }
}
