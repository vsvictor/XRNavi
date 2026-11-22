package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
internal fun UnavailableSettingCard(setting: UnavailableSetting) {
    SettingsRow(
        "${setting.title} · ${setting.status}", setting.explanation,
        grouped = false, icon = AccountIcon.Download
    )
}
