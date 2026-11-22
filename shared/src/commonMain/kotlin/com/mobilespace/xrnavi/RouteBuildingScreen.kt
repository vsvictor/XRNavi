package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun RouteBuildingScreen(actions: AdditionalActions) {
    AdditionalMapPreview(
        actions, origin = "Київ · приклад",
        destination = "Львів · площа Ринок, 1 · приклад"
    ) {
        AdditionalTitle("Ford Mustang GT")
        CircularProgressIndicator(Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary)
        AdditionalTitle("Будуємо маршрут")
        AdditionalBody("Приклад індикації. Маршрутизатор не підключено; розрахунок не запущено.")
        AdditionalButton("Переглянути стан помилки") { actions.open(AdditionalScreen.RouteFailed) }
        AdditionalButton("Скасувати побудову", secondary = true, onClick = actions.back)
    }
}
