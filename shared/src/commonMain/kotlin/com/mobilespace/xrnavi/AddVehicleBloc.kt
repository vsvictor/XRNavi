package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextRange

internal enum class AddVehicleStep { Class, Model, Details, Review }

internal enum class VehicleCatalogGroup(val label: String) {
    Passenger("Легкове"),
    Cargo("Вантажне"),
    Special("Спецтехніка"),
    Other("Інші класи"),
}

internal fun vehicleCatalogGroup(name: String): VehicleCatalogGroup? {
    val normalized = name.trim().lowercase().replace('ё', 'е')
    return when {
        listOf("легков", "passenger", "sedan", "suv").any(normalized::contains) ->
            VehicleCatalogGroup.Passenger

        listOf("вантаж", "грузов", "truck", "cargo", "freight", "тягач", "автовоз")
            .any(normalized::contains) -> VehicleCatalogGroup.Cargo

        listOf("спец", "special", "евакуатор", "эвакуатор", "трактор", "автокран")
            .any(normalized::contains) -> VehicleCatalogGroup.Special

        else -> null
    }
}

internal fun VehicleClassItem.catalogGroup(): VehicleCatalogGroup =
    vehicleCatalogGroup(name) ?: VehicleCatalogGroup.Other

internal fun VehicleClassItem.isGroupLabel(): Boolean {
    val normalized = name.trim().lowercase().replace('ё', 'е')
    return normalized in setOf(
        "легкове", "легкові", "легковые", "легковой", "passenger",
        "вантажне", "вантажні", "грузовые", "грузовой", "truck", "cargo",
        "спецтехніка", "спецтехника", "special",
    )
}

internal data class AddVehicleState(
    val open: Boolean = false,
    val selectedTab: AddVehicleTab = AddVehicleTab.General,
    val equipment: VehicleEquipmentState = VehicleEquipmentState(),
    val step: AddVehicleStep = AddVehicleStep.Class,
    val loading: Boolean = false,
    val error: String = "",
    val vinQuery: String = "",
    val vinDialogOpen: Boolean = false,
    val vinLoading: Boolean = false,
    val vinError: String = "",
    val vinSearched: Boolean = false,
    val decodedVin: DecodedVehicleVin? = null,
    val searchQuery: String = "",
    val makeSelection: TextRange = TextRange.Zero,
    val modelSelection: TextRange = TextRange.Zero,
    val inputEffect: AddVehicleInputEffect? = null,
    val makeLoading: Boolean = false,
    val makeError: String = "",
    val makeSearched: Boolean = false,
    val makeResults: List<VehicleMake> = emptyList(),
    val makeDropdownExpanded: Boolean = false,
    val modelDropdownExpanded: Boolean = false,
    val classes: List<VehicleClassItem> = emptyList(),
    val selectedCatalogGroup: VehicleCatalogGroup = VehicleCatalogGroup.Passenger,
    val makes: List<VehicleMake> = emptyList(),
    val models: List<VehicleModelItem> = emptyList(),
    val selectedClass: VehicleClassItem? = null,
    val selectedMake: VehicleMake? = null,
    val selectedModel: VehicleModelItem? = null,
    val yearsLoading: Boolean = false,
    val yearsError: String = "",
    val year: Int? = null,
    val trim: VehicleTrim? = null,
    val added: GarageVehicle? = null,
    val editingVehicle: GarageVehicle? = null,
    val editCatalogError: String = "",
) {
    val canSubmit: Boolean
        get() = open && step != AddVehicleStep.Review && !loading && !vinLoading &&
                selectedMake != null && selectedModel != null && year != null
    val canSearchVin: Boolean
        get() = open && step != AddVehicleStep.Review && !loading && !vinLoading &&
                vinQuery.length == 17 && vinQuery.all { it in "0123456789ABCDEFGHJKLMNPRSTUVWXYZ" }
    val filteredMakes: List<VehicleMake>
        get() = makeResults.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
}

internal sealed interface AddVehicleEvent {
    data object Open : AddVehicleEvent
    data class Edit(val vehicle: GarageVehicle) : AddVehicleEvent
    data object RetryEditCatalog : AddVehicleEvent
    data object Retry : AddVehicleEvent
    data object RetryYears : AddVehicleEvent
    data object RetryEquipment : AddVehicleEvent
    data object RetryEquipmentBodies : AddVehicleEvent
    data object RetryEquipmentEngines : AddVehicleEvent
    data object RetryEquipmentMileages : AddVehicleEvent
    data class EquipmentTrimQueryChanged(val query: String) : AddVehicleEvent
    data class EquipmentTrimExpanded(val expanded: Boolean) : AddVehicleEvent
    data class EquipmentTrimCursorChanged(val selection: TextRange) : AddVehicleEvent
    data class SelectEquipmentTrim(val id: String) : AddVehicleEvent
    data class ShowEngineDetails(val id: String) : AddVehicleEvent
    data object DismissEngineDetails : AddVehicleEvent
    data class ConfigurationCursorChanged(
        val field: VehicleConfigurationField,
        val selection: TextRange
    ) : AddVehicleEvent

    data class ConfigurationQueryChanged(val field: VehicleConfigurationField, val query: String) :
        AddVehicleEvent

    data class ConfigurationExpanded(val field: VehicleConfigurationField, val expanded: Boolean) :
        AddVehicleEvent

