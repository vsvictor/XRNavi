package com.mobilespace.xrnavi

import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun DeleteAccountScreen(layout: AccountLayout, actions: AdditionalActions) {
    val model: DeleteAccountViewModel = viewModel { DeleteAccountViewModel() }
    val state by model.bloc.state.collectAsStateWithLifecycle()
    DisposableEffect(model) { onDispose { model.bloc.dispatch(DeleteAccountEvent.Left) } }
    AdditionalColumns(layout, primary = {
        AdditionalCard(destructive = true) {
            AdditionalTitle("Ви втратите доступ до XRNavi", large = true)
            AdditionalBody("Це видалення особистого аккаунта, а не всіх даних компанії.")
        }
        AdditionalHeading("Що буде видалено")
        AdditionalCard {
            AdditionalTitle("Особистий профіль та історія")
            AdditionalBody("Поїздки, збережені місця й особистий гараж більше не будуть доступні.")
            AdditionalTitle("Вхід на всіх пристроях")
            AdditionalBody("Сеанси завершаться. Локальні мапи та кеш потрібно очистити на кожному пристрої.")
        }
        AdditionalNotice(
            "Робочі записи зберігаються окремо",
            "Реальні строки та правила організації необхідно перевірити перед видаленням.",
            warning = true
        )
    }, secondary = {
        AdditionalAction("Спочатку експортувати дані", "Отримати копію особистих поїздок") {
            actions.unavailable("Експорт даних")
        }
        AdditionalConsent(
            "Розумію наслідки та окреме зберігання робочих даних.",
            state.consent
        ) { model.bloc.dispatch(DeleteAccountEvent.Consent(it)) }
        AdditionalButton("Скасувати · залишити аккаунт", secondary = true) { actions.back() }
        AdditionalButton(
            "Підтвердити запит на видалення", destructive = true,
            enabled = state.consent
        ) { model.bloc.dispatch(DeleteAccountEvent.Confirm) }
    })
    AdditionalNoticeDialog(state.notice) { model.bloc.dispatch(DeleteAccountEvent.DismissNotice) }
}
