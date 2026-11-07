package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthenticatedMapScreen(
    factory: PlatformMapFactory,
    state: MapState,
    dispatch: (MapEvent) -> Unit,
    expired: Boolean,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isDarkXrnaviTheme()
    LaunchedEffect(dark) { dispatch(MapEvent.StyleChanged) }
    Box(modifier.testTag("authenticated-map")) {
        NativeMap(factory, state, dark, dispatch, Modifier.fillMaxSize())
        if (state.status != MapLoadStatus.Ready || state.notice.isNotEmpty() || expired) {
            Surface(
                Modifier.align(Alignment.TopCenter).padding(12.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 3.dp,
            ) {
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
                        Text("Сесія завершилась. Онлайн-дані акаунта недоступні; мапа залишається доступною.")
                        TextButton(onClick = onProfile) { Text("Перейти до профілю") }
                    }
                    if (state.notice.isNotEmpty()) {
                        Text(state.notice)
                        TextButton(onClick = { dispatch(MapEvent.DismissNotice) }) { Text("Зрозуміло") }
                    }
                }
            }
        }
        Surface(
            Modifier.align(Alignment.CenterEnd).padding(12.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 3.dp,
        ) {
            Column {
                TextButton(onClick = { dispatch(MapEvent.Control(MapCommand.ZoomIn)) },
                    modifier = Modifier.testTag("map-zoom-in")) { Text("Збільшити +") }
                TextButton(onClick = { dispatch(MapEvent.Control(MapCommand.ZoomOut)) },
                    modifier = Modifier.testTag("map-zoom-out")) { Text("Зменшити −") }
                TextButton(onClick = { dispatch(MapEvent.Control(MapCommand.Recenter)) },
                    modifier = Modifier.testTag("map-recenter")) { Text("Центр · Київ") }
                TextButton(onClick = { dispatch(MapEvent.SearchRequested) },
                    modifier = Modifier.testTag("map-search")) { Text("Пошук / маршрут") }
            }
        }
    }
}
