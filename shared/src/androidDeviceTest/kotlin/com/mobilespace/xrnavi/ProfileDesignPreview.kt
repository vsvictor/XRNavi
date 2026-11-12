package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ProfileDesignPreview(
    profile: AuthenticatedProfile,
    garage: GarageState,
    selectedTab: ProfileAccountTab = ProfileAccountTab.Personal,
    scrollToBottom: Boolean = false,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val layout = AccountLayout.forWindow(maxWidth.value, maxHeight.value)
        Column {
            Row(Modifier.weight(1f)) {
                if (layout.landscape) {
                    BottomNavigation(
                        DemoSection.Profile,
                        {},
                        rail = true,
                        compactRail = !layout.tablet
                    )
                }
                Column(
                    Modifier.weight(1f).fillMaxWidth()
                        .verticalScroll(rememberScrollState(if (scrollToBottom) Int.MAX_VALUE else 0))
                        .accountPageBackground().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AuthenticatedProfileScreen(
                        profile, layout, garage, {}, {}, {}, {},
                        selectedTab, {}, onLogout = {},
                    )
                }
            }
            if (!layout.landscape) BottomNavigation(DemoSection.Profile, {})
        }
    }
}
