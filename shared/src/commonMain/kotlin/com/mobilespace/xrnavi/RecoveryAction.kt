package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.*

@Composable
internal fun RecoveryAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) { Text(text) }
}
