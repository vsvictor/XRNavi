package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

internal interface SavedPlacesStore {
    fun read(): String?
    fun write(value: String): Boolean
}

@Composable
internal expect fun rememberSavedPlacesStore(): SavedPlacesStore
