package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
internal fun ColumnScope.LocationEducationScreen(actions: AdditionalActions) {
    ProductWordmark()
    AdditionalBadge("Ваш перший маршрут")
    AdditionalTitle("Почнімо з вашого місця", large = true)
    AdditionalBody("З поточною позицією XRNavi підставить початок маршруту та допоможе зорієнтуватися в дорозі.")
    AdditionalCard {
        AdditionalBody("Лише коли потрібно — для визначення початку й навігації.")
        AdditionalBody("Особисті поїздки залишаються приватними. Робочі правила діють лише під час робочого рейсу.")
        AdditionalBody("Без обов'язкового фонового доступу. Маршрут можна спланувати й вручну.")
    }
    AdditionalButton("Надати доступ до геолокації") {
        actions.unavailable("Системний запит геолокації")
    }
    AdditionalButton("Продовжити без геолокації", secondary = true) {
        actions.section(DemoSection.Map)
    }
    AdditionalAction(
        "Переглянути стан визначення місця",
        "Локальний приклад · GPS не запитується"
    ) { actions.open(AdditionalScreen.LocationAcquiring) }
}
