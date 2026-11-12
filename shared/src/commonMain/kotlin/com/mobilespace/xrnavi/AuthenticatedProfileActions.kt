package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthenticatedProfileActions(
    layout: AccountLayout,
    onOpenSecurity: () -> Unit,
    onOpenFavorites: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = accountSurfaceColor()) {
        Column {
            ProfileNavigationRow(
                "Безпека та особисті дані", "Пошта, дані акаунта й сеанс",
                AccountIcon.Lock, "authenticated-profile-security", onOpenSecurity,
            )
            HorizontalDivider(
                Modifier.padding(start = 66.dp, end = 14.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            )
            ProfileNavigationRow(
                "Обране", "Ваші збережені місця",
                AccountIcon.Route, "authenticated-profile-favorites", onOpenFavorites,
            )
        }
    }
}