    data class SelectConfigurationOption(val field: VehicleConfigurationField, val id: String) :
        AddVehicleEvent

    data class SelectTab(val tab: AddVehicleTab) : AddVehicleEvent
    data class MakeSelectionChanged(val selection: TextRange) : AddVehicleEvent
    data class ModelSelectionChanged(val selection: TextRange) : AddVehicleEvent
    data class InputEffectHandled(val effect: AddVehicleInputEffect) : AddVehicleEvent
    data class VinChanged(val query: String) : AddVehicleEvent
    data object SearchVin : AddVehicleEvent
    data object OpenVinDialog : AddVehicleEvent
    data object DismissVinDialog : AddVehicleEvent
    data class SearchChanged(val query: String) : AddVehicleEvent
    data class MakeChanged(val query: String) : AddVehicleEvent
    data class MakeFocusChanged(val focused: Boolean) : AddVehicleEvent
    data class ModelFocusChanged(val focused: Boolean) : AddVehicleEvent
    data class MakeDropdownChanged(val expanded: Boolean) : AddVehicleEvent
    data class ModelDropdownChanged(val expanded: Boolean) : AddVehicleEvent
    data class SelectCatalogGroup(val group: VehicleCatalogGroup) : AddVehicleEvent
    data class SelectClass(val id: String) : AddVehicleEvent
    data class SelectMake(val id: String) : AddVehicleEvent
    data class SelectModel(val id: String) : AddVehicleEvent
    data class SelectYear(val year: Int) : AddVehicleEvent
    data class SelectTrim(val id: String) : AddVehicleEvent
    data object Submit : AddVehicleEvent
    data object Back : AddVehicleEvent
    data object Close : AddVehicleEvent
}

