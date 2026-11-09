package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun MapMessages(
    state: MapState,
    routeState: RouteState,
    expired: Boolean,
    dispatch: (MapEvent) -> Unit,
    routeDispatch: (RouteEvent) -> Unit,
    onProfile: () -> Unit,
) {
    val notice = routeState.notice.ifEmpty { state.notice }
    if (state.status == MapLoadStatus.Ready && notice.isEmpty() && !expired) return
    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 3.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (state.status) {
                MapLoadStatus.Loading -> {
                    CircularProgressIndicator(Modifier.testTag("map-loading"))
                    Text("Завантаження мапи…")
                }
                MapLoadStatus.Failed -> {
                    Text("Не вдалося завантажити мапу. Перевірте мережу та налаштування Mapbox.")
                    TextButton(onClick = { dispatch(MapEvent.Retry) }) { Text("Повторити") }
                }
                MapLoadStatus.Ready -> Unit
            }
            if (expired) {
                Text("Сесія завершилась. Онлайн-дані аккаунта недоступні; мапа залишається доступною.")
                TextButton(onClick = onProfile) { Text("Перейти до профілю") }
            }
            if (notice.isNotEmpty()) {
                Text(notice)
                TextButton(onClick = {
                    dispatch(MapEvent.DismissNotice)
                    routeDispatch(RouteEvent.DismissNotice)
                }) { Text("Зрозуміло") }
            }
        }
    }
}
