package com.mobilespace.xrnavi

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.BoxWithConstraints

@Composable
internal fun AuthenticatedProfileDetails(profile: AuthenticatedProfile) {
    BoxWithConstraints {
        ResponsiveSections(
            columns = maxWidth >= 600.dp,
            firstWeight = 0.55f,
            first = {
                SecurityPersonalCard(profile)
            },
            second = {
                ProfileSectionCard {
                    CardHeading("Дані акаунта", singleLine = true)
                    AccountDetailField(
                        "Зареєстрований",
                        formatAuthenticatedProfileDate(profile.createdAt),
                    )
                    AccountDetailField(
                        "ID",
                        profile.id,
                        singleLineValue = true,
                        inlineLabelValue = true,
                    )
                    if (profile.roles.isEmpty()) {
                        AccountDetailField(
                            "Ролі",
                            "немає призначених ролей",
                            divider = false,
                            emphasizedLabel = true,
                        )
                    } else {
                        AccountDetailLabel("Ролі", emphasized = true)
                        profile.roles.forEachIndexed { index, role ->
                            ProfileRoleRow(role)
                            if (index != profile.roles.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}
