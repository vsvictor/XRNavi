package com.mobilespace.xrnavi

import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun VehicleModelScreen(layout: AccountLayout, actions: AdditionalActions) {
    val model: VehicleModelViewModel = viewModel { VehicleModelViewModel() }
    val state by model.bloc.state.collectAsStateWithLifecycle()
    AdditionalColumns(layout, primary = {
        AdditionalVehicleImage()
        AdditionalTitle("Ford ${state.model}", large = true)
        AdditionalBody("Ілюстративний автомобіль · характеристики не підтверджені")
    }, secondary = {
        AdditionalHeading("Модель")
        AdditionalChoices(listOf("Mustang", "Focus", "Explorer"), state.model) {
            model.bloc.dispatch(VehicleModelEvent.Model(it))
        }
        AdditionalHeading("Рік випуску")
        AdditionalChoices(listOf("2022", "2023", "2024", "2025"), state.year) {
            model.bloc.dispatch(VehicleModelEvent.Year(it))
        }
        AdditionalHeading("Комплектація")
        listOf("GT · 5.0 V8 · автоматична", "EcoBoost · 2.3").forEach { trim ->
            AdditionalCard(selected = state.trim == trim) {
                TextButton(onClick = { model.bloc.dispatch(VehicleModelEvent.Trim(trim)) }) {
                    Text(trim)
                }
                AdditionalBody("Приклад · параметри не підтверджено")
            }
        }
        AdditionalNotice(
            "Перевірте перед збереженням",
            "Зображення не підтверджує комплектацію. Звірте параметри з техпаспортом; це не ліцензований каталог."
        )
        AdditionalButton("Далі · перевірити параметри") {
            actions.open(AdditionalScreen.VehicleReview)
        }
    })
}
