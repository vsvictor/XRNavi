package com.mobilespace.xrnavi

import com.mobilespace.xrnavi.data.*
import com.mobilespace.xrnavi.domain.*
import com.mobilespace.xrnavi.presentation.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OwnedFeaturesTest {
    private class Session(initial: SessionState) : SessionRepository {
        override val state = MutableStateFlow(initial)
        override fun update(transform: (SessionState) -> SessionState) {
            state.value = transform(state.value)
        }
    }

    private fun workSession() = Session(
        SessionState(context = DrivingContext.Work, organizationMember = true),
    )

    @Test
    fun deletionConsentStartsFalseAndBlocksRepository() = runBlocking<Unit> {
        var calls = 0
        val repository = object : AccountDeletionRepository {
            override suspend fun requestDeletion(): AccountDeletionResult {
                calls++
                return AccountDeletionResult.NotConfigured
            }
            override suspend fun exportData(): AccountDeletionResult = AccountDeletionResult.NotConfigured
        }
        val vm = AccountDeletionViewModel(
            RequestAccountDeletion(repository), ExportAccountData(repository), Dispatchers.Unconfined,
        )
        assertFalse(vm.uiState.value.acknowledged)
        vm.deleteAccount()
        assertEquals(0, calls)
        assertNotNull(vm.uiState.value.statusMessage)
        vm.toggleAcknowledgement()
        assertTrue(vm.uiState.value.acknowledged)
        assertNull(vm.uiState.value.statusMessage)
        vm.deleteAccount()
        assertEquals(1, calls)
        assertFalse(vm.uiState.value.isSubmitting)
        assertNotNull(vm.uiState.value.statusMessage)
    }

    @Test
    fun duplicateDeletionSubmissionIsIgnoredAndConsentCannotChangeInFlight() = runBlocking {
        val response = CompletableDeferred<AccountDeletionResult>()
        var calls = 0
        val repository = object : AccountDeletionRepository {
            override suspend fun requestDeletion(): AccountDeletionResult {
                calls++
                return response.await()
            }
            override suspend fun exportData(): AccountDeletionResult = AccountDeletionResult.NotConfigured
        }
        val vm = AccountDeletionViewModel(
            RequestAccountDeletion(repository), ExportAccountData(repository), Dispatchers.Unconfined,
        )
        vm.toggleAcknowledgement()
        vm.deleteAccount()
        vm.deleteAccount()
        vm.toggleAcknowledgement()
        assertTrue(vm.uiState.value.isSubmitting)
        assertTrue(vm.uiState.value.acknowledged)
        assertEquals(1, calls)
        response.complete(AccountDeletionResult.NotConfigured)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    @Test
    fun exportIsHonestlyUnavailable() {
        val repository = UnconfiguredAccountDeletionService()
        val vm = AccountDeletionViewModel(
            RequestAccountDeletion(repository), ExportAccountData(repository), Dispatchers.Unconfined,
        )
        vm.exportAccountData()
        assertNotNull(vm.uiState.value.statusMessage)
        assertFalse(vm.uiState.value.isSubmitting)
        assertFalse(vm.uiState.value.acknowledged)
    }

    @Test
    fun allUnavailableAdaptersReturnNotConfigured() = runBlocking {
        val deletion = UnconfiguredAccountDeletionService()
        assertEquals(AccountDeletionResult.NotConfigured, deletion.requestDeletion())
        assertEquals(AccountDeletionResult.NotConfigured, deletion.exportData())
        for (action in OfflineMapsAction.entries) {
            assertEquals(OfflineMapsActionResult.NotConfigured, UnconfiguredOfflineMapsService().perform(action))
        }
        assertEquals(
            OrganizationInvitationResult.NotConfigured,
            UnconfiguredOrganizationInvitationService().acceptInvitation("code", "email"),
        )
        for (action in TripHistoryAction.entries) {
            assertEquals(TripHistoryActionResult.NotConfigured, UnconfiguredTripHistoryService().perform(action))
        }
        assertEquals(TripPlanActionResult.NotConfigured, UnconfiguredTripPlanService().addStop("DL-204"))
        for (action in WorkTripAction.entries) {
            assertEquals(WorkTripActionResult.NotConfigured, UnconfiguredWorkTripService().perform(action, "DL-204"))
        }
    }

    @Test
    fun invitationUnavailableDoesNotJoinOrganizationOrSwitchContext() = runBlocking {
        val session = Session(SessionState())
        val before = session.state.value
        val accept = AcceptOrganizationInvitation(UnconfiguredOrganizationInvitationService(), session)
        assertEquals(OrganizationInvitationResult.ConsentRequired, accept("code", "email", false))
        assertEquals(OrganizationInvitationResult.NotConfigured, accept("code", "email", true))
        assertEquals(before, session.state.value)
        val vm = OrganizationInvitationViewModel(accept, "code", "email", Dispatchers.Unconfined)
        vm.accept()
        assertNotNull(vm.uiState.value.statusMessage)
        assertFalse(vm.uiState.value.isSubmitting)
        assertEquals(before, session.state.value)
    }

    @Test
    fun invitationAcceptanceSetsMembershipWithoutSwitchingPersonalContext() = runBlocking {
        val session = Session(SessionState())
        var receivedCode: String? = null
        var receivedEmail: String? = null
        val repository = object : OrganizationInvitationRepository {
            override suspend fun acceptInvitation(code: String, email: String): OrganizationInvitationResult {
                receivedCode = code
                receivedEmail = email
                return OrganizationInvitationResult.Accepted
            }
        }
        val vm = OrganizationInvitationViewModel(
            AcceptOrganizationInvitation(repository, session), "chosen-code", "chosen-email", Dispatchers.Unconfined,
        )
        vm.accept()
        assertEquals("chosen-code", receivedCode)
        assertEquals("chosen-email", receivedEmail)
        assertTrue(session.state.value.organizationMember)
        assertEquals(DrivingContext.Personal, session.state.value.context)
        assertEquals(FeatureDestination.Accepted, vm.effects.first().destination)
    }

    @Test
    fun offlineActionsAndHelpAreViewModelState() {
        val vm = OfflineMapsViewModel(
            PerformOfflineMapsAction(UnconfiguredOfflineMapsService()), Dispatchers.Unconfined,
        )
        vm.toggleHelp()
        assertTrue(vm.uiState.value.showHelp)
        for (action in OfflineMapsAction.entries) {
            vm.perform(action)
            assertNotNull(vm.uiState.value.statusMessage)
            assertFalse(vm.uiState.value.isSubmitting)
        }
    }

    @Test
    fun historyFiltersAndUnavailableActionsAreViewModelState() {
        val vm = TripHistoryViewModel(
            PerformTripHistoryAction(UnconfiguredTripHistoryService()), Dispatchers.Unconfined,
        )
        vm.selectFilter(1)
        assertTrue(vm.uiState.value.showPersonalTrips)
        assertFalse(vm.uiState.value.showWorkTrips)
        vm.selectFilter(2)
        assertFalse(vm.uiState.value.showPersonalTrips)
        assertTrue(vm.uiState.value.showWorkTrips)
        vm.selectFilter(100)
        assertEquals(2, vm.uiState.value.selectedFilter)
        for (action in TripHistoryAction.entries) {
            vm.perform(action)
            assertNotNull(vm.uiState.value.statusMessage)
            assertFalse(vm.uiState.value.isSubmitting)
        }
        vm.selectFilter(0)
        assertNull(vm.uiState.value.statusMessage)
        vm.toggleHelp()
        assertTrue(vm.uiState.value.showHelp)
    }

    @Test
    fun personalWorkActionsNeverReachRepositoryOrSwitchContext() = runBlocking {
        val session = Session(SessionState(organizationMember = true))
        val before = session.state.value
        var calls = 0
        val repository = object : WorkTripRepository {
            override suspend fun perform(action: WorkTripAction, taskId: String): WorkTripActionResult {
                calls++
                return WorkTripActionResult.NotConfigured
            }
        }
        val task = WorkTask("DL-204", "Lviv")
        val perform = PerformWorkTripAction(repository, session)
        assertEquals(WorkTripActionResult.InvalidContext, perform(WorkTripAction.ReportArrival, task))
        assertFalse(OpenChosenWorkTask(session)(task))
        assertEquals(0, calls)
        assertEquals(before, session.state.value)
    }

    @Test
    fun previewTaskDoesNotReplaceDestinationOrSelectWorkMode() {
        val session = Session(SessionState(destination = "Personal destination"))
        val before = session.state.value
        assertTrue(OpenChosenWorkTask(session)(null))
        assertEquals(before, session.state.value)
    }

    @Test
    fun selectedTaskIdAndOfferedTaskIdRemainDistinct() = runBlocking<Unit> {
        val session = workSession()
        val chosen = WorkTask("DL-204", "Lviv")
        val offered = WorkTask("DL-205", "Kyiv")
        var receivedId: String? = null
        val repository = object : WorkTripRepository {
            override suspend fun perform(action: WorkTripAction, taskId: String): WorkTripActionResult {
                receivedId = taskId
                return WorkTripActionResult.NotConfigured
            }
        }
        val vm = WorkTasksViewModel(
            PerformWorkTripAction(repository, session), OpenChosenWorkTask(session),
            session, chosen, offered, Dispatchers.Unconfined,
        )
        vm.openTask()
        assertEquals(chosen, session.state.value.selectedTask)
        assertEquals(chosen.destination, session.state.value.destination)
        assertEquals(chosen.id, vm.effects.first().taskId)
        vm.acceptTask()
        assertEquals(offered.id, receivedId)
        assertEquals(chosen, session.state.value.selectedTask)
        assertNotNull(vm.uiState.value.statusMessage)
    }

    @Test
    fun tripDetailsPassChosenTaskToPlanAndRespectLiveSession() = runBlocking {
        val session = workSession()
        val chosen = WorkTask("DL-204", "Lviv")
        var calls = 0
        var receivedId: String? = null
        val repository = object : WorkTripRepository {
            override suspend fun perform(action: WorkTripAction, taskId: String): WorkTripActionResult {
                calls++
                receivedId = taskId
                return WorkTripActionResult.NotConfigured
            }
        }
        val vm = WorkTripDetailsViewModel(
            PerformWorkTripAction(repository, session), session, chosen, Dispatchers.Unconfined,
        )
        vm.perform(WorkTripAction.ReportDelay)
        assertEquals(chosen.id, receivedId)
        vm.openTripPlan()
        val effect = vm.effects.first()
        assertEquals(FeatureDestination.TripPlan, effect.destination)
        assertEquals(chosen.id, effect.taskId)
        session.update { it.copy(context = DrivingContext.Personal) }
        assertEquals(DrivingContext.Personal, vm.uiState.value.session.context)
        vm.perform(WorkTripAction.ReportArrival)
        assertEquals(1, calls)
        assertEquals(DrivingContext.Personal, session.state.value.context)
    }

    @Test
    fun tripPlanNeverAddsStopToWrongContextOrMissingTask() = runBlocking {
        val session = workSession()
        val chosen = WorkTask("DL-204", "Lviv")
        var receivedId: String? = null
        var calls = 0
        val repository = object : TripPlanRepository {
            override suspend fun addStop(taskId: String): TripPlanActionResult {
                calls++
                receivedId = taskId
                return TripPlanActionResult.NotConfigured
            }
        }
        val addStop = AddTripPlanStop(repository, session)
        assertEquals(TripPlanActionResult.InvalidContext, addStop(null))
        val vm = TripPlanViewModel(addStop, session, chosen, Dispatchers.Unconfined)
        vm.addStop()
        assertEquals(chosen.id, receivedId)
        assertNotNull(vm.uiState.value.statusMessage)
        session.update { it.copy(context = DrivingContext.Personal) }
        vm.addStop()
        assertEquals(1, calls)
        assertTrue(vm.uiState.value.showHelp)
        assertEquals(DrivingContext.Personal, session.state.value.context)
    }

    @Test
    fun unavailableResultResetsBusyState() {
        val repository = object : OfflineMapsRepository {
            override suspend fun perform(action: OfflineMapsAction): OfflineMapsActionResult =
                OfflineMapsActionResult.NotConfigured
        }
        val vm = OfflineMapsViewModel(PerformOfflineMapsAction(repository), Dispatchers.Unconfined)
        vm.perform(OfflineMapsAction.DownloadRegion)
        assertFalse(vm.uiState.value.isSubmitting)
        assertNotNull(vm.uiState.value.statusMessage)
    }
}
