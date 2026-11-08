package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleScreen(state: AddVehicleState, dispatch: (AddVehicleEvent) -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("add-vehicle"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            when (state.step) {
                AddVehicleStep.Class -> "Клас транспорту"
                AddVehicleStep.Make -> "Марка · ${state.selectedClass?.name.orEmpty()}"
                AddVehicleStep.Model -> "Модель · ${state.selectedMake?.name.orEmpty()}"
                AddVehicleStep.Details -> "${state.selectedMake?.name.orEmpty()} ${state.selectedModel?.name.orEmpty()}"
            },
            style = MaterialTheme.typography.titleLarge,
        )
        if (state.loading) CircularProgressIndicator(Modifier.testTag("add-vehicle-loading"))
        if (state.error.isNotEmpty()) {
            Text(state.error, Modifier.testTag("add-vehicle-error"), color = MaterialTheme.colorScheme.error)
            OutlinedButton(
                onClick = {
                    dispatch(if (state.step == AddVehicleStep.Details) AddVehicleEvent.Submit else AddVehicleEvent.Retry)
                },
                modifier = Modifier.testTag("add-vehicle-retry"),
            ) { Text("Повторити") }
        }
        when (state.step) {
            AddVehicleStep.Class -> state.classes.forEach {
                Choice(it.name, "add-class-${it.id}", false) { dispatch(AddVehicleEvent.SelectClass(it.id)) }
            }
            AddVehicleStep.Make -> state.makes.forEach {
                Choice(it.name, "add-make-${it.id}", false) { dispatch(AddVehicleEvent.SelectMake(it.id)) }
            }
            AddVehicleStep.Model -> state.models.forEach {
                Choice(it.name, "add-model-${it.id}", false) { dispatch(AddVehicleEvent.SelectModel(it.id)) }
            }
            AddVehicleStep.Details -> {
                Text("Рік")
                state.selectedModel?.years?.forEach {
                    Choice("$it", "add-year-$it", state.year == it) { dispatch(AddVehicleEvent.SelectYear(it)) }
                }
                Text("Комплектація")
                state.selectedModel?.trims?.forEach {
                    Choice(it.name, "add-trim-${it.id}", state.trim?.id == it.id) {
                        dispatch(AddVehicleEvent.SelectTrim(it.id))
                    }
                }
                Button(
                    onClick = { dispatch(AddVehicleEvent.Submit) },
                    enabled = state.canSubmit,
                    modifier = Modifier.testTag("add-vehicle-submit"),
                ) { Text("Додати") }
            }
        }
        OutlinedButton(
            onClick = { dispatch(AddVehicleEvent.Back) },
            modifier = Modifier.testTag("add-vehicle-back"),
        ) { Text("Назад") }
    }
}
