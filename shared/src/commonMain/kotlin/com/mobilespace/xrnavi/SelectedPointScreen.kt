package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun SelectedPointScreen(actions: AdditionalActions) {
    AdditionalMapPreview(actions, destination = "Львів · площа Ринок, 1 · приклад") {
        AdditionalTitle("Львів · площа Ринок, 1", large = true)
        AdditionalBody("Точка з макета · не визначена за координатами натискання")
        AdditionalButton("Переглянути побудову маршруту") {
            actions.open(AdditionalScreen.RouteBuilding)
        }
        AdditionalButton("Змінити точку", secondary = true) {
            actions.open(AdditionalScreen.SearchEmpty)
        }
    }
}
