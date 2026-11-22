package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalNotice(title: String, body: String, warning: Boolean = false) {
    Surface(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
        color = if (warning) accountPolicyColor().copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccountIcon(
                if (warning) AccountIcon.Lock else AccountIcon.Help,
                Modifier.size(20.dp), tint = if (warning) accountPolicyColor() else null
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (warning) accountPolicyColor() else MaterialTheme.colorScheme.onSurface
                )
                AdditionalBody(body)
            }
        }
    }
}
