package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun DiagnosticsScreen(layout: AccountLayout, actions: AdditionalActions) {
    AdditionalColumns(layout, primary = {
        AdditionalHeading("Екран автомобіля")
        AdditionalCard {
            AdditionalTitle("Android Auto")
            AdditionalBody("Заплановано · інтеграція ще не доступна")
            AdditionalTitle("Apple CarPlay")
            AdditionalBody("Заплановано · інтеграція ще не доступна")
        }
        AdditionalHeading("Навігація у фоні")
        AdditionalAction("Фонова геолокація", "Дозвіл ОС не перевірено") {
            actions.unavailable("Налаштування фонової геолокації")
        }
        AdditionalAction("Голосові підказки", "Сервіс не підключено") {
            actions.unavailable("Налаштування голосу")
        }
        AdditionalAction("Сповіщення", "Дозвіл ОС не перевірено") {
            actions.unavailable("Налаштування сповіщень")
        }
        AdditionalAction("Енергозбереження", "Стан пристрою не перевірено") {
            actions.unavailable("Налаштування енергозбереження")
        }
        AdditionalNotice(
            "Менше батареї — рідше оновлення",
            "Енергозбереження ОС може затримувати GPS і фонові підказки.", warning = true
        )
    }, secondary = {
        AdditionalHeading("Діагностика GPS")
        AdditionalCard {
            AdditionalTitle("Немає підтвердженого виміру")
            AdditionalBody("Точність, джерело й вік позиції не отримано з ОС.")
        }
        AdditionalButton("Відкрити налаштування пристрою", secondary = true) {
            actions.unavailable("Перехід до системних налаштувань")
        }
        AdditionalHeading("Приклади станів інтерфейсу · не операції ОС або сервера")
        AdditionalScreen.entries.filter { it.statePreview }.forEach { preview ->
            AdditionalAction(preview.title, "${preview.designId} · локальний приклад") {
                actions.open(preview)
            }
        }
    })
}
