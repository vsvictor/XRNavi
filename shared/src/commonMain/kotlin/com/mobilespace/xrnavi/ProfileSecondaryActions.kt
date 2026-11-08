package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ProfileSecondaryActions(
    horizontal: Boolean = false,
    onOpenPrivacy: (() -> Unit)? = null,
    onOpenSavedPlaces: (() -> Unit)? = null,
    unavailable: (String) -> Unit
) {
    val security: @Composable () -> Unit = {
        AccountAction("Безпека та особисті дані",
            if (onOpenPrivacy != null) "Відкрити центр приватності" else "Не підключено") {
            if (onOpenPrivacy != null) onOpenPrivacy()
            else unavailable("Керування даними акаунта й видалення профілю не підключено. Демо-профіль не є акаунтом.")
        }
    }
    val favorites: @Composable () -> Unit = {
        AccountAction("Обране", if (onOpenSavedPlaces != null) "Локальні місця" else "Немає даних") {
            if (onOpenSavedPlaces != null) onOpenSavedPlaces()
            else unavailable("Сервіс обраного не підключено. Збережених даних немає.")
        }
    }
    if (horizontal) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(2f)) { security() }
        Box(Modifier.weight(1f)) { favorites() }
    } else {
        security()
        favorites()
    }
}
