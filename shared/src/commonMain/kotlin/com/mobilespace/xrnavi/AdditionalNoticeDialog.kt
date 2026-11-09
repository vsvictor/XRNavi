package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalNoticeDialog(notice: String?, onDismiss: () -> Unit) {
    notice?.let {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Демонстраційний режим") },
            text = { Text(it, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Зрозуміло") } }
        )
    }
}
