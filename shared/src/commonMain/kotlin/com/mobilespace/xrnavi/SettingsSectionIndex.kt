package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SettingsSectionIndex() {
    Surface(shape = RoundedCornerShape(8.dp), color = accountSurfaceColor()) {
        Row(Modifier.fillMaxWidth().padding(4.dp)) {
            listOf("Особисті", "Від організації", "Офлайн-мапи").forEach { text ->
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(4.dp))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() }
                    )
                }
            }
        }
    }
}
