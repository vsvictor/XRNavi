package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
internal fun ProfileOrganization(profile: DemoProfileState, layout: AccountLayout) {
    AccountCard(
        tonal = true,
        minHeight = if (layout == AccountLayout.TabletLandscape) 164 else 0
    ) {
        CardHeading("Організація")
        AccountBody(profile.organization)
        AccountBody("Синхронізація та корпоративні права недоступні.")
    }
}
