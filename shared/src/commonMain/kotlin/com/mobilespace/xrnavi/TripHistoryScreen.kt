package com.mobilespace.xrnavi

import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun TripHistoryScreen(layout: AccountLayout, actions: AdditionalActions) {
    val model: TripHistoryViewModel = viewModel { TripHistoryViewModel() }
    val state by model.bloc.state.collectAsStateWithLifecycle()
    AdditionalChoices(HistoryFilter.entries.map { it.label }, state.filter.label) {
        model.bloc.dispatch(TripHistoryEvent.Filter(HistoryFilter.entries.first { f -> f.label == it }))
    }
    AdditionalNotice(
        "Лише приклади поїздок",
        "Історію із сервера не отримано. Жоден приклад не є активним робочим рейсом."
    )
    AdditionalColumns(layout, primary = {
        if (state.filter != HistoryFilter.Work) {
            AdditionalHeading("Особиста поїздка · приклад")
            AdditionalCard {
                AdditionalTitle("Київ → Львів")
                AdditionalBody("Ford Mustang GT")
                AdditionalRouteIllustration()
                AdditionalBody("09:41–15:53 · 540 км · 6 год 12 хв")
                TextButton(onClick = { actions.open(AdditionalScreen.TripSummary) }) {
                    Text("Підсумок поїздки")
                }
            }
        }
    }, secondary = {
        if (state.filter != HistoryFilter.Personal) {
            AdditionalHeading("Робочі поїздки · приклад")
            AdditionalAction(
                "DL-198 · Київ → Житомир",
                "142 км · 2 год 25 хв · не підтверджено"
            ) { actions.open(AdditionalScreen.TaskDetails) }
        }
        AdditionalAction(
            "Класифікація поїздки",
            "Особистий / робочий режим обирається до старту"
        ) { actions.unavailable("Зміна класифікації") }
    })
}
