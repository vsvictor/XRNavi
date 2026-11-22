package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun MapTopBar(
    demoMode: Boolean,
    preview: Boolean,
    vehicleMode: String,
    onBack: () -> Unit,
    onOpenStopPlaces: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenExplanation: (() -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        val compact = maxWidth < 440.dp
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compact) 12.dp else 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (preview) {
                    Text(
                        text = "‹",
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onBack)
                            .padding(end = 8.dp),
                        fontSize = 27.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ProductWordmark()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 10.dp)
            ) {
                TextButton(
                    onClick = onOpenStopPlaces,
                    modifier = Modifier.widthIn(max = 90.dp)
                ) {
                    Text("Місця зупинки", fontSize = 11.sp, lineHeight = 14.sp)
                }
                Text(
                    text = when {
                        compact && demoMode -> "Демо"
                        demoMode -> "Демо · $vehicleMode"
                        else -> vehicleMode
                    },
                    modifier = Modifier
                        .semantics {
                            contentDescription = if (demoMode) {
                                "Демо · $vehicleMode"
                            } else {
                                vehicleMode
                            }
                        },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Тема",
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button, onClick = onToggleTheme)
                        .padding(horizontal = if (compact) 2.dp else 6.dp, vertical = 5.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
                if (onOpenExplanation != null) IconButton(onClick = onOpenExplanation) {
                    AccountIcon(AccountIcon.Help, contentDescription = "Чому обрано цей шлях · приклад")
                }
            }
        }
    }
}
