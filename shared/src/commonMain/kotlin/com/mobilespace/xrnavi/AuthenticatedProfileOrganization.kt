package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AuthenticatedProfileOrganization(
    profile: AuthenticatedProfile,
    layout: AccountLayout,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accountTonalColor(),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(if (layout.tablet) 16.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Організація",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold,
            )
            Text(
                when (profile.accountType) {
                    AuthenticatedAccountType.Corporate -> "Корпоративний акаунт. Відомості про організацію ще не надані."
                    AuthenticatedAccountType.Personal -> "Відомості про членство в організації не надані."
                    AuthenticatedAccountType.Unknown -> "Тип акаунта й відомості про організацію не надані."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp, lineHeight = 18.sp,
            )
            Text(
                "Корпоративні політики ще не підключені.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp, lineHeight = 18.sp,
            )
        }
    }
}
