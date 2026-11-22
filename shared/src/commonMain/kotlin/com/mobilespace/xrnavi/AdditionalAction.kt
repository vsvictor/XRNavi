package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalAction(
    title: String, detail: String, destructive: Boolean = false, onClick: () -> Unit
) {
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                AdditionalBody(detail)
            }
            AccountIcon(AccountIcon.ChevronRight, Modifier.size(18.dp))
        }
    }
}
