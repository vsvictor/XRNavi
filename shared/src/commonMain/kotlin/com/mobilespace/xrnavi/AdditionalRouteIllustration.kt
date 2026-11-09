package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalRouteIllustration(
    caption: String = "Київ → Львів · схема з макета, не навігаційна мапа"
) {
    Surface(
        Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            MapCanvas(Modifier.fillMaxWidth().height(180.dp), preview = true, compact = true)
            Box(Modifier.padding(12.dp)) { AdditionalBody(caption) }
        }
    }
}
