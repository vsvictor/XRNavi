package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberGoogleIdTokenProvider(
    onToken: (String) -> Unit,
    onFailure: (String) -> Unit,
): () -> Unit = {
    onFailure("Вхід через Google поки недоступний на iOS.")
}
