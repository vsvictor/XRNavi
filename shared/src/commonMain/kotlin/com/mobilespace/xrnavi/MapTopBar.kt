package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
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
    onOpenExplanation: (() -> Unit)? = null,
    overlay: Boolean = false,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (overlay) Modifier else Modifier.background(MaterialTheme.colorScheme.background))
    ) {
        val compact = maxWidth < 440.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .headerSafeTop()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.Top) {
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
                if (onOpenExplanation != null) IconButton(onClick = onOpenExplanation) {
                    AccountIcon(AccountIcon.Help, contentDescription = "Чому обрано цей шлях · приклад")
                }
            }
        }
    }
}
