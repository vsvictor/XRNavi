package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun InvitationScreen(layout: AccountLayout, actions: AdditionalActions) {
    val model: InvitationViewModel = viewModel { InvitationViewModel() }
    val state by model.bloc.state.collectAsStateWithLifecycle()
    DisposableEffect(model) { onDispose { model.bloc.dispatch(InvitationEvent.Left) } }
    AdditionalColumns(layout, primary = {
        AdditionalHeading("Код запрошення")
        OutlinedTextField(
            value = state.code,
            onValueChange = { model.bloc.dispatch(InvitationEvent.Code(it)) },
            placeholder = { Text("Код від адміністратора") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Код не перевірено · сервіс запрошень не підключено") }
        )
        AdditionalCard {
            AdditionalTitle("Dnipro Logistics")
            AdditionalBody("Приклад запрошення на роль: водій")
            AdditionalBadge("Політика автопарку 3.2 · не отримана із сервера")
        }
    }, secondary = {
        AdditionalHeading("Що зміниться після приєднання")
        AdditionalNotice(
            "Організація керує",
            "Габаритами робочої техніки, дозволеними дорогами та лімітом швидкості.",
            warning = true
        )
        AdditionalCard {
            AdditionalTitle("Ви налаштовуєте")
            AdditionalBody("Мову, голосові підказки, вигляд мапи та сповіщення.")
        }
        AdditionalCard {
            AdditionalTitle("Особисті маршрути — приватні")
            AdditionalBody("Передачу даних і політику організації необхідно перевірити перед прийняттям.")
        }
        AdditionalConsent(
            "Ознайомився з правилами організації та умовами передачі даних",
            state.consent
        ) { model.bloc.dispatch(InvitationEvent.Consent(it)) }
        AdditionalButton(
            "Прийняти запрошення",
            enabled = state.code.isNotBlank() && state.consent
        ) { model.bloc.dispatch(InvitationEvent.Accept) }
        AdditionalButton("Не зараз · залишитися в особистому режимі", secondary = true) {
            actions.back()
        }
    })
    AdditionalNoticeDialog(state.notice) { model.bloc.dispatch(InvitationEvent.DismissNotice) }
}
