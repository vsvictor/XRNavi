package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun PrivacyScreen(layout: AccountLayout, actions: AdditionalActions) {
    AdditionalColumns(layout, primary = {
        AdditionalCard {
            AdditionalBadge("Особистий простір")
            AdditionalTitle("Особисті поїздки — лише ваші", large = true)
            AdditionalBody("У цьому деморежимі історія та маршрути не передаються організації.")
        }
        AdditionalHeading("Робочий рейс · приклад політики")
        AdditionalPolicy("Передача робочої геолокації", "Лише від старту до завершення рейсу")
        AdditionalNotice(
            "Це правило організації",
            "Фактичну політику не отримано. Обов'язкове відстеження не можна удавано вимкнути перемикачем.",
            warning = true
        )
    }, secondary = {
        AdditionalHeading("Зберігання даних")
        AdditionalCard {
            AdditionalTitle("Робоча геолокація · 30 днів")
            AdditionalBody("Робочі звіти — 180 днів · приклад із дизайну, не підтверджена політика")
            AdditionalTitle("Особиста історія")
            AdditionalBody("Історію в аккаунті й строки зберігання не перевірено.")
        }
        AdditionalAction(
            "Експортувати особисті дані",
            "Отримати копію профілю та власних поїздок"
        ) { actions.unavailable("Експорт даних") }
        AdditionalAction("Видалити особисту історію", "Не видаляє робочі записи організації") {
            actions.unavailable("Видалення історії")
        }
        AdditionalAction(
            "Видалення аккаунта", "Наслідки та підтвердження на наступному кроці",
            destructive = true
        ) { actions.open(AdditionalScreen.DeleteAccount) }
    })
}
