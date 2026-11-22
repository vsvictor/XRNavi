package com.mobilespace.xrnavi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun Choice(label: String, tag: String, selected: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().testTag(tag).clickable(onClick = onClick)) {
        Text(if (selected) "✓ $label" else label, Modifier.padding(12.dp))
    }
}
