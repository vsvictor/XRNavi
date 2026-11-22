package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun GarageListHeader() {
    Row(
        Modifier.fillMaxWidth().headerSafeTop()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .testTag("garage-header"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductWordmark()
    }
}
