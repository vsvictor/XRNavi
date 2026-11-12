package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AuthBackButton(
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.size(48.dp).semantics { contentDescription = description },
    ) {
        Text("‹", fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
    }
}
