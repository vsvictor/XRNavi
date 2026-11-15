package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun GarageHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onAdd: (() -> Unit)? = null,
    titleSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    subtitleSize: androidx.compose.ui.unit.TextUnit = 12.sp,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("garage-header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.semantics { contentDescription = "Назад" },
        ) {
            AccountIcon(AccountIcon.Back, Modifier.size(24.dp))
        }
        androidx.compose.foundation.layout.Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                title,
                fontFamily = rememberRobotoCondensedFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = titleSize,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                subtitle,
                fontSize = subtitleSize,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        if (onAdd != null) {
            IconButton(
                onClick = onAdd,
                modifier = Modifier.semantics { contentDescription = "Додати транспорт" },
            ) {
                Text(
                    "+",
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("garage-add-action"),
                )
            }
        }
    }
}
