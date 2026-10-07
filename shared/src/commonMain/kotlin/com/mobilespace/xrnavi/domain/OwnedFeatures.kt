package com.mobilespace.xrnavi.domain

sealed interface AccountDeletionResult {
    data object NotConfigured : AccountDeletionResult
    data object ConsentRequired : AccountDeletionResult
}

interface AccountDeletionRepository {
    suspend fun requestDeletion(): AccountDeletionResult
    suspend fun exportData(): AccountDeletionResult
}

class RequestAccountDeletion(private val repository: AccountDeletionRepository) {
    suspend operator fun invoke(acknowledged: Boolean): AccountDeletionResult =
        if (acknowledged) repository.requestDeletion() else AccountDeletionResult.ConsentRequired
}

class ExportAccountData(private val repository: AccountDeletionRepository) {
    suspend operator fun invoke(): AccountDeletionResult = repository.exportData()
}

enum class OfflineMapsAction { UpdateRegion, PauseDownload, CancelDownload, OpenCachedRoute, DownloadRegion }
sealed interface OfflineMapsActionResult {
    data object NotConfigured : OfflineMapsActionResult
}
interface OfflineMapsRepository {
    suspend fun perform(action: OfflineMapsAction): OfflineMapsActionResult
}
class PerformOfflineMapsAction(private val repository: OfflineMapsRepository) {
    suspend operator fun invoke(action: OfflineMapsAction): OfflineMapsActionResult = repository.perform(action)
}

sealed interface OrganizationInvitationResult {
    data object NotConfigured : OrganizationInvitationResult
    data object Accepted : OrganizationInvitationResult
    data object ConsentRequired : OrganizationInvitationResult
}
interface OrganizationInvitationRepository {
    suspend fun acceptInvitation(code: String, email: String): OrganizationInvitationResult
}
class AcceptOrganizationInvitation(
    private val repository: OrganizationInvitationRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(code: String, email: String, consent: Boolean): OrganizationInvitationResult {
        if (!consent) return OrganizationInvitationResult.ConsentRequired
        return repository.acceptInvitation(code, email).also {
            if (it == OrganizationInvitationResult.Accepted) {
                session.update { state -> state.copy(organizationMember = true) }
            }
        }
    }
}

enum class TripHistoryAction { OpenPersonalTrip, OpenWorkTrip, OpenClassification }
sealed interface TripHistoryActionResult {
    data object NotConfigured : TripHistoryActionResult
}
interface TripHistoryRepository {
    suspend fun perform(action: TripHistoryAction): TripHistoryActionResult
}
class PerformTripHistoryAction(private val repository: TripHistoryRepository) {
    suspend operator fun invoke(action: TripHistoryAction): TripHistoryActionResult = repository.perform(action)
}

enum class WorkTripAction { ContactReceiver, MessageDispatcher, ReportArrival, ReportDelay, AcceptTask }
sealed interface WorkTripActionResult {
    data object NotConfigured : WorkTripActionResult
    data object InvalidContext : WorkTripActionResult
}
interface WorkTripRepository {
    suspend fun perform(action: WorkTripAction, taskId: String): WorkTripActionResult
}

class PerformWorkTripAction(
    private val repository: WorkTripRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(action: WorkTripAction, task: WorkTask?): WorkTripActionResult {
        val state = session.state.value
        if (task == null || state.context != DrivingContext.Work || !state.organizationMember) {
            return WorkTripActionResult.InvalidContext
        }
        return repository.perform(action, task.id)
    }
}

sealed interface TripPlanActionResult {
    data object NotConfigured : TripPlanActionResult
    data object InvalidContext : TripPlanActionResult
}
interface TripPlanRepository {
    suspend fun addStop(taskId: String): TripPlanActionResult
}
class AddTripPlanStop(
    private val repository: TripPlanRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(task: WorkTask?): TripPlanActionResult {
        val state = session.state.value
        if (task == null || state.context != DrivingContext.Work || !state.organizationMember) {
            return TripPlanActionResult.InvalidContext
        }
        return repository.addStop(task.id)
    }
}

class OpenChosenWorkTask(private val session: SessionRepository) {
    operator fun invoke(task: WorkTask?): Boolean {
        // A null task opens the explicitly demonstrational view, without changing the session.
        if (task == null) return true
        val state = session.state.value
        if (state.context != DrivingContext.Work || !state.organizationMember) return false
        return SelectWorkTask(session)(task)
    }
}
