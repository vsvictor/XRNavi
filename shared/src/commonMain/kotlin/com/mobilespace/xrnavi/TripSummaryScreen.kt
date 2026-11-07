package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun TripSummaryScreen(layout: AccountLayout, actions: AdditionalActions) {
    AdditionalColumns(layout, primary = {
        AdditionalBadge("Приклад завершеної поїздки")
        AdditionalRouteIllustration()
    }, secondary = {
        AdditionalMetrics(
            layout,
            listOf("Відстань" to "540 км", "Загальний час" to "6 год 12 хв")
        )
        AdditionalMetrics(layout, listOf("У русі" to "5 год 54 хв", "Зупинки · 1" to "18 хв"))
        AdditionalCard {
            AdditionalTitle("Ford Mustang GT")
            AdditionalBody("Київ · 09:41 → Львів · 15:53 · особистий режим")
        }
        AdditionalNotice(
            "Не збережено в історії",
            "Це приклад підсумку з макета, а не результат виконаної навігації."
        )
        AdditionalButton("Повернутися до мапи") { actions.section(DemoSection.Map) }
    })
}
