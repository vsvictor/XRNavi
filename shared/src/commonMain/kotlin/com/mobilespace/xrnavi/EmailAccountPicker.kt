package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberEmailAccountPicker(
    onSelected: (String) -> Unit,
    onFailure: (String) -> Unit,
): () -> Unit
