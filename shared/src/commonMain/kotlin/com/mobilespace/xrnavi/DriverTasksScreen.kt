package com.mobilespace.xrnavi

import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun DriverTasksScreen(layout: AccountLayout, actions: AdditionalActions) {
    val model: DriverTasksViewModel = viewModel { DriverTasksViewModel() }
    val state by model.bloc.state.collectAsStateWithLifecycle()
    AdditionalColumns(layout, primary = {
        AdditionalChoices(
            listOf("Активні · 2", "Завершені · 1"),
            if (state.completedTasks) "Завершені · 1" else "Активні · 2"
        ) {
            model.bloc.dispatch(DriverTasksEvent.CompletedTasks(it.startsWith("Завершені")))
        }
        AdditionalPolicy(
            "MAN TGX · автовоз",
            "Приклад призначеного транспорту · не підтверджено"
        )
        AdditionalNotice(
            "Робоча геолокація не передається",
            "Сервіс завдань і передача позиції не підключені. Ці приклади не є призначеними рейсами."
        )
    }, secondary = {
        if (!state.completedTasks) {
            AdditionalCard(selected = true) {
                AdditionalBody("DL-204 · приклад рейсу")
                AdditionalTitle("Київ → Львів", large = true)
                AdditionalBody("6 авто · склад «Захід» · 17:00–18:00")
                AdditionalMetrics(
                    layout,
                    listOf("ETA · приклад" to "17:30", "Затримка" to "+20 хв")
                )
                TextButton(onClick = { actions.open(AdditionalScreen.TaskDetails) }) {
                    Text("Відкрити завдання")
                }
            }
            AdditionalCard {
                AdditionalBody("DL-205 · приклад нового завдання")
                AdditionalTitle("Львів → Київ", large = true)
                AdditionalBody("4 авто · MAN TGX · термінал «Захід»")
                AdditionalButton("Прийняти завдання") { actions.unavailable("Прийняття завдання") }
            }
        } else AdditionalAction("DL-198 · Київ → Житомир", "Приклад завершеного рейсу") {
            actions.open(AdditionalScreen.TripSummary)
        }
    })
}
