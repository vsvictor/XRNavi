package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalColumns(
    layout: AccountLayout,
    primary: @Composable ColumnScope.() -> Unit,
    secondary: @Composable ColumnScope.() -> Unit
) {
    if (layout.landscape || layout.tablet) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = primary
            )
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = secondary
            )
        }
    } else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        primary()
        secondary()
    }
}
