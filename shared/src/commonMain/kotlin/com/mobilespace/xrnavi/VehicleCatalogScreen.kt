package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun VehicleCatalogScreen(layout: AccountLayout, actions: AdditionalActions) {
    val model: VehicleCatalogViewModel = viewModel { VehicleCatalogViewModel() }
    val state by model.bloc.state.collectAsStateWithLifecycle()
    AdditionalColumns(layout, primary = {
        OutlinedTextField(
            state.query, { model.bloc.dispatch(VehicleCatalogEvent.Query(it)) },
            placeholder = { Text("Пошук марки або моделі") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        AdditionalHeading("Клас транспорту")
        AdditionalChoices(
            GarageFilter.entries.filter { it != GarageFilter.All }.map { it.label },
            state.vehicleClass.label
        ) { label ->
            model.bloc.dispatch(
                VehicleCatalogEvent.VehicleClass(GarageFilter.entries.first { it.label == label })
            )
        }
        AdditionalNotice(
            "Не знайшли свій транспорт?",
            "Довідник має обмежене покриття. Підключеного глобального каталогу немає."
        )
        AdditionalButton("Ввести транспорт вручну", secondary = true) {
            actions.open(AdditionalScreen.VehicleReview)
        }
    }, secondary = {
        AdditionalHeading("Оберіть марку · приклади")
        val brands = when (state.vehicleClass) {
            GarageFilter.Passenger -> listOf("Ford", "Volkswagen", "Toyota", "BMW")
            GarageFilter.Cargo -> listOf("MAN", "Volvo", "Mercedes-Benz")
            else -> listOf("Евакуатор", "Автовоз", "Тягач")
        }
        val results = brands.filter {
            it.contains(state.query.trim(), ignoreCase = true) ||
                    (it == "Ford" && "Mustang Focus Explorer".contains(
                        state.query.trim(),
                        ignoreCase = true
                    ))
        }
        if (results.isEmpty()) AdditionalCard { AdditionalBody("За цим запитом прикладів немає.") }
        results.forEach { brand ->
            AdditionalAction(
                brand,
                if (brand == "Ford") "Mustang · Focus · Explorer" else "Приклад марки"
            ) {
                if (brand == "Ford") actions.open(AdditionalScreen.VehicleModel)
                else actions.unavailable("Каталог $brand")
            }
        }
    })
}
