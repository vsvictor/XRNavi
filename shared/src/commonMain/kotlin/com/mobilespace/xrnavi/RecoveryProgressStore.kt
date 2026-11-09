package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

/** Keeps only the e-mail of a pending reset so the code screen survives process death. */
internal interface RecoveryProgressStore {
    fun read(): String?
    fun write(email: String?)
}

internal const val recoveryProgressPreferenceKey = "recovery.pending.email.v1"

@Composable
internal expect fun rememberRecoveryProgressStore(): RecoveryProgressStore
