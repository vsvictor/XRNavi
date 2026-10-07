package com.mobilespace.xrnavi.domain

enum class DrivingContext { Personal, Work }
enum class VehicleId { Mustang, Man }

data class RoutingSettings(
    val vehicleCategory: Int = 0,
    val trailerIndex: Int = 0,
    val selectedRoute: Int = 0,
    val restStopEnabled: Boolean = true,
    val avoidTolls: Boolean = false,
)

data class WorkTask(val id: String, val destination: String)

data class SessionState(
    val context: DrivingContext = DrivingContext.Personal,
    val selectedVehicle: VehicleId = VehicleId.Mustang,
    val organizationMember: Boolean = false,
    val routingSettings: RoutingSettings = RoutingSettings(),
    val destination: String = "",
    val selectedTask: WorkTask? = null,
)

interface SessionRepository {
    val state: kotlinx.coroutines.flow.StateFlow<SessionState>
    fun update(transform: (SessionState) -> SessionState)
}

class SelectDrivingContext(private val session: SessionRepository) {
    operator fun invoke(context: DrivingContext): Boolean {
        if (context == DrivingContext.Work && !session.state.value.organizationMember) return false
        session.update {
            it.copy(context = context, selectedTask = null, selectedVehicle =
                if (context == DrivingContext.Personal) VehicleId.Mustang else VehicleId.Man,
                routingSettings = it.routingSettings.copy(vehicleCategory = if (context == DrivingContext.Personal) 0 else 1))
        }
        return true
    }
}

class SelectWorkTask(private val session: SessionRepository) {
    operator fun invoke(task: WorkTask): Boolean {
        if (session.state.value.context != DrivingContext.Work || !session.state.value.organizationMember) return false
        session.update { it.copy(selectedTask = task, destination = task.destination) }
        return true
    }
}
