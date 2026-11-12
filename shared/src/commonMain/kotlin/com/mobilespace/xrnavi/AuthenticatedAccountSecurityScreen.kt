package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity

@Composable
internal fun AuthenticatedAccountSecurityScreen(
    profile: AuthenticatedProfile,
    onBack: () -> Unit,
) {
    AccountPageLayout(
        title = "Безпека та особисті дані",
        modifier = Modifier.testTag("authenticated-account-security"),
        onBack = onBack,
    ) {
        AuthenticatedProfileDetails(profile)
    }
}
