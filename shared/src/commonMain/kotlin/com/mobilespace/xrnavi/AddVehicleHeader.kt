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
internal fun AddVehicleHeader(onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag("add-vehicle-header"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuthBackButton(onBack, "Назад до гаража")
        ProductWordmark()
    }
}
