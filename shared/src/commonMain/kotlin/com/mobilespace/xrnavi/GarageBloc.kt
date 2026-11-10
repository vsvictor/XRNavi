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
    val error: String = "",
    val loadFailed: Boolean = false,
) {
    val active: GarageVehicle? get() = vehicles.firstOrNull { it.id == activeVehicleId }
}

internal sealed interface GarageEvent {
    data object Load : GarageEvent
    data class Select(val vehicleId: String) : GarageEvent
}

internal class GarageBloc(
    private val service: GarageService,
    private val sessionProvider: () -> AuthSession?,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(GarageState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    fun dispatch(event: GarageEvent) {
        when (event) {
            GarageEvent.Load -> load()
            is GarageEvent.Select -> select(event.vehicleId)
        }
    }

    private fun load() {
        val session = sessionProvider() ?: return fail("Сесія завершилась. Увійдіть знову.")
        if (mutableState.value.loading) return
        mutableState.value = mutableState.value.copy(loading = true, error = "", loadFailed = false)
        job = scope.launch {
            try {
                val snapshot = service.vehicles(session)
                mutableState.value = mutableState.value.copy(
                    loading = false, loaded = true, vehicles = snapshot.vehicles,
                    activeVehicleId = snapshot.activeVehicleId, loadFailed = false,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fail("Не вдалося завантажити гараж. Перевірте з'єднання та повторіть.")
                mutableState.value = mutableState.value.copy(loadFailed = true)
            }
        }
    }

    // The previous selection stays until the server confirms the new one.
    private fun select(vehicleId: String) {
        val session = sessionProvider() ?: return fail("Сесія завершилась. Увійдіть знову.")
        if (mutableState.value.loading || mutableState.value.activeVehicleId == vehicleId) return
        mutableState.value = mutableState.value.copy(loading = true, error = "")
        job = scope.launch {
            try {
                val confirmed = service.setActive(session, vehicleId)
                mutableState.value = mutableState.value.copy(loading = false, activeVehicleId = confirmed)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                fail("Не вдалося вибрати авто. Попередній вибір збережено.")
            }
        }
    }

    private fun fail(message: String) {
        mutableState.value = mutableState.value.copy(loading = false, error = message)
    }
}
