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
internal fun AddVehicleGeneralTab(state: AddVehicleState, dispatch: (AddVehicleEvent) -> Unit) {
    Column(
        Modifier.fillMaxWidth().testTag("add-vehicle-general"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.step != AddVehicleStep.Review) {
            AddVehicleLookupButtons(
                enabled = !state.loading,
                onVin = { dispatch(AddVehicleEvent.OpenVinDialog) },
            )
            if (state.vinDialogOpen) AddVehicleVinDialog(state, dispatch)
        }
        if (state.step == AddVehicleStep.Review) {
            Text(
                when (state.step) {
                    AddVehicleStep.Class -> "Додати транспорт"
                    AddVehicleStep.Model -> "Модель"
                    AddVehicleStep.Details -> "Рік і комплектація"
                    AddVehicleStep.Review -> "Перевірка параметрів"
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                when (state.step) {
                    AddVehicleStep.Class -> "Гараж · крок 1 із 3"
                    AddVehicleStep.Model -> "Крок 2 із 3 · ${state.selectedMake?.name.orEmpty()}"
                    AddVehicleStep.Details -> "Крок 2 із 3 · ${state.selectedMake?.name.orEmpty()} ${state.selectedModel?.name.orEmpty()}"
                    AddVehicleStep.Review -> "Крок 3 із 3 · перевірка параметрів"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.step == AddVehicleStep.Details && state.error.isNotEmpty()) {
            Text(
                state.error,
                Modifier.fillMaxWidth().testTag("add-vehicle-error"),
                color = MaterialTheme.colorScheme.error,
            )
            OutlinedButton(
                onClick = {
                    dispatch(if (state.step == AddVehicleStep.Details) AddVehicleEvent.Submit else AddVehicleEvent.Retry)
                },
                enabled = !state.loading,
                modifier = Modifier.testTag("add-vehicle-retry"),
            ) { Text("Повторити") }
        }
        when (state.step) {
            AddVehicleStep.Class -> {
                if (state.classes.isEmpty() && !state.loading && state.error.isEmpty()) {
                    Text(
                        "Каталог не містить доступних класів.",
                        Modifier.testTag("add-class-empty"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.classes.isNotEmpty()) {
                    val availableGroups = state.classes.map { it.catalogGroup() }.distinct()
                    AddVehicleCatalogClassTabs(
                        selectedGroup = state.selectedCatalogGroup,
                        availableGroups = availableGroups,
                        enabled = !state.loading && !state.makeLoading,
                        onSelected = { dispatch(AddVehicleEvent.SelectCatalogGroup(it)) },
                    )
                    val groupClasses = state.classes.filter {
                        it.catalogGroup() == state.selectedCatalogGroup
                    }
                    val showClassOptions = groupClasses.size > 1 ||
                        groupClasses.singleOrNull()?.isGroupLabel() == false
                    if (showClassOptions) {
                        AddVehicleCatalogClassOptions(
                            classes = groupClasses,
                            selectedClassId = state.selectedClass?.id,
                            enabled = !state.loading && !state.makeLoading,
                            onSelected = { dispatch(AddVehicleEvent.SelectClass(it)) },
                        )
                    }
                }
                val items = state.filteredMakes
                AddVehicleCatalogSectionHeading(
                    title = "Або оберіть марку",
                )
                AddVehicleDropdown(
                    value = state.searchQuery,
                    placeholder = "Введіть марку авто",
                    selection = state.makeSelection,
                    onSelectionChanged = { dispatch(AddVehicleEvent.MakeSelectionChanged(it)) },
                    onValueChange = { dispatch(AddVehicleEvent.SearchChanged(it)) },
                    enabled = !state.loading,
                    options = items.map { it.id to it.name },
                    expanded = state.makeDropdownExpanded,
                    tag = "add-vehicle-make-dropdown",
                    onExpandedChange = { dispatch(AddVehicleEvent.MakeDropdownChanged(it)) },
                    onSelected = { dispatch(AddVehicleEvent.SelectMake(it)) },
                    onFocusChanged = { dispatch(AddVehicleEvent.MakeFocusChanged(it)) },
                )
                if (state.makeLoading) {
                    CircularProgressIndicator(Modifier.testTag("add-vehicle-make-loading"))
                }
                if (state.makeError.isNotEmpty()) {
                    Text(
                        state.makeError,
                        Modifier.fillMaxWidth().testTag("add-vehicle-make-error"),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (items.isEmpty() && !state.loading && !state.makeLoading && state.makeError.isEmpty()) {
                    Text(
                        if (!state.makeSearched) "Оберіть поле для завантаження марок."
                        else "За цим запитом марок не знайдено.",
                        Modifier.testTag("add-make-empty"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!state.loading && state.error.isEmpty()) {
                    AddVehicleCatalogNotice(
                        title = "Не знайшли потрібну марку?",
                        message = "Каталог показує дані, отримані від сервера. " +
                                "Звірте марку, модель і параметри транспорту з документами.",
                    )
                    Text(
                        "Марки, моделі й параметри доступні лише в межах підключеного каталогу.",
                        Modifier.fillMaxWidth().testTag("add-vehicle-catalog-disclaimer"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            AddVehicleStep.Model, AddVehicleStep.Details -> {
                AddVehicleCatalogSectionHeading("Або оберіть марку")
                AddVehicleDropdown(
                    value = state.selectedMake?.name.orEmpty(),
                    selection = state.makeSelection,
                    inputEffect = state.inputEffect?.takeIf { it == AddVehicleInputEffect.FocusMake },
                    onInputEffectHandled = { dispatch(AddVehicleEvent.InputEffectHandled(it)) },
                    onSelectionChanged = { dispatch(AddVehicleEvent.MakeSelectionChanged(it)) },
                    placeholder = "Введіть марку авто",
                    onValueChange = { dispatch(AddVehicleEvent.MakeChanged(it)) },
                    enabled = !state.loading,
                    options = state.makeResults.map { it.id to it.name },
                    expanded = state.makeDropdownExpanded,
                    tag = "add-vehicle-make-dropdown",
                    onExpandedChange = { dispatch(AddVehicleEvent.MakeDropdownChanged(it)) },
                    onSelected = { dispatch(AddVehicleEvent.SelectMake(it)) },
                    onFocusChanged = { dispatch(AddVehicleEvent.MakeFocusChanged(it)) },
                )
                val items = state.models.filter {
                    state.selectedModel != null ||
                        it.name.contains(state.searchQuery.trim(), ignoreCase = true)
                }
                AddVehicleDropdown(
                    value = state.searchQuery,
                    placeholder = "Введіть модель авто",
                    selection = state.modelSelection,
                    onSelectionChanged = { dispatch(AddVehicleEvent.ModelSelectionChanged(it)) },
                    inputEffect = state.inputEffect?.takeIf { it == AddVehicleInputEffect.HideKeyboard },
                    onInputEffectHandled = { dispatch(AddVehicleEvent.InputEffectHandled(it)) },
                    onValueChange = { dispatch(AddVehicleEvent.SearchChanged(it)) },
                    enabled = !state.loading,
                    options = items.map { it.id to it.name },
                    expanded = state.modelDropdownExpanded,
                    tag = "add-vehicle-model-dropdown",
                    onExpandedChange = { dispatch(AddVehicleEvent.ModelDropdownChanged(it)) },
                    onSelected = { dispatch(AddVehicleEvent.SelectModel(it)) },
                    onFocusChanged = { dispatch(AddVehicleEvent.ModelFocusChanged(it)) },
                )
                if (state.step == AddVehicleStep.Model && state.error.isNotEmpty()) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { dispatch(AddVehicleEvent.Retry) }) { Text("Повторити") }
                }
                if (items.isEmpty() && !state.loading && state.error.isEmpty()) {
                    Text(
                        if (state.models.isEmpty()) "Для цієї марки моделей не знайдено."
                        else "За цим запитом моделей не знайдено.",
                        Modifier.testTag("add-model-empty"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                state.selectedModel?.let { model ->
                    AddVehicleYearOptions(
                        years = model.years,
                        selectedYear = state.year,
                        loading = state.yearsLoading,
                        error = state.yearsError,
                        onSelected = { dispatch(AddVehicleEvent.SelectYear(it)) },
                        onRetry = { dispatch(AddVehicleEvent.RetryYears) },
                    )
                    if (model.trims.isNotEmpty()) {
                        Text("Комплектація", style = MaterialTheme.typography.titleSmall)
                        model.trims.forEach {
                            Choice(it.name, "add-trim-${it.id}", state.trim?.id == it.id) {
                                dispatch(AddVehicleEvent.SelectTrim(it.id))
                            }
                        }
                        Button(
                            onClick = { dispatch(AddVehicleEvent.Submit) },
                            enabled = state.canSubmit,
                            modifier = Modifier.fillMaxWidth().testTag("add-vehicle-submit"),
                        ) { Text("Далі · перевірити параметри") }
                    }
                }
            }

            AddVehicleStep.Review -> {
                state.added?.let { vehicle ->
                    AddVehicleReviewScreen(vehicle) { dispatch(AddVehicleEvent.Close) }
                }
            }
        }
    }
}
