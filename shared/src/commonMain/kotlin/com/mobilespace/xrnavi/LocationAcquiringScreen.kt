package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LocationAcquiringScreen(actions: AdditionalActions) {
    AdditionalMapPreview(actions) {
        CircularProgressIndicator(Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary)
        AdditionalTitle("Визначаємо місце")
        AdditionalBody("Очікуємо надійний сигнал GPS. Це лише приклад; позицію та дозволи ОС не запитуємо.")
        AdditionalButton("Скасувати визначення", secondary = true, onClick = actions.back)
        AdditionalButton("Задати початок вручну") { actions.section(DemoSection.Map) }
    }
}
