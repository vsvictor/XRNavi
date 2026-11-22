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
internal fun AdditionalMetrics(layout: AccountLayout, values: List<Pair<String, String>>) {
    if (layout == AccountLayout.PhonePortrait && values.size > 2) {
        values.chunked(2).forEach { AdditionalMetrics(layout, it) }
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { (label, value) ->
            Surface(
                Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdditionalBody(label)
                    Text(
                        value, fontFamily = rememberRobotoCondensedFontFamily(),
                        fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
