package com.mobilespace.xrnavi

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun ResetTokenDialog(token: String?, onDismiss: () -> Unit) {
    token?.let {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Запит на відновлення пароля") },
            text = {
                Text(
                    "Для вашого аккаунта запросили відновлення пароля. Код для введення на іншому пристрої: $it",
                    Modifier.testTag("reset_token_dialog_text"),
                )
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Закрити") } },
        )
    }
}
