package com.mobilespace.xrnavi

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun ExitConfirmationDialog(
    canExit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Вийти із застосунку?") },
        text = {
            Text(
                if (canExit) "Ви справді хочете завершити роботу застосунку?"
                else "На iOS скористайтеся системним жестом, щоб закрити застосунок."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (canExit) "Вийти" else "Зрозуміло")
            }
        },
        dismissButton = if (canExit) {
            { TextButton(onClick = onDismiss) { Text("Скасувати") } }
        } else null,
    )
}
