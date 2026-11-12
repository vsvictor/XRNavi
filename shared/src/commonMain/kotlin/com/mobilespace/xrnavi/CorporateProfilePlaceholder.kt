package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
internal fun CorporateProfilePlaceholder() {
    ProfileSectionCard {
        CardHeading("Корпоративний акаунт")
        Text(
            "Цей розділ поки не налаштовано.",
            modifier = Modifier.testTag("profile-corporate-placeholder"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
