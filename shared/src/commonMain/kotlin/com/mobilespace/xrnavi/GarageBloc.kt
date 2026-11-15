package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class GarageState(
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val vehicles: List<GarageVehicle> = emptyList(),
    val activeVehicleId: String? = null,
    val selectedCategory: String? = null,
    val error: String = "",
    val loadFailed: Boolean = false,
    val deleteVehicleId: String? = null,
) {
    val vehicleToDelete: GarageVehicle? get() = vehicles.firstOrNull { it.id == deleteVehicleId }
    val active: GarageVehicle? get() = vehicles.firstOrNull { it.id == activeVehicleId }
    val categories: List<String>
        get() = (
            listOf("passenger", "truck", "special") +
                vehicles.map { it.category.trim() }
                    .filter(String::isNotEmpty)
                    .map { garageCategoryFilterKey(it) ?: it }
            ).distinct()
    val visibleVehicles: List<GarageVehicle>
        get() = vehicles.filter { vehicle ->
            selectedCategory == null ||
                garageCategoryFilterKey(vehicle.category)?.let { it == selectedCategory }
                ?: vehicle.category.trim().equals(selectedCategory, ignoreCase = true)
        }
}

internal sealed interface GarageEvent {
    data object Load : GarageEvent
    data object Clear : GarageEvent
    data class Select(val vehicleId: String) : GarageEvent
    data class FilterChanged(val category: String?) : GarageEvent
    data class RequestDelete(val vehicleId: String) : GarageEvent
    data object CancelDelete : GarageEvent
    data object ConfirmDelete : GarageEvent
}

internal class GarageBloc(
    private val service: GarageService,
    private val sessionProvider: () -> AuthSession?,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(GarageState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null
    private var request: Any? = null

    fun dispatch(event: GarageEvent) {
        when (event) {
            GarageEvent.Load -> load()
            GarageEvent.Clear -> {
                request = null
                job?.cancel()
                job = null
                mutableState.value = GarageState()
            }
            is GarageEvent.Select -> select(event.vehicleId)
            is GarageEvent.RequestDelete -> if (!mutableState.value.loading &&
                mutableState.value.vehicles.any { it.id == event.vehicleId }) {
                mutableState.value = mutableState.value.copy(deleteVehicleId = event.vehicleId, error = "")
            }
            GarageEvent.CancelDelete -> if (!mutableState.value.loading) {
                mutableState.value = mutableState.value.copy(deleteVehicleId = null)
            }
            GarageEvent.ConfirmDelete -> delete()
            is GarageEvent.FilterChanged -> {
                if (
                    event.category == null ||
                    event.category in mutableState.value.categories ||
                    garageCategoryFilterKey(event.category) in mutableState.value.categories
                ) {
                    mutableState.value = mutableState.value.copy(selectedCategory = event.category)
                }
            }
        }
    }

    private fun load() {
        val session = sessionProvider() ?: return fail("Сесія завершилась. Увійдіть знову.")
        if (mutableState.value.loading) return
        val identity = Any()
        request = identity
        mutableState.value = mutableState.value.copy(loading = true, error = "", loadFailed = false)
        job = scope.launch {
            try {
                val snapshot = service.vehicles(session)
                if (request !== identity) return@launch
                mutableState.value = mutableState.value.copy(
                    loading = false, loaded = true, vehicles = snapshot.vehicles,
                    activeVehicleId = snapshot.activeVehicleId, loadFailed = false,
                    selectedCategory = mutableState.value.selectedCategory
                        ?.takeIf { category -> snapshot.vehicles.any { it.category.trim() == category } },
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (request !== identity) return@launch
                fail("Не вдалося завантажити гараж. Перевірте з'єднання та повторіть.")
                mutableState.value = mutableState.value.copy(loadFailed = true)
            }
        }
    }

    // The previous selection stays until the server confirms the new one.
    private fun select(vehicleId: String) {
        val session = sessionProvider() ?: return fail("Сесія завершилась. Увійдіть знову.")
        if (mutableState.value.loading || mutableState.value.activeVehicleId == vehicleId) return
        val identity = Any()
        request = identity
        mutableState.value = mutableState.value.copy(loading = true, error = "")
        job = scope.launch {
            try {
                val confirmed = service.setActive(session, vehicleId)
                if (request !== identity) return@launch
                mutableState.value = mutableState.value.copy(loading = false, activeVehicleId = confirmed)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (request !== identity) return@launch
                fail("Не вдалося вибрати авто. Попередній вибір збережено.")
            }
        }
    }

    private fun fail(message: String) {
        mutableState.value = mutableState.value.copy(loading = false, error = message)
    }

    private fun delete() {
        val current = mutableState.value
        val id = current.deleteVehicleId ?: return
        if (current.loading) return
        val session = sessionProvider() ?: return fail("Сесія завершилась. Увійдіть знову.")
        val identity = Any()
        request = identity
        mutableState.value = current.copy(loading = true, error = "")
        job = scope.launch {
            try {
                val activeId = service.deleteVehicle(session, id)
                if (request !== identity) return@launch
                mutableState.value = mutableState.value.copy(
                    loading = false, vehicles = mutableState.value.vehicles.filterNot { it.id == id },
                    activeVehicleId = activeId, deleteVehicleId = null,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (request === identity) fail("Не вдалося видалити авто. Повторіть.")
            }
        }
    }
}
