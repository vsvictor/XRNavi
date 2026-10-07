package com.mobilespace.xrnavi

import com.mobilespace.xrnavi.data.DemoVehicleRepository
import com.mobilespace.xrnavi.domain.*
import com.mobilespace.xrnavi.presentation.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VehicleArchitectureTest {
    @Test
    fun garageSelectionUpdatesSharedContextWithoutResettingRouting() = withScope { scope ->
        val session = TestVehicleSession(SessionState(organizationMember = true,
            routingSettings = RoutingSettings(avoidTolls = true)))
        val garage = GarageViewModel(session, scope)
        assertTrue(garage.selectVehicle(VehicleId.Man))
        assertEquals(DrivingContext.Work, session.state.value.context)
        assertEquals(VehicleId.Man, garage.state.value.selectedVehicle)
        assertTrue(session.state.value.routingSettings.avoidTolls)
        assertTrue(garage.selectVehicle(VehicleId.Mustang))
        assertEquals(DrivingContext.Personal, session.state.value.context)
    }

    @Test
    fun organizationVehicleRequiresMembership() = withScope { scope ->
        val session = TestVehicleSession()
        val garage = GarageViewModel(session, scope)
        assertFalse(garage.selectVehicle(VehicleId.Man))
        assertEquals(VehicleId.Mustang, session.state.value.selectedVehicle)
        assertEquals(VehicleMessage.MembershipRequired, garage.state.value.message)
    }

    @Test
    fun reopeningCurrentTruckPreservesSelectedTask() {
        val task = WorkTask("delivery", "Kyiv")
        val session = TestVehicleSession(SessionState(context = DrivingContext.Work, selectedVehicle = VehicleId.Man,
            organizationMember = true, selectedTask = task))
        assertTrue(SelectGarageVehicle(session)(VehicleId.Man))
        assertEquals(task, session.state.value.selectedTask)
        assertTrue(SelectGarageVehicle(session)(VehicleId.Mustang))
        assertNull(session.state.value.selectedTask)
    }

    @Test
    fun garageFiltersHaveCorrectVisibility() = withScope { scope ->
        val vm = GarageViewModel(TestVehicleSession(), scope)
        vm.selectCategory(2)
        assertFalse(vm.state.value.showPersonal)
        assertTrue(vm.state.value.showTruck)
        assertFalse(vm.state.value.showSpecial)
        vm.selectCategory(3)
        assertTrue(vm.state.value.showSpecial)
        assertFalse(vm.state.value.showTruck)
        vm.selectCategory(99)
        assertEquals(3, vm.state.value.selectedCategory)
    }

    @Test
    fun catalogSearchMatchesModelsAndUsesStableMakeIds() {
        val vm = AddVehicleViewModel(DemoVehicleRepository())
        vm.search("  rAv4  ")
        assertEquals(listOf(MakeId.Toyota), vm.state.value.makes.map { it.id })
        assertFalse(vm.chooseMake(MakeId.Toyota))
        assertEquals(VehicleMessage.MakeUnavailable(MakeId.Toyota), vm.state.value.message)
        assertFalse(vm.chooseMake(MakeId.Ford))
        vm.search("Focus")
        assertTrue(vm.chooseMake(MakeId.Ford))
    }

    @Test
    fun specialClassRemovesPassengerCatalogAndClassChangeClearsSpecialSelection() {
        val vm = AddVehicleViewModel(DemoVehicleRepository())
        vm.selectSpecialClass(1)
        assertEquals(2, vm.state.value.vehicleClass)
        assertTrue(vm.state.value.makes.isEmpty())
        vm.selectClass(0)
        assertEquals(-1, vm.state.value.specialClass)
        assertEquals(4, vm.state.value.makes.size)
        vm.manualEntry()
        assertEquals(VehicleMessage.ManualEntryUnavailable, vm.state.value.message)
    }

    @Test
    fun fordValidationPreparesPersonalDraftInsteadOfCorporateProfile() = withScope { scope ->
        val session = TestVehicleSession(SessionState(context = DrivingContext.Work, selectedVehicle = VehicleId.Man,
            organizationMember = true, selectedTask = WorkTask("job", "Odesa")))
        val vehicles = DemoVehicleRepository()
        val ford = FordConfigurationViewModel(session, vehicles, scope)
        val profile = VehicleProfileViewModel(session, vehicles, RecordingVehicleOperations(), scope)
        ford.selectYear(3)
        ford.nextTrim()
        assertTrue(ford.validateParameters())
        assertEquals(VehicleId.Mustang, session.state.value.selectedVehicle)
        assertEquals(DrivingContext.Personal, session.state.value.context)
        assertNull(session.state.value.selectedTask)
        assertTrue(profile.state.value.isPersonal)
        assertEquals(2025, profile.state.value.ford.year)
        assertEquals(FordTrim.EcoBoost, profile.state.value.ford.trim)
    }

    @Test
    fun unsupportedFordModelNeverNavigatesOrChangesSession() = withScope { scope ->
        val session = TestVehicleSession()
        val ford = FordConfigurationViewModel(session, DemoVehicleRepository(), scope)
        ford.nextTrim()
        ford.selectModel(1)
        assertEquals(FordTrim.GT, ford.state.value.specification.trim)
        assertFalse(ford.validateParameters())
        assertEquals(VehicleMessage.CatalogUnavailable, ford.state.value.message)
        ford.selectModel(99)
        ford.selectYear(-1)
        assertEquals(FordModel.Focus, ford.state.value.specification.model)
        assertEquals(2024, ford.state.value.specification.year)
    }

    @Test
    fun truckLoadAcceptsCommaAndRejectsNonFiniteOrOutOfRangeValues() {
        val validate = ValidateTruckLoad()
        assertEquals(36.8, validate("36,8", 40.0))
        assertEquals(0.0, validate("0", 40.0))
        assertEquals(40.0, validate("40", 40.0))
        listOf("", "-1", "40.1", "NaN", "Infinity").forEach { assertNull(validate(it, 40.0)) }
    }

    @Test
    fun personalProfileNeverValidatesTruckLoadAndPersistsReviewDraft() = withScope { scope ->
        val session = TestVehicleSession()
        val vehicles = DemoVehicleRepository()
        val operations = RecordingVehicleOperations()
        val vm = VehicleProfileViewModel(session, vehicles, operations, scope)
        vm.changeActualLoad("invalid")
        assertTrue(vm.state.value.saveEnabled)
        vm.changeFordParameter(FordParameter.Height, "1,5")
        assertEquals("1,5", vm.state.value.fields.height)
        assertEquals(1.5, vehicles.fordDraft.value.dimensions.height)
        vm.save()
        assertEquals(VehicleId.Mustang, operations.requests.single().vehicle)
        assertNull(operations.requests.single().actualLoad)
        assertEquals(1.5, operations.requests.single().ford?.dimensions?.height)
        assertEquals(VehicleMessage.OperationUnavailable(VehicleOperation.SaveProfile), vm.state.value.message)
        val reopened = VehicleProfileViewModel(session, vehicles, operations, scope)
        assertEquals("1.5", reopened.state.value.fields.height)
    }

    @Test
    fun invalidPersonalParametersCannotSave() = withScope { scope ->
        val operations = RecordingVehicleOperations()
        val vm = VehicleProfileViewModel(TestVehicleSession(), DemoVehicleRepository(), operations, scope)
        vm.changeFordParameter(FordParameter.Mass, "NaN")
        assertFalse(vm.state.value.saveEnabled)
        vm.save()
        assertTrue(operations.requests.isEmpty())
        vm.changeFordParameter(FordParameter.Mass, "0")
        assertFalse(vm.state.value.saveEnabled)
    }

    @Test
    fun truckProfileValidationAndAvoidTollsAreShared() = withScope { scope ->
        val session = truckSession()
        val operations = RecordingVehicleOperations()
        val vehicles = DemoVehicleRepository()
        val vm = VehicleProfileViewModel(session, vehicles, operations, scope)
        assertFalse(vm.state.value.isPersonal)
        vm.changeActualLoad("41")
        assertFalse(vm.state.value.saveEnabled)
        vm.save()
        assertTrue(operations.requests.isEmpty())
        vm.changeActualLoad("36,8")
        vm.toggleAvoidTolls()
        assertTrue(session.state.value.routingSettings.avoidTolls)
        vm.save()
        assertEquals(36.8, operations.requests.single().actualLoad)
        assertTrue(operations.requests.single().avoidTolls)
        session.update { it.copy(selectedVehicle = VehicleId.Mustang, context = DrivingContext.Personal) }
        assertTrue(vm.state.value.isPersonal)
        assertTrue(vm.state.value.avoidTolls)
    }

    @Test
    fun emptyConfigurationIsSelectedAndActionCarriesItsId() = withScope { scope ->
        val operations = RecordingVehicleOperations()
        val vm = VehicleConfigurationsViewModel(truckSession(), DemoVehicleRepository(), operations, scope)
        vm.selectConfiguration(0)
        assertEquals(ConfigurationId.Empty, vm.state.value.selected)
        vm.applyConfiguration()
        assertEquals(ConfigurationId.Empty, operations.requests.single().configuration)
        assertEquals(VehicleMessage.OperationUnavailable(VehicleOperation.ApplyConfiguration), vm.state.value.message)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun recoveryConfigurationUsesItsOwnLimits() = withScope { scope ->
        val vm = VehicleConfigurationsViewModel(truckSession(), DemoVehicleRepository(), RecordingVehicleOperations(), scope)
        vm.selectConfiguration(2)
        assertEquals(7.2, vm.state.value.selectedConfiguration?.maximumMass)
        assertEquals(3.6, vm.state.value.selectedConfiguration?.maximumAxleMass)
    }

    @Test
    fun personalConfigurationsNeverExposeCorporateFixturesOrDispatcherRequests() = withScope { scope ->
        val session = truckSession()
        val operations = RecordingVehicleOperations()
        val vm = VehicleConfigurationsViewModel(session, DemoVehicleRepository(), operations, scope)
        session.update { it.copy(selectedVehicle = VehicleId.Mustang, context = DrivingContext.Personal) }
        assertTrue(vm.state.value.isPersonal)
        assertTrue(vm.state.value.configurations.isEmpty())
        vm.requestDispatcherChange()
        assertTrue(operations.requests.isEmpty())
    }

    @Test
    fun submittingGuardPreventsDuplicateRequestsAndFailureClearsBusyState() = withScope { scope ->
        val response = CompletableDeferred<VehicleOperationResult>()
        var calls = 0
        val operations = object : VehicleOperationsRepository {
            override suspend fun perform(request: VehicleOperationRequest): VehicleOperationResult {
                calls++
                return response.await()
            }
        }
        val vm = VehicleConfigurationsViewModel(truckSession(), DemoVehicleRepository(), operations, scope)
        vm.applyConfiguration()
        vm.applyConfiguration()
        assertEquals(1, calls)
        assertTrue(vm.state.value.isSubmitting)
        response.complete(VehicleOperationResult.Failed)
        assertFalse(vm.state.value.isSubmitting)
        assertEquals(VehicleMessage.OperationFailed, vm.state.value.message)
    }

    @Test
    fun unavailableServiceReturnsOperationUnavailableAndResetsBusyState() = withScope { scope ->
        val unavailable = object : VehicleOperationsRepository {
            override suspend fun perform(request: VehicleOperationRequest): VehicleOperationResult =
                VehicleOperationResult.NotConfigured
        }
        val vm = VehicleProfileViewModel(TestVehicleSession(), DemoVehicleRepository(), unavailable, scope)
        vm.save()
        assertEquals(VehicleMessage.OperationUnavailable(VehicleOperation.SaveProfile), vm.state.value.message)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun domainRejectsInvalidOrIncompleteRequestsBeforeAdapter() = withScope { scope ->
        val adapter = RecordingVehicleOperations()
        val perform = PerformVehicleOperation(DemoVehicleRepository(), adapter)
        var result: VehicleOperationResult? = null
        scope.launch {
            result = perform(VehicleOperationRequest(VehicleOperation.SaveProfile, VehicleId.Mustang))
        }
        assertEquals(VehicleOperationResult.Invalid, result)
        scope.launch {
            result = perform(VehicleOperationRequest(VehicleOperation.SaveProfile, VehicleId.Man,
                ConfigurationId.Recovery, actualLoad = 8.0))
        }
        assertEquals(VehicleOperationResult.Invalid, result)
        assertTrue(adapter.requests.isEmpty())
    }

    private fun truckSession() = TestVehicleSession(SessionState(
        context = DrivingContext.Work, selectedVehicle = VehicleId.Man, organizationMember = true))

    private fun withScope(test: (CoroutineScope) -> Unit) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try { test(scope) } finally { scope.cancel() }
    }
}

private class TestVehicleSession(initial: SessionState = SessionState()) : SessionRepository {
    private val mutableState = MutableStateFlow(initial)
    override val state = mutableState.asStateFlow()
    override fun update(transform: (SessionState) -> SessionState) {
        mutableState.value = transform(mutableState.value)
    }
}

private class RecordingVehicleOperations : VehicleOperationsRepository {
    val requests = mutableListOf<VehicleOperationRequest>()
    override suspend fun perform(request: VehicleOperationRequest): VehicleOperationResult {
        requests += request
        return VehicleOperationResult.NotConfigured
    }
}
