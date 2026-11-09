package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberEmailAccountPicker(
    onSelected: (String) -> Unit,
    onFailure: (String) -> Unit,
): () -> Unit = {
    onFailure("iOS не дозволяє отримати список поштових аккаунтів телефону. Введіть пошту вручну.")
}
