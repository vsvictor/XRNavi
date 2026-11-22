package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberGoogleIdTokenProvider(
    onToken: (String) -> Unit,
    onFailure: (String) -> Unit,
): () -> Unit
