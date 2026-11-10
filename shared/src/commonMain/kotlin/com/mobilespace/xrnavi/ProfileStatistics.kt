package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfileStatistics(layout: AccountLayout, showUnavailableNotice: Boolean = true) {
    Row(
        Modifier.testTag("profile-statistics"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("поїздки", "км", "год у дорозі").forEach { label ->
            Surface(
                Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    Modifier.heightIn(
                        min = when (layout) {
                            AccountLayout.TabletLandscape -> 88.dp
                            AccountLayout.TabletPortrait, AccountLayout.PhoneLandscape -> 72.dp
                            AccountLayout.PhonePortrait -> 64.dp
                        }
                    ).padding(horizontal = 12.dp, vertical = if (layout.tablet) 12.dp else 6.dp)
                ) {
                    Text(
                        "—",
                        fontSize = if (layout.tablet) 32.sp else 26.sp,
                        lineHeight = if (layout.tablet) 38.sp else 31.sp,
                        fontFamily = rememberRobotoCondensedFontFamily(),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(
                        Modifier.height(
                            when (layout) {
                                AccountLayout.PhonePortrait -> 2.dp
                                AccountLayout.TabletLandscape -> 4.dp
                                else -> 0.dp
                            }
                        )
                    )
                    Text(
                        label, fontSize = 12.sp,
                        lineHeight = 16.sp, letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    if (showUnavailableNotice) AccountBody("Статистика відсутня · деморежим")
}
