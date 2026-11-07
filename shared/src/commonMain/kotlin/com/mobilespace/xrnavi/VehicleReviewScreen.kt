package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun VehicleReviewScreen(layout: AccountLayout, actions: AdditionalActions) {
    val vehicle: VehicleModelViewModel = viewModel { VehicleModelViewModel() }
    val review: VehicleReviewViewModel = viewModel { VehicleReviewViewModel() }
    val state by vehicle.bloc.state.collectAsStateWithLifecycle()
    val dimensions by review.bloc.state.collectAsStateWithLifecycle()
    AdditionalColumns(layout, primary = {
        AdditionalVehicleImage()
        AdditionalCard {
            AdditionalTitle("Ford ${state.model} · ${state.year}")
            AdditionalBody(state.trim)
            AdditionalBody("Приклад вибору з каталогу · не доданий до гаража")
        }
    }, secondary = {
        AdditionalHeading("Звірте параметри з техпаспортом · локальна чернетка")
        AdditionalChoices(listOf("Mustang", "Focus", "Explorer"), state.model) {
            vehicle.bloc.dispatch(VehicleModelEvent.Model(it))
        }
        OutlinedTextField(
            state.year, { vehicle.bloc.dispatch(VehicleModelEvent.Year(it)) },
            label = { Text("Рік випуску") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            dimensions.height, { review.bloc.dispatch(VehicleReviewEvent.Height(it)) },
            label = { Text("Висота, м") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            dimensions.length, { review.bloc.dispatch(VehicleReviewEvent.Length(it)) },
            label = { Text("Довжина, м") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            dimensions.mass, { review.bloc.dispatch(VehicleReviewEvent.Mass(it)) },
            label = { Text("Повна маса, т") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        AdditionalNotice(
            "Характеристики не підтверджені",
            "Не використовуйте ілюстративний профіль для перевірки обмежень маршруту.",
            warning = true
        )
        AdditionalButton("Зберегти транспорт") { actions.unavailable("Збереження нового транспорту") }
        AdditionalButton("До каталогу", secondary = true) {
            actions.open(AdditionalScreen.VehicleCatalog)
        }
    })
}
