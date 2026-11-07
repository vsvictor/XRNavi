package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun RouteFailedScreen(actions: AdditionalActions) {
    AdditionalMapPreview(
        actions, origin = "Київ · приклад",
        destination = "Львів · площа Ринок, 1 · приклад"
    ) {
        AdditionalTitle("Ford Mustang GT")
        AdditionalCard {
            AdditionalTitle("Маршрут не побудовано")
            AdditionalBody("Перевірте з’єднання або змініть адреси. У цьому прикладі серверний розрахунок не виконувався.")
        }
        AdditionalButton("Повторити · приклад") { actions.open(AdditionalScreen.RouteBuilding) }
        AdditionalButton("Редагувати маршрут", secondary = true) {
            actions.open(AdditionalScreen.SearchEmpty)
        }
    }
}
