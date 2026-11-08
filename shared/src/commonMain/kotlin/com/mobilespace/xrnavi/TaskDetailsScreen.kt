package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun TaskDetailsScreen(layout: AccountLayout, actions: AdditionalActions) {
    AdditionalColumns(layout, primary = {
        AdditionalBadge("Приклад · не активний рейс")
        AdditionalCard {
            AdditionalTitle("Київ → Львів", large = true)
            AdditionalBody("Розвантаження · склад «Захід»")
            AdditionalBody("вул. Городоцька, 359 · ворота 2")
            AdditionalBody("6 автомобілів · 36,8 т · не ADR · приклад")
        }
        AdditionalMetrics(
            layout, listOf(
                "Прибуття, ETA · приклад" to "17:30",
                "Вікно приймання" to "17:00–18:00"
            )
        )
        AdditionalNotice(
            "Затримка +20 хв · приклад",
            "Жодного повідомлення диспетчеру не надсилалось.", warning = true
        )
    }, secondary = {
        AdditionalAction("Контакт приймання", "Контакт із сервера не отримано") {
            actions.unavailable("Контакт складу")
        }
        AdditionalAction("Диспетчер", "Контакт із сервера не отримано") {
            actions.unavailable("Зв'язок із диспетчером")
        }
        AdditionalNotice(
            "Під'їзд потребує перевірки",
            "Останні 4,2 км не перевірені для автовоза.", warning = true
        )
        AdditionalButton("Я прибув", secondary = true) { actions.unavailable("Підтвердження прибуття") }
        AdditionalButton("Завершити", enabled = false) { actions.unavailable("Завершення рейсу") }
        AdditionalAction("Повідомити нову затримку", "Причина · новий очікуваний час") {
            actions.unavailable("Повідомлення про затримку")
        }
    })
}
