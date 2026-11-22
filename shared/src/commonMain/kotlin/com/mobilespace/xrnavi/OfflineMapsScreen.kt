package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun OfflineMapsScreen(layout: AccountLayout, actions: AdditionalActions) {
    AdditionalColumns(layout, primary = {
        AdditionalCard {
            AdditionalTitle("Мапи на пристрої")
            AdditionalBody("Фактичне сховище не підключено · завантажених регіонів не перевірено")
            LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth())
        }
        AdditionalNotice(
            "Мапа ≠ офлайн-перебудова",
            "Офлайн-перегляд, нові маршрути й перебудова без інтернету не підключені.",
            warning = true
        )
    }, secondary = {
        AdditionalHeading("Регіони · приклад дизайну")
        AdditionalCard {
            AdditionalTitle("Київська область")
            AdditionalBody("248 МБ · розмір із макета, файл не завантажено")
            AdditionalBadge("Не завантажено")
        }
        AdditionalCard {
            AdditionalTitle("Коридор Київ → Львів")
            AdditionalBody("612 МБ · приклад регіону")
            TextButton(onClick = { actions.unavailable("Оновлення регіону") }) {
                Text("Оновити регіон")
            }
        }
        AdditionalCard {
            AdditionalTitle("Львівська область")
            AdditionalBody("Завантаження не розпочато")
            LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth())
            AdditionalButton("Завантажити", secondary = true) {
                actions.unavailable("Завантаження мап")
            }
        }
        AdditionalAction("Кеш активного маршруту", "Кеш не створено") {
            actions.unavailable("Перегляд кешу")
        }
        AdditionalButton("Завантажити інший регіон", secondary = true) {
            actions.unavailable("Вибір регіону для завантаження")
        }
    })
}
