package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

/** Shared body of S38 (explanation) and S17 (unverified route); each has its own screen file. */
@Composable
internal fun RouteRestrictionContent(
    layout: AccountLayout,
    actions: AdditionalActions,
    unverified: Boolean
) {
    AdditionalColumns(
        layout,
        primary = {
            AdditionalRouteIllustration()
            AdditionalMetrics(
                layout, listOf(
                    "Плановий шлях · приклад" to "552 км",
                    "Орієнтовний час" to "7 год 10 хв"
                )
            )
        },
        secondary = {
            AdditionalNotice(
                if (unverified) "Безпеку для автовоза не підтверджено"
                else "Останні 4,2 км не перевірені",
                "М-06 → склад «Захід», вул. Городоцька, 359. Не починайте рейс за неперевіреним під'їздом.",
                warning = true
            )
            AdditionalCard(selected = !unverified) {
                AdditionalTitle("М-06 · об'їзд центру Львова", large = true)
                AdditionalBody("Приклад пояснення: міст із лімітом 3,8 м виключено; висота MAN TGX — 4,0 м.")
            }
            AdditionalMetrics(
                layout, listOf(
                    "Повна маса · факт / max" to "36,8 / 40 т",
                    "Вісь · факт / max" to "10,8 / 11,5 т"
                )
            )
            AdditionalBadge("Пояснення · не сертифікація безпеки")
            AdditionalBody("Профіль і обмеження не отримані з сервера. Легковий маршрут не є безпечною альтернативою для вантажівки.")
            AdditionalButton("Переглянути альтернативи") {
                actions.unavailable("Побудова альтернативного маршруту")
            }
            AdditionalButton("Уточнити під'їзд із диспетчером", secondary = true) {
                actions.unavailable("Зв'язок із диспетчером")
            }
            if (!unverified) AdditionalAction(
                "Вантажна перевірка",
                "Чому маршрут не можна використовувати"
            ) { actions.open(AdditionalScreen.RouteUnverified) }
        })
}
