package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class AddVehicleStep { Class, Make, Model, Details }

internal data class AddVehicleState(
    val open: Boolean = false,
    val step: AddVehicleStep = AddVehicleStep.Class,
    val loading: Boolean = false,
    val error: String = "",
    val classes: List<VehicleClassItem> = emptyList(),
    val makes: List<VehicleMake> = emptyList(),
    val models: List<VehicleModelItem> = emptyList(),
    val selectedClass: VehicleClassItem? = null,
    val selectedMake: VehicleMake? = null,
    val selectedModel: VehicleModelItem? = null,
    val year: Int? = null,
    val trim: VehicleTrim? = null,
    val added: GarageVehicle? = null,
) {
    val canSubmit: Boolean get() = !loading && year != null && trim != null
}

internal sealed interface AddVehicleEvent {
    data object Open : AddVehicleEvent
    data object Retry : AddVehicleEvent
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

    /** Returns true when the event was consumed (a wizard step was closed). */
    fun handleBack(): Boolean {
        if (!mutableState.value.open) return false
        dispatch(AddVehicleEvent.Back)
        return true
    }

    fun dispatch(event: AddVehicleEvent) {
        val s = mutableState.value
        when (event) {
            AddVehicleEvent.Open -> {
                mutableState.value = AddVehicleState(open = true)
                loadStep()
            }
            AddVehicleEvent.Retry -> loadStep()
            is AddVehicleEvent.SelectClass -> s.classes.firstOrNull { it.id == event.id }?.let {
                mutableState.value = s.copy(
                    selectedClass = it, step = AddVehicleStep.Make, makes = emptyList(),
                    selectedMake = null, selectedModel = null, year = null, trim = null, error = "",
                )
                loadStep()
            }
            is AddVehicleEvent.SelectMake -> s.makes.firstOrNull { it.id == event.id }?.let {
                mutableState.value = s.copy(
                    selectedMake = it, step = AddVehicleStep.Model, models = emptyList(),
                    selectedModel = null, year = null, trim = null, error = "",
                )
                loadStep()
            }
            is AddVehicleEvent.SelectModel -> s.models.firstOrNull { it.id == event.id }?.let {
                mutableState.value = s.copy(
                    selectedModel = it, step = AddVehicleStep.Details, year = null, trim = null, error = "",
                )
            }
            is AddVehicleEvent.SelectYear ->
                if (s.selectedModel?.years?.contains(event.year) == true) {
                    mutableState.value = s.copy(year = event.year)
                }
            is AddVehicleEvent.SelectTrim ->
                s.selectedModel?.trims?.firstOrNull { it.id == event.id }?.let {
                    mutableState.value = s.copy(trim = it)
                }
            AddVehicleEvent.Submit -> submit()
            AddVehicleEvent.Back -> mutableState.value = when (s.step) {
                AddVehicleStep.Class -> AddVehicleState()
                AddVehicleStep.Make -> s.copy(step = AddVehicleStep.Class, error = "", loading = false)
                AddVehicleStep.Model -> s.copy(step = AddVehicleStep.Make, error = "", loading = false)
                AddVehicleStep.Details -> s.copy(step = AddVehicleStep.Model, error = "", loading = false)
            }
            AddVehicleEvent.Close -> mutableState.value = AddVehicleState()
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
                        val items = service.classes(session)
                        update(AddVehicleStep.Class) { it.copy(classes = items, loading = false) }
                    }
                    AddVehicleStep.Make -> {
                        val items = service.makes(session, s.selectedClass?.id ?: return@launch)
                        update(AddVehicleStep.Make) { it.copy(makes = items, loading = false) }
                    }
                    AddVehicleStep.Model -> {
                        val items = service.models(session, s.selectedMake?.id ?: return@launch)
                        update(AddVehicleStep.Model) { it.copy(models = items, loading = false) }
                    }
                    AddVehicleStep.Details -> update(s.step) { it.copy(loading = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fail("Не вдалося завантажити каталог. Повторіть.")
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
            s.selectedClass?.id ?: return, s.selectedMake?.id ?: return,
            s.selectedModel?.id ?: return, s.year ?: return, s.trim?.id ?: return,
        )
        mutableState.value = s.copy(loading = true, error = "")
        scope.launch {
            try {
                val vehicle = service.addVehicle(session, request)
                mutableState.value = AddVehicleState(added = vehicle)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fail("Не вдалося додати авто. Повторіть.")
            }
        }
    }

    private fun fail(message: String) {
        mutableState.value = mutableState.value.copy(loading = false, error = message)
    }
}
