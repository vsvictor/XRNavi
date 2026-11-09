package com.mobilespace.xrnavi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

@Composable
internal fun MapControlButton(
    iconLight: DrawableResource,
    iconDark: DrawableResource,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            FigmaIcon(
                light = iconLight,
                dark = iconDark,
                modifier = Modifier.size(20.dp),
                contentDescription = label
            )
        }
    }
}
