package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

/** Shared body of S49 (denied) and S50 (service disabled); each has its own screen file. */
@Composable
internal fun LocationUnavailableContent(screen: AdditionalScreen, actions: AdditionalActions) {
    AdditionalMapPreview(actions) {
        AdditionalCard {
            AdditionalTitle(screen.title)
            AdditionalBody("Позицію не отримано. Демонстраційний стан не означає, що фактичний доступ або служба ОС вимкнені.")
        }
        AdditionalButton("Відкрити налаштування пристрою", secondary = true) {
            actions.unavailable("Системні налаштування")
        }
        AdditionalButton("Задати початок вручну") { actions.section(DemoSection.Map) }
    }
}