internal class AddVehicleBloc(
    private val service: GarageService,
    private val sessionProvider: () -> AuthSession?,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(AddVehicleState())
    val state = mutableState.asStateFlow()
    private var makeRequest: Any? = null
    private var yearsRequest: Any? = null
    private var equipmentRequest: Any? = null
    private var vinRequest: Any? = null
    private var bodiesRequest: Any? = null
    private var enginesRequest: Any? = null
    private var mileagesRequest: Any? = null
    private var submitRequest: Any? = null
    private var editRequest: Any? = null

    /** Returns true when the event was consumed (a wizard step was closed). */
    fun handleBack(): Boolean {
        val current = mutableState.value
        if (!current.open) return false
        if (current.vinDialogOpen) {
            dispatch(AddVehicleEvent.DismissVinDialog)
            return true
        }
        if (!current.loading) dispatch(AddVehicleEvent.Back)
        return true
    }

    fun dispatch(event: AddVehicleEvent) {
        val s = mutableState.value
        when (event) {
            AddVehicleEvent.Open -> {
                editRequest = null
                submitRequest = null
                vinRequest = null
                makeRequest = null
                yearsRequest = null
                equipmentRequest = null
                mutableState.value = AddVehicleState(open = true)
                loadStep()
            }

            is AddVehicleEvent.Edit -> {
                dispatch(AddVehicleEvent.Close)
                try {
                    mutableState.value = event.vehicle.editState()
                    loadEditCatalog()
                } catch (_: GarageException) {
                    mutableState.value = AddVehicleState(
                        open = true, editingVehicle = event.vehicle,
                        editCatalogError = "Збережені дані авто неповні. Неможливо відкрити редагування.",
                    )
                }
            }

            AddVehicleEvent.RetryEditCatalog -> loadEditCatalog()
            AddVehicleEvent.Retry -> loadStep()
            AddVehicleEvent.RetryYears -> loadYears()
            AddVehicleEvent.RetryEquipment -> loadEquipmentTrims(
                s.decodedVin?.takeIf {
                    s.vinSearched && it.make.equals(s.selectedMake?.name, true) &&
                            it.model.equals(s.selectedModel?.name, true) && it.year == s.year
                },
            )

            AddVehicleEvent.RetryEquipmentBodies -> loadEquipmentBodies()
            AddVehicleEvent.RetryEquipmentEngines -> loadEquipmentEngines()
            AddVehicleEvent.RetryEquipmentMileages -> loadEquipmentMileages()
            is AddVehicleEvent.EquipmentTrimQueryChanged ->
                mutableState.value = s.copy(
                    equipment = s.equipment.copy(
                        trimQuery = event.query, selectedTrim = null, trimExpanded = true,
                        bodies = emptyList(), bodiesLoading = false, bodiesError = "",
                        engines = emptyList(), enginesLoading = false, enginesError = "",
                        mileages = emptyList(), mileagesLoading = false, mileagesError = "",
                    )
                )

            is AddVehicleEvent.EquipmentTrimExpanded ->
                mutableState.value = s.copy(
                    equipment = s.equipment.copy(
                        trimExpanded = event.expanded,
                        expanded = if (event.expanded) null else s.equipment.expanded,
                        engineDetails = null,
                    )
                )

            is AddVehicleEvent.EquipmentTrimCursorChanged ->
                mutableState.value =
                    s.copy(equipment = s.equipment.copy(trimCursor = event.selection))

            is AddVehicleEvent.SelectEquipmentTrim ->
                s.equipment.trims.firstOrNull { it.id == event.id }?.let {
                    mutableState.value = s.copy(
                        equipment = s.equipment.copy(
                            selectedTrim = it, trimQuery = it.name,
                            vinNotice = "",
                            trimCursor = TextRange(it.name.length), trimExpanded = false,
                            bodies = emptyList(), bodiesError = "",
                            engines = emptyList(), enginesError = "",
                            mileages = emptyList(), mileagesError = "",
                            selections = if (s.equipment.selectedTrim?.id == it.id) s.equipment.selections else emptyMap(),
                            queries = if (s.equipment.selectedTrim?.id == it.id) s.equipment.queries else emptyMap(),
                            cursors = if (s.equipment.selectedTrim?.id == it.id) s.equipment.cursors else emptyMap(),
                            expanded = null, engineDetails = null,
                        )
                    )
                    loadEquipmentBodies()
                    loadEquipmentEngines()
                    loadEquipmentMileages()
                }

            is AddVehicleEvent.ShowEngineDetails ->
                s.equipment.options(VehicleConfigurationField.Engine)
                    .firstOrNull { it.id == event.id }?.let {
                        mutableState.value =
                            s.copy(equipment = s.equipment.copy(engineDetails = it))
                    }

            AddVehicleEvent.DismissEngineDetails ->
                mutableState.value = s.copy(equipment = s.equipment.copy(engineDetails = null))

            is AddVehicleEvent.ConfigurationCursorChanged ->
                mutableState.value = s.copy(
                    equipment = s.equipment.copy(
                        cursors = s.equipment.cursors + (event.field to event.selection),
                    )
                )

            is AddVehicleEvent.ConfigurationQueryChanged -> {
                mutableState.value = s.copy(
                    equipment = s.equipment.copy(
                        queries = s.equipment.queries + (event.field to event.query),
                        selections = s.equipment.selections - event.field,
                        expanded = event.field,
                        engineDetails = null,
                    )
                )
            }

            is AddVehicleEvent.ConfigurationExpanded ->
                mutableState.value = s.copy(
                    equipment = s.equipment.copy(
                        expanded = if (event.expanded) event.field
                        else s.equipment.expanded?.takeUnless { it == event.field },
                        trimExpanded = if (event.expanded) false else s.equipment.trimExpanded,
                        engineDetails = null,
                    )
                )

            is AddVehicleEvent.SelectConfigurationOption ->
                s.equipment.options(event.field).firstOrNull { it.id == event.id }?.let { option ->
                    mutableState.value = s.copy(
                        equipment = s.equipment.copy(
                            selections = s.equipment.selections + (event.field to option),
                            queries = s.equipment.queries + (event.field to option.name),
                            cursors = s.equipment.cursors + (event.field to TextRange(option.name.length)),
                            expanded = null,
                            engineDetails = null,
                        )
                    )
                }

            is AddVehicleEvent.MakeSelectionChanged ->
                mutableState.value = s.copy(makeSelection = event.selection)

            is AddVehicleEvent.ModelSelectionChanged ->
                mutableState.value = s.copy(modelSelection = event.selection)

            is AddVehicleEvent.InputEffectHandled -> if (s.inputEffect == event.effect) {
                mutableState.value = s.copy(inputEffect = null)
            }

            is AddVehicleEvent.SelectTab -> if (s.open) {
                mutableState.value = s.copy(
                    selectedTab = event.tab,
                    equipment = s.equipment.copy(
                        expanded = null,
                        engineDetails = null,
                        trimExpanded = false
                    ),
                    inputEffect = null,
                    makeDropdownExpanded = false,
                    modelDropdownExpanded = false,
                )
            }

            AddVehicleEvent.OpenVinDialog -> if (s.open && !s.loading && !s.vinLoading && s.step != AddVehicleStep.Review) {
                mutableState.value = s.copy(
                    vinDialogOpen = true, vinError = "",
                    makeDropdownExpanded = false, modelDropdownExpanded = false, inputEffect = null,
                )
            }

            AddVehicleEvent.DismissVinDialog -> {
                vinRequest = null
                mutableState.value =
                    s.copy(vinDialogOpen = false, vinLoading = false, vinError = "")
            }

            is AddVehicleEvent.VinChanged -> if (!s.loading && !s.vinLoading) {
                mutableState.value = s.copy(
                    vinQuery = event.query
                        .filter { it in '0'..'9' || it in 'A'..'Z' || it in 'a'..'z' }
                        .uppercase()
                        .filterNot { it in "IOQ" }
                        .take(17),
                    vinError = "",
                    vinSearched = false,
                )
            }

            AddVehicleEvent.SearchVin -> searchVin()
            is AddVehicleEvent.SearchChanged -> if (!s.loading) {
                equipmentRequest = null
                if (s.selectedModel != null) yearsRequest = null
                mutableState.value = s.copy(
                    equipment = VehicleEquipmentState(),
                    searchQuery = event.query, makeError = "", error = "",
                    step = if (s.step == AddVehicleStep.Details) AddVehicleStep.Model else s.step,
                    selectedModel = null, year = null, trim = null,
                    yearsLoading = false, yearsError = "",
                    inputEffect = null,
                    makeDropdownExpanded = s.step == AddVehicleStep.Class,
                    modelDropdownExpanded = s.step == AddVehicleStep.Model || s.step == AddVehicleStep.Details,
                )
                searchMakes()
            }

            is AddVehicleEvent.MakeChanged -> if (!s.loading &&
                (s.step == AddVehicleStep.Model || s.step == AddVehicleStep.Details)
            ) {
                equipmentRequest = null
                yearsRequest = null
                mutableState.value = s.copy(
                    equipment = VehicleEquipmentState(),
                    step = AddVehicleStep.Class, searchQuery = event.query,
                    selectedMake = null, selectedModel = null, models = emptyList(),
                    year = null, trim = null, error = "", makeError = "",
                    yearsLoading = false, yearsError = "",
                    makeDropdownExpanded = true, modelDropdownExpanded = false,
                    inputEffect = null,
                )
                searchMakes()
            }

            is AddVehicleEvent.MakeFocusChanged -> if (s.open) {
                val expand = event.focused && s.inputEffect != AddVehicleInputEffect.FocusMake
                mutableState.value = s.copy(makeDropdownExpanded = expand)
                if (expand) searchMakes()
            }

            is AddVehicleEvent.ModelFocusChanged -> if (s.open &&
                (s.step == AddVehicleStep.Model || s.step == AddVehicleStep.Details)
            ) {
                mutableState.value = s.copy(modelDropdownExpanded = event.focused)
            }

            is AddVehicleEvent.MakeDropdownChanged -> {
                mutableState.value = s.copy(makeDropdownExpanded = event.expanded)
                if (event.expanded) searchMakes()
            }

            is AddVehicleEvent.ModelDropdownChanged ->
                mutableState.value = s.copy(modelDropdownExpanded = event.expanded)

            is AddVehicleEvent.SelectCatalogGroup -> selectCatalogGroup(event.group)
            is AddVehicleEvent.SelectClass -> selectClass(event.id)
            is AddVehicleEvent.SelectMake -> if (!s.makeLoading) (
                    if (s.step == AddVehicleStep.Model || s.step == AddVehicleStep.Details) s.makeResults
                    else if (s.makeSearched) s.filteredMakes else s.makes
                    ).firstOrNull { it.id == event.id }?.let {
                    equipmentRequest = null
                    yearsRequest = null
                    mutableState.value = s.copy(
                        equipment = VehicleEquipmentState(),
                        selectedMake = it,
                        step = AddVehicleStep.Model,
                        models = emptyList(),
                        selectedModel = null,
                        year = null,
                        trim = null,
                        searchQuery = "",
                        error = "",
                        yearsLoading = false,
                        yearsError = "",
                        makeDropdownExpanded = false,
                        modelDropdownExpanded = false,
                        makeSelection = TextRange(it.name.length),
                        modelSelection = TextRange.Zero,
                        inputEffect = AddVehicleInputEffect.FocusMake,
                    )
                    loadStep()
                }

            is AddVehicleEvent.SelectModel -> s.models.firstOrNull { it.id == event.id }?.let {
                equipmentRequest = null
                yearsRequest = null
                mutableState.value = s.copy(
                    equipment = VehicleEquipmentState(),
                    selectedModel = it.copy(years = emptyList()), step = AddVehicleStep.Details,
                    year = null, trim = null, searchQuery = it.name, error = "",
                    yearsLoading = false, yearsError = "",
                    modelDropdownExpanded = false,
                    modelSelection = TextRange(it.name.length),
                    inputEffect = AddVehicleInputEffect.HideKeyboard,
                )
                loadYears()
            }

            is AddVehicleEvent.SelectYear ->
                if (s.open && s.selectedMake != null && !s.yearsLoading && s.yearsError.isEmpty() &&
                    s.selectedModel?.years?.contains(event.year) == true
                ) {
                    mutableState.value = s.copy(
                        year = event.year,
                        selectedTab = AddVehicleTab.Equipment,
                        equipment = VehicleEquipmentState(),
                        makeDropdownExpanded = false,
                        modelDropdownExpanded = false,
                        inputEffect = null,
                    )
                    loadEquipmentTrims()
                }

            is AddVehicleEvent.SelectTrim ->
                s.selectedModel?.trims?.firstOrNull { it.id == event.id }?.let {
                    mutableState.value = s.copy(trim = it)
                }

            AddVehicleEvent.Submit -> submit()
            AddVehicleEvent.Back -> if (!s.loading) {
                if (s.editingVehicle != null) {
                    dispatch(AddVehicleEvent.Close)
                    return
                }
                vinRequest = null
                equipmentRequest = null
                yearsRequest = null
                mutableState.value = when (s.step) {
                    AddVehicleStep.Class -> AddVehicleState()
                    AddVehicleStep.Model -> s.copy(
                        equipment = VehicleEquipmentState(),
                        inputEffect = null,
                        step = AddVehicleStep.Class,
                        selectedMake = null,
                        selectedModel = null, year = null, trim = null,
                        yearsLoading = false, yearsError = "",
                        searchQuery = "",
                        error = "",
                        loading = false,
                    )

                    AddVehicleStep.Details -> s.copy(
                        equipment = VehicleEquipmentState(),
                        inputEffect = null,
                        step = AddVehicleStep.Model, searchQuery = "", error = "", loading = false,
                        selectedModel = null, year = null, trim = null,
                        yearsLoading = false, yearsError = "",
                    )

                    AddVehicleStep.Review -> AddVehicleState()
                }
            }

            AddVehicleEvent.Close -> {
                editRequest = null
                submitRequest = null
                vinRequest = null
                equipmentRequest = null
                makeRequest = null
                yearsRequest = null
                mutableState.value = AddVehicleState()
            }
        }
    }

    private fun selectClass(classId: String) {
        val s = mutableState.value
        val selected = s.classes.firstOrNull { it.id == classId } ?: return
        val session = sessionProvider() ?: return fail("Сесія завершилась. Увійдіть знову.")
        if (s.loading || s.makeLoading) return
        if (s.selectedClass?.id == selected.id) {
            if (s.searchQuery.isNotEmpty()) mutableState.value = s.copy(searchQuery = "")
            return
        }
        yearsRequest = null
        equipmentRequest = null
        mutableState.value = s.copy(
            equipment = VehicleEquipmentState(),
            selectedCatalogGroup = selected.catalogGroup(),
            selectedClass = selected,
            makes = emptyList(),
            selectedMake = null,
            selectedModel = null,
            yearsLoading = false,
            yearsError = "",
            year = null,
            trim = null,
            searchQuery = "",
            loading = true,
            error = "",
        )
        scope.launch {
            try {
                val items = service.makes(session, selected.id)
                if (mutableState.value.open && mutableState.value.selectedClass?.id == selected.id) {
                    mutableState.value = mutableState.value.copy(makes = items, loading = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fail("Не вдалося завантажити марки. Повторіть.")
            }
        }
    }

    private fun selectCatalogGroup(group: VehicleCatalogGroup) {
        val s = mutableState.value
        if (s.loading || s.makeLoading || s.selectedCatalogGroup == group) return
        val groupClasses = s.classes.filter { it.catalogGroup() == group }
        val selected = s.selectedClass?.takeIf { it.catalogGroup() == group }
            ?: groupClasses.firstOrNull()
        mutableState.value = s.copy(selectedCatalogGroup = group)
        if (selected == null) {
            yearsRequest = null
            equipmentRequest = null
            mutableState.value = mutableState.value.copy(
                equipment = VehicleEquipmentState(),
                selectedClass = null,
                makes = emptyList(),
                selectedMake = null,
                selectedModel = null,
                yearsLoading = false,
                yearsError = "",
                year = null,
                trim = null,
                searchQuery = "",
                error = "",
            )
        } else {
            selectClass(selected.id)
        }
    }

    private fun loadStep() {
        val session = sessionProvider() ?: return fail("Сесія завершилась. Увійдіть знову.")
        val s = mutableState.value
        mutableState.value = s.copy(loading = true, error = "")
        scope.launch {
            try {
                when (s.step) {
                    AddVehicleStep.Class -> {
                        val classes = service.classes(session)
                        val groupClasses =
                            classes.filter { it.catalogGroup() == s.selectedCatalogGroup }
                        val selected = s.selectedClass
                            ?.takeIf { item -> groupClasses.any { it.id == item.id } }
                            ?: groupClasses.firstOrNull()
                        val makes = selected?.let { service.makes(session, it.id) }.orEmpty()
                        update(AddVehicleStep.Class) {
                            it.copy(
                                classes = classes,
                                selectedClass = selected,
                                makes = makes,
                                loading = false,
                            )
                        }
                    }

                    AddVehicleStep.Model -> {
                        val items =
                            service.searchModels(session, s.selectedMake?.name ?: return@launch)
                        if (mutableState.value.selectedMake?.id == s.selectedMake.id) {
                            update(AddVehicleStep.Model) {
                                it.copy(
                                    models = items,
                                    loading = false
                                )
                            }
                        }
                    }

                    AddVehicleStep.Details -> update(s.step) { it.copy(loading = false) }
                    AddVehicleStep.Review -> update(s.step) { it.copy(loading = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fail(
                    if (s.step == AddVehicleStep.Class && s.classes.isNotEmpty())
                        "Не вдалося завантажити марки. Повторіть."
                    else if (s.step == AddVehicleStep.Model) "Не вдалося завантажити моделі. Повторіть."
                    else "Не вдалося завантажити каталог. Повторіть."
                )
            }
        }
    }

    private fun update(step: AddVehicleStep, change: (AddVehicleState) -> AddVehicleState) {
        if (mutableState.value.step == step && mutableState.value.open) {
            mutableState.value = change(mutableState.value)
        }
    }

    private fun submit() {
        val s = mutableState.value
        val session = sessionProvider() ?: return fail("Сесія завершилась. Увійдіть знову.")
        if (!s.canSubmit) return
        val request = NewVehicle(
            s.selectedClass?.id ?: "passenger", s.selectedMake?.id ?: return,
            s.selectedModel?.id ?: return, s.year ?: return,
            s.equipment.selectedTrim?.id ?: s.trim?.id,
            s.vehicleSnapshot(),
        )
        val identity = Any()
        submitRequest = identity
        mutableState.value = s.copy(loading = true, error = "")
        scope.launch {
            fun isCurrent(): Boolean {
                return submitRequest === identity && mutableState.value.open
            }
            try {
                val vehicle =
                    s.editingVehicle?.let { service.updateVehicle(session, it.id, request) }
                        ?: service.addVehicle(session, request)
                if (!isCurrent()) return@launch
                mutableState.value = mutableState.value.copy(
                    step = AddVehicleStep.Review,
                    loading = false,
                    error = "",
                    added = vehicle,
                    selectedTab = AddVehicleTab.General,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (isCurrent()) fail(if (s.editingVehicle != null) "Не вдалося зберегти зміни. Повторіть." else "Не вдалося додати авто. Повторіть.")
            }

        }
    }

    private fun loadEditCatalog() {
        val s = mutableState.value
        if (!s.open || s.editingVehicle == null || s.selectedMake == null ||
            s.selectedModel == null || s.year == null
        ) return
        val session = sessionProvider()
        if (session == null) {
            mutableState.value = s.copy(editCatalogError = "Сесія завершилась. Увійдіть знову.")
            return
        }
        val identity = Any()
        editRequest = identity
        mutableState.value = s.copy(loading = true, editCatalogError = "")
        scope.launch {
            try {
                val models = service.searchModels(session, s.selectedMake.name)
                val years = service.years(session, s.selectedMake.name, s.selectedModel.name)
                val trims = service.equipmentTrims(
                    session,
                    s.selectedMake.name,
                    s.selectedModel.name,
                    s.year
                )
                val savedTrim = s.equipment.selectedTrim
                val available = listOfNotNull(savedTrim) + trims.filterNot { it.id == savedTrim?.id }
                val configurations = mutableListOf<VehicleEquipmentDetails>()
                for (trim in available) {
                    if (editRequest !== identity) return@launch
                    configurations += if (trim.id == savedTrim?.id)
                        s.equipment.configurations.single() else service.equipmentDetails(session, trim.id)
                }
                if (editRequest !== identity || !mutableState.value.open) return@launch
                val model = s.selectedModel.copy(years = (years + s.year).distinct().sorted())
                mutableState.value = mutableState.value.copy(
                    loading = false,
                    models = listOf(model) + models.filterNot { it.id == model.id },
                    selectedModel = model,
                    equipment = s.equipment.copy(
                        trims = available,
                        configurations = configurations
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (editRequest === identity && mutableState.value.open) {
                    mutableState.value = mutableState.value.copy(
                        loading = false,
                        editCatalogError = "Не вдалося завантажити каталог для редагування. Повторіть.",
                    )
                }
            }
        }
    }

    private fun searchVin() {
        val current = mutableState.value
        if (!current.canSearchVin) return
        val vin = current.vinQuery
        val session = sessionProvider()
        if (session == null) {
            mutableState.value = current.copy(vinError = "Сесія завершилась. Увійдіть знову.")
            return
        }
        val request = Any()
        vinRequest = request
        equipmentRequest = null
        bodiesRequest = null
        enginesRequest = null
        mileagesRequest = null
        mutableState.value = current.copy(
            vinLoading = true, vinDialogOpen = false, vinError = "", vinSearched = false,
            equipment = VehicleEquipmentState(),
            makeDropdownExpanded = false, modelDropdownExpanded = false, inputEffect = null,
        )
        scope.launch {
            try {
                val decoded = service.decodeVin(session, vin)
                if (vinRequest !== request) return@launch
                val makes = service.searchMakes(session)
                if (vinRequest !== request) return@launch
                val make = makes.firstOrNull { it.name.equals(decoded.make, ignoreCase = true) }
                    ?: throw GarageException(GarageException.Kind.InvalidResponse)
                val models = service.searchModels(session, make.name)
                if (vinRequest !== request) return@launch
                val model = models.firstOrNull { it.name.equals(decoded.model, ignoreCase = true) }
                    ?: throw GarageException(GarageException.Kind.InvalidResponse)
                val years = service.years(session, make.name, model.name)
                if (decoded.year !in years) throw GarageException(GarageException.Kind.InvalidResponse)
                val latest = mutableState.value
                if (vinRequest === request && latest.open) {
                    makeRequest = null
                    yearsRequest = null
                    equipmentRequest = null
                    mutableState.value = latest.copy(
                        vinLoading = false,
                        vinSearched = true,
                        vinDialogOpen = false,
                        decodedVin = decoded,
                        selectedTab = AddVehicleTab.General,
                        step = AddVehicleStep.Details,
                        selectedMake = make,
                        selectedModel = model.copy(years = years),
                        year = decoded.year,
                        trim = null,
                        searchQuery = model.name,
                        makeResults = makes,
                        makeSearched = true,
                        makeLoading = false,
                        makeError = "",
                        models = models,
                        yearsLoading = false,
                        yearsError = "",
                        error = "",
                        makeSelection = TextRange(make.name.length),
                        modelSelection = TextRange(model.name.length),
                        makeDropdownExpanded = false,
                        modelDropdownExpanded = false,
                        inputEffect = AddVehicleInputEffect.HideKeyboard,
                        equipment = VehicleEquipmentState(),
                    )
                    loadEquipmentTrims(decoded)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                val latest = mutableState.value
                if (vinRequest === request && latest.open) {
                    mutableState.value = latest.copy(
                        vinLoading = false,
                        vinDialogOpen = true,
                        vinError = "Не вдалося визначити авто за VIN або знайти його в каталозі. Перевірте VIN та повторіть.",
                    )
                }
            }
        }
    }

    private fun searchMakes() {
        val current = mutableState.value
        if (!current.open || current.step != AddVehicleStep.Class ||
            current.makeLoading || current.makeSearched
        ) return
        val session = sessionProvider()
        if (session == null) {
            mutableState.value = current.copy(makeError = "Сесія завершилась. Увійдіть знову.")
            return
        }

        val request = Any()
        makeRequest = request
        mutableState.value = current.copy(makeLoading = true, makeError = "", makeSearched = false)
        scope.launch {
            try {
                val makes = service.searchMakes(session)
                if (makeRequest === request && mutableState.value.open) {
                    mutableState.value = mutableState.value.copy(
                        makeLoading = false, makeSearched = true, makeResults = makes,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (makeRequest === request && mutableState.value.open) {
                    mutableState.value = mutableState.value.copy(
                        makeLoading = false,
                        makeError = "Не вдалося виконати пошук марки. Повторіть.",
                    )
                }
            }
        }
    }

    private fun loadYears() {
        val s = mutableState.value
        val make = s.selectedMake ?: return
        val model = s.selectedModel ?: return
        if (!s.open || s.yearsLoading) return
        val session = sessionProvider()
        if (session == null) {
            mutableState.value = s.copy(yearsError = "Сесія завершилась. Увійдіть знову.")
            return
        }
        val request = Any()
        yearsRequest = request
        equipmentRequest = null
        mutableState.value = s.copy(
            yearsLoading = true, yearsError = "", year = null, trim = null,
            equipment = VehicleEquipmentState(),
        )
        scope.launch {
            try {
                val years = service.years(session, make.name, model.name)
                val latest = mutableState.value
                if (yearsRequest === request && latest.open) {
                    mutableState.value = latest.copy(
                        selectedModel = model.copy(years = years), yearsLoading = false,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (yearsRequest === request && mutableState.value.open) {
                    mutableState.value = mutableState.value.copy(
                        yearsLoading = false,
                        yearsError = "Не вдалося завантажити роки випуску. Повторіть.",
                    )
                }
            }
        }
    }

    private fun fail(message: String) {
        mutableState.value = mutableState.value.copy(loading = false, error = message)
    }

    private fun currentVinEquipment(): VehicleVinEquipment? {
        val s = mutableState.value
        return s.decodedVin?.takeIf {
            s.vinSearched && it.make.equals(s.selectedMake?.name, true) &&
                    it.model.equals(s.selectedModel?.name, true) && it.year == s.year
        }?.equipment(s.equipment.trims)?.takeIf { it.trim.id == s.equipment.selectedTrim?.id }
    }

    private fun loadEquipmentBodies(seed: kotlinx.serialization.json.JsonObject? = currentVinEquipment()?.body) {
        val s = mutableState.value
        val trim = s.equipment.selectedTrim ?: return
        if (!s.open) return
        val session = sessionProvider()
        if (session == null) {
            mutableState.value = s.copy(
                equipment = s.equipment.copy(
                    bodiesLoading = false, bodiesError = "Сесія завершилась. Увійдіть знову.",
                )
            )
            return
        }
        val request = Any()
        val vehicleRequest = equipmentRequest
        bodiesRequest = request
        mutableState.value = s.copy(
            equipment = s.equipment.copy(
                bodies = emptyList(), bodiesLoading = true, bodiesError = "",
            )
        )
        scope.launch {
            fun isCurrent(): Boolean =
                bodiesRequest === request && equipmentRequest === vehicleRequest &&
                        mutableState.value.open && mutableState.value.equipment.selectedTrim === trim
            try {
                val bodies = mergeVinRecord(service.equipmentBodies(session, trim.id), seed)
                if (isCurrent()) {
                    val latest = mutableState.value
                    mutableState.value = latest.copy(
                        equipment = latest.equipment.copy(
                            bodies = bodies, bodiesLoading = false,
                        )
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (isCurrent()) {
                    val latest = mutableState.value
                    mutableState.value = latest.copy(
                        equipment = latest.equipment.copy(
                            bodiesLoading = false,
                            bodiesError = "Не вдалося завантажити дані кузова. Повторіть.",
                        )
                    )
                }
            }
        }
    }

    private fun loadEquipmentEngines(seed: kotlinx.serialization.json.JsonObject? = currentVinEquipment()?.engine) {
        val s = mutableState.value
        val trim = s.equipment.selectedTrim ?: return
        if (!s.open) return
        val session = sessionProvider()
        if (session == null) {
            mutableState.value = s.copy(
                equipment = s.equipment.copy(
                    enginesLoading = false, enginesError = "Сесія завершилась. Увійдіть знову.",
                )
            )
            return
        }
        val request = Any()
        val vehicleRequest = equipmentRequest
        enginesRequest = request
        mutableState.value = s.copy(
            equipment = s.equipment.copy(
                engines = emptyList(), enginesLoading = true, enginesError = "",
            )
        )
        scope.launch {
            fun isCurrent(): Boolean =
                enginesRequest === request && equipmentRequest === vehicleRequest &&
                        mutableState.value.open && mutableState.value.equipment.selectedTrim === trim
            try {
                val engines = mergeVinRecord(service.equipmentEngines(session, trim.id), seed)
                if (isCurrent()) {
                    val latest = mutableState.value
                    mutableState.value = latest.copy(
                        equipment = latest.equipment.copy(
                            engines = engines, enginesLoading = false,
                        )
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (isCurrent()) {
                    val latest = mutableState.value
                    mutableState.value = latest.copy(
                        equipment = latest.equipment.copy(
                            enginesLoading = false,
                            enginesError = "Не вдалося завантажити дані двигуна. Повторіть.",
                        )
                    )
                }
            }
        }
    }

    private fun loadEquipmentMileages(seed: kotlinx.serialization.json.JsonObject? = currentVinEquipment()?.mileage) {
        val s = mutableState.value
        val trim = s.equipment.selectedTrim ?: return
        if (!s.open) return
        val session = sessionProvider()
        if (session == null) {
            mutableState.value = s.copy(
                equipment = s.equipment.copy(
                    mileagesLoading = false, mileagesError = "Сесія завершилась. Увійдіть знову.",
                )
            )
            return
        }
        val request = Any()
        val vehicleRequest = equipmentRequest
        mileagesRequest = request
        mutableState.value = s.copy(
            equipment = s.equipment.copy(
                mileages = emptyList(), mileagesLoading = true, mileagesError = "",
            )
        )
        scope.launch {
            fun isCurrent(): Boolean =
                mileagesRequest === request && equipmentRequest === vehicleRequest &&
                        mutableState.value.open && mutableState.value.equipment.selectedTrim === trim
            try {
                val mileages = mergeVinRecord(service.equipmentMileages(session, trim.id), seed)
                if (isCurrent()) {
                    val latest = mutableState.value
                    mutableState.value = latest.copy(
                        equipment = latest.equipment.copy(
                            mileages = mileages, mileagesLoading = false,
                        )
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (isCurrent()) {
                    val latest = mutableState.value
                    mutableState.value = latest.copy(
                        equipment = latest.equipment.copy(
                            mileagesLoading = false,
                            mileagesError = "Не вдалося завантажити витрату палива. Повторіть.",
                        )
                    )
                }
            }
        }
    }

    private fun loadEquipmentTrims(decoded: DecodedVehicleVin? = null) {
        val s = mutableState.value
        val make = s.selectedMake ?: return
        val model = s.selectedModel ?: return
        val year = s.year ?: return
        if (!s.open) return
        equipmentRequest = null
        val session = sessionProvider()
        if (session == null) {
            mutableState.value = s.copy(
                equipment = s.equipment.copy(
                    loading = false, error = "Сесія завершилась. Увійдіть знову.",
                )
            )
            return
        }
        val request = Any()
        equipmentRequest = request
        mutableState.value = s.copy(equipment = VehicleEquipmentState(loading = true))
        scope.launch {
            try {
                val trims = service.equipmentTrims(session, make.name, model.name, year)
                val configurations = mutableListOf<VehicleEquipmentDetails>()
                for (trim in trims) {
                    if (equipmentRequest !== request || !mutableState.value.open) return@launch
                    configurations += service.equipmentDetails(session, trim.id)
                }
                if (equipmentRequest === request && mutableState.value.open) {
                    mutableState.value = mutableState.value.copy(
                        equipment = VehicleEquipmentState(
                            trims = trims,
                            configurations = configurations
                        ),
                    )
                    if (decoded != null) {
                        val matched = decoded.equipment(trims)
                        if (matched == null) {
                            mutableState.value = mutableState.value.copy(
                                equipment = mutableState.value.equipment.copy(
                                    vinNotice = "VIN не визначає одну комплектацію. Оберіть її зі списку.",
                                ),
                            )
                        } else {
                            val equipment = mutableState.value.equipment
                            val details =
                                configurations[trims.indexOfFirst { it.id == matched.trim.id }]
                            val bodies = mergeVinRecord(details.bodies, matched.body)
                            val engines = mergeVinRecord(details.engines, matched.engine)
                            val mileages = mergeVinRecord(details.mileages, matched.mileage)
                            mutableState.value = mutableState.value.copy(
                                equipment = equipment.copy(
                                    selectedTrim = matched.trim, trimQuery = matched.trim.name,
                                    trimCursor = TextRange(matched.trim.name.length),
                                    bodies = bodies, engines = engines, mileages = mileages,
                                )
                            )
                            if (bodies.isEmpty() || bodies.any {
                                    !it.hasVinFields(
                                        "type",
                                        "height",
                                        "width",
                                        "length",
                                        "curb_weight"
                                    )
                                }) {
                                loadEquipmentBodies(bodies.singleOrNull())
                            }
                            if (engines.isEmpty() || engines.any {
                                    !it.hasVinFields(
                                        "engine_type",
                                        "fuel_type",
                                        "size",
                                        "horsepower_hp",
                                        "transmission",
                                        "drive_type"
                                    )
                                }) {
                                loadEquipmentEngines(engines.singleOrNull())
                            }
                            if (mileages.isEmpty() || mileages.any {
                                    !it.hasVinFields(
                                        "combined_mpg",
                                        "epa_city_mpg",
                                        "epa_highway_mpg",
                                        "fuel_tank_capacity"
                                    )
                                }) {
                                loadEquipmentMileages(mileages.singleOrNull())
                            }
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (equipmentRequest === request && mutableState.value.open) {
                    mutableState.value = mutableState.value.copy(
                        equipment = VehicleEquipmentState(
                            error = "Не вдалося завантажити комплектації. Повторіть.",
                        )
                    )
                }
            }
        }
    }

}
