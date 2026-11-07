package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun CompanyScreen(layout: AccountLayout, actions: AdditionalActions) {
    val model: CompanyViewModel = viewModel { CompanyViewModel() }
    val state by model.bloc.state.collectAsStateWithLifecycle()
    AdditionalColumns(layout, primary = {
        AdditionalCard {
            AdditionalTitle("Dnipro Logistics")
            AdditionalBody("Приклад організації · політика автопарку 3.2")
            AdditionalBadge("Успадковано · лише для читання")
        }
        AdditionalHeading("Правила організації")
        AdditionalCard {
            AdditionalPolicy(
                "Профілі техніки та габарити",
                "MAN TGX · 4,0 × 2,55 × 16,5 м · 40 т"
            )
            HorizontalDivider()
            AdditionalPolicy("Дозволені дороги", "Без ґрунтових доріг · обхід обмежень")
            HorizontalDivider()
            AdditionalPolicy(
                "Максимальна швидкість",
                "90 км/год · нижчий дорожній ліміт важливіший"
            )
        }
    }, secondary = {
        AdditionalHeading("Ваші налаштування · локальний приклад")
        AdditionalCard {
            AdditionalSwitch(
                "Сповіщення диспетчера", "Нові рейси та зміни завдань",
                state.dispatcherNotifications
            ) { model.bloc.dispatch(CompanyEvent.DispatcherNotifications(it)) }
            HorizontalDivider()
            AdditionalSwitch("Голосові підказки", "Українська · Олена", state.voicePrompts) {
                model.bloc.dispatch(CompanyEvent.VoicePrompts(it))
            }
        }
        AdditionalAction("Приєднатися до команди", "Ввести код запрошення") {
            actions.open(AdditionalScreen.Invitation)
        }
        AdditionalButton("Запросити зміну параметрів", secondary = true) {
            actions.unavailable("Запит адміністратору")
        }
        AdditionalBody("Геолокація не передається. Організацію та робочу роль не підтверджено.")
    })
    AdditionalNoticeDialog(state.notice) { model.bloc.dispatch(CompanyEvent.DismissNotice) }
}
