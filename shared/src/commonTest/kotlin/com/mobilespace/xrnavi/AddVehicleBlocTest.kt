package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddVehicleBlocTest {
    @Test
    fun selectionsMoveCursorToEndAndKeepMakeFocusThenDismissKeyboard() = runTest {
        val bloc = AddVehicleBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeSelectionChanged(TextRange(1)))
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        assertEquals(TextRange("Toyota".length), bloc.state.value.makeSelection)
        assertEquals(AddVehicleInputEffect.FocusMake, bloc.state.value.inputEffect)
        runCurrent()
        assertEquals(AddVehicleInputEffect.FocusMake, bloc.state.value.inputEffect)
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        assertFalse(bloc.state.value.makeDropdownExpanded)
        assertFalse(bloc.state.value.modelDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.InputEffectHandled(AddVehicleInputEffect.FocusMake))
        assertNull(bloc.state.value.inputEffect)
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(false))
        bloc.dispatch(AddVehicleEvent.ModelFocusChanged(true))
        assertTrue(bloc.state.value.modelDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.ModelSelectionChanged(TextRange(1)))
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        assertEquals(TextRange("Focus".length), bloc.state.value.modelSelection)
        assertEquals(AddVehicleInputEffect.HideKeyboard, bloc.state.value.inputEffect)
        bloc.dispatch(AddVehicleEvent.InputEffectHandled(AddVehicleInputEffect.FocusMake))
        assertEquals(AddVehicleInputEffect.HideKeyboard, bloc.state.value.inputEffect)
        bloc.dispatch(AddVehicleEvent.InputEffectHandled(AddVehicleInputEffect.HideKeyboard))
        assertNull(bloc.state.value.inputEffect)
        runCurrent()
        assertEquals(TextRange("Focus".length), bloc.state.value.modelSelection)
    }

    @Test
    fun tabChangesPreserveFormAndCloseDropdownsWithoutReloadingCatalog() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.Equipment))
        assertEquals(AddVehicleTab.General, bloc.state.value.selectedTab)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        bloc.dispatch(AddVehicleEvent.ModelDropdownChanged(true))
        bloc.dispatch(AddVehicleEvent.InputEffectHandled(AddVehicleInputEffect.HideKeyboard))
        val original = bloc.state.value
        for (tab in AddVehicleTab.entries) {
            bloc.dispatch(AddVehicleEvent.SelectTab(tab))
            assertEquals(
                original.copy(
                    selectedTab = tab, makeDropdownExpanded = false, modelDropdownExpanded = false,
                ),
                bloc.state.value,
            )
        }
        runCurrent()
        assertEquals(1, service.makeSearchCalls)
        bloc.dispatch(AddVehicleEvent.Close)
        bloc.dispatch(AddVehicleEvent.Open)
        assertEquals(AddVehicleTab.General, bloc.state.value.selectedTab)
    }

    @Test
    fun selectedModelLoadsYearsAndSupportsFailureRetryAndYearSelection() = runTest {
        val service = FakeGarageService().apply { failYears = true }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        assertEquals("Focus", bloc.state.value.searchQuery)
        assertTrue(bloc.state.value.yearsLoading)
        assertTrue(bloc.state.value.selectedModel!!.years.isEmpty())
        runCurrent()
        assertEquals("Toyota" to "Focus", service.requestedYears)
        assertFalse(bloc.state.value.yearsLoading)
        assertTrue(bloc.state.value.yearsError.isNotEmpty())
        service.failYears = false
        bloc.dispatch(AddVehicleEvent.RetryYears)
        runCurrent()
        assertEquals(service.yearsResult, bloc.state.value.selectedModel?.years)
        assertEquals("", bloc.state.value.yearsError)
        bloc.dispatch(AddVehicleEvent.SelectYear(1999))
        assertNull(bloc.state.value.year)
        assertEquals(AddVehicleTab.General, bloc.state.value.selectedTab)
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        assertEquals(2021, bloc.state.value.year)
        assertEquals(AddVehicleTab.Equipment, bloc.state.value.selectedTab)
        assertEquals("Toyota", bloc.state.value.selectedMake?.name)
        assertEquals("Focus", bloc.state.value.selectedModel?.name)
        bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.General))
        assertEquals(2021, bloc.state.value.year)
        bloc.dispatch(AddVehicleEvent.SearchChanged("Foc"))
        assertNull(bloc.state.value.selectedModel)
        assertNull(bloc.state.value.year)
        assertEquals("Foc", bloc.state.value.searchQuery)
    }

    @Test
    fun outdatedYearResponsesCannotReplaceNewSelectionOrReopenClosedWizard() = runTest {
        for (change in listOf("model", "make", "close")) {
            val response = kotlinx.coroutines.CompletableDeferred<List<Int>>()
            val service = FakeGarageService().apply { yearsResponse = response }
            val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
            bloc.dispatch(AddVehicleEvent.Open)
            runCurrent()
            bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
            runCurrent()
            bloc.dispatch(AddVehicleEvent.SelectMake("1"))
            runCurrent()
            bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
            runCurrent()
            when (change) {
                "model" -> {
                    bloc.dispatch(AddVehicleEvent.SearchChanged("Foc"))
                    service.yearsResponse = null
                    service.yearsResult = emptyList()
                    bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
                    runCurrent()
                }
                "make" -> bloc.dispatch(AddVehicleEvent.MakeChanged("Ford"))
                "close" -> bloc.dispatch(AddVehicleEvent.Close)
            }
            response.complete(listOf(1990))
            runCurrent()
            assertFalse(bloc.state.value.yearsLoading)
            assertEquals("", bloc.state.value.yearsError)
            if (change == "model") {
                assertEquals(emptyList(), bloc.state.value.selectedModel?.years)
                assertEquals("Focus", bloc.state.value.searchQuery)
            } else {
                assertNull(bloc.state.value.selectedModel)
            }
        }
    }

    @Test
    fun selectedMakeAndModelRetainEntireDataObjects() = runTest {
        val makeData = """{"id":42,"name":"Toyota","country":"Japan","metadata":{"codes":[1,2]},"optional":null}"""
        val modelData = """{"id":10,"name":"Corolla","make_id":42,"metadata":{"active":true},"aliases":["Corolla Sedan"]}"""
        val service = RpcGarageService { method, _ ->
            when (method) {
                "catalog.classes.list" -> """{"classes":[]}"""
                "car.makes_index2_get" -> """{"data":[$makeData]}"""
                "car.oemmakemodels_index_get" -> """{"data":[$modelData]}"""
                "car.years_index2_get" -> "[2015,2016]"
                else -> error("Unexpected RPC: $method")
            }
        }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SearchChanged("toy"))
        bloc.dispatch(AddVehicleEvent.SelectMake("42"))
        runCurrent()
        assertEquals(Json.parseToJsonElement(makeData).jsonObject, bloc.state.value.selectedMake?.rawData)
        bloc.dispatch(AddVehicleEvent.SearchChanged("cor"))
        bloc.dispatch(AddVehicleEvent.SelectModel("10"))
        runCurrent()
        assertEquals(listOf(2015, 2016), bloc.state.value.selectedModel?.years)
        assertEquals(Json.parseToJsonElement(modelData).jsonObject, bloc.state.value.selectedModel?.rawData)
        assertEquals(Json.parseToJsonElement(makeData).jsonObject, bloc.state.value.selectedMake?.rawData)
        bloc.dispatch(AddVehicleEvent.Back)
        bloc.dispatch(AddVehicleEvent.MakeChanged("Ford"))
        assertNull(bloc.state.value.selectedMake)
        assertNull(bloc.state.value.selectedModel)
    }

    @Test
    fun makeSearchStartsOnFocusWithEmptyInputAndFiltersCachedResponseIgnoringCase() = runTest {
        val service = FakeGarageService()
        service.searchMakesResult = listOf(
            VehicleMake("1", "Toyota"), VehicleMake("2", "Ford"),
            VehicleMake("3", "SuperTOY Motors"),
        )
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        assertEquals(0, service.makeSearchCalls)
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        assertTrue(bloc.state.value.makeLoading)
        assertEquals("", bloc.state.value.searchQuery)
        runCurrent()
        assertEquals(service.searchMakesResult, bloc.state.value.filteredMakes)
        assertTrue(bloc.state.value.makeDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(false))
        assertFalse(bloc.state.value.makeDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        assertEquals(1, service.makeSearchCalls)
        bloc.dispatch(AddVehicleEvent.SearchChanged("tOy"))
        runCurrent()
        assertTrue(bloc.state.value.makeSearched)
        assertEquals(listOf("Toyota", "SuperTOY Motors"), bloc.state.value.filteredMakes.map { it.name })
        bloc.dispatch(AddVehicleEvent.SearchChanged("TOYOTA"))
        runCurrent()
        assertEquals(listOf("Toyota"), bloc.state.value.filteredMakes.map { it.name })
        bloc.dispatch(AddVehicleEvent.SearchChanged("FoRd"))
        runCurrent()
        assertEquals(listOf("Ford"), bloc.state.value.filteredMakes.map { it.name })
        assertEquals(1, service.makeSearchCalls)
        bloc.dispatch(AddVehicleEvent.SearchChanged("fo"))
        assertEquals(listOf("Ford"), bloc.state.value.filteredMakes.map { it.name })
        bloc.dispatch(AddVehicleEvent.SearchChanged("xyz"))
        assertTrue(bloc.state.value.filteredMakes.isEmpty())
        assertEquals(1, service.makeSearchCalls)
    }

    @Test
    fun makeSearchAllowsEditingWithoutDuplicateRequestsAndUsesLatestQuery() = runTest {
        val response = kotlinx.coroutines.CompletableDeferred<Unit>()
        val service = FakeGarageService().apply { makeSearchResponse = response }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchChanged("Toyota"))
        runCurrent()
        assertTrue(bloc.state.value.makeLoading)
        bloc.dispatch(AddVehicleEvent.SearchChanged("Ford"))
        bloc.dispatch(AddVehicleEvent.SelectMake("m"))
        runCurrent()
        assertEquals("Ford", bloc.state.value.searchQuery)
        assertEquals(AddVehicleStep.Class, bloc.state.value.step)
        assertEquals(1, service.makeSearchCalls)
        response.complete(Unit)
        runCurrent()
        assertFalse(bloc.state.value.makeLoading)
        assertTrue(bloc.state.value.makeSearched)
        assertEquals(listOf("Ford"), bloc.state.value.filteredMakes.map { it.name })
        assertEquals("1GTG6CEN0L1139305", bloc.state.value.vinQuery)
        bloc.dispatch(AddVehicleEvent.SelectMake("2"))
        runCurrent()
        assertEquals("Ford", bloc.state.value.selectedMake?.name)
        assertEquals(AddVehicleStep.Model, bloc.state.value.step)
    }

    @Test
    fun modelFocusOpensLoadedOptionsWithoutTypingAndClosesOnBlur() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.ModelFocusChanged(true))
        assertFalse(bloc.state.value.modelDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.ModelFocusChanged(true))
        assertFalse(bloc.state.value.modelDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        val models = bloc.state.value.models
        assertTrue(models.isNotEmpty())
        assertFalse(bloc.state.value.modelDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.ModelFocusChanged(true))
        assertTrue(bloc.state.value.modelDropdownExpanded)
        assertEquals("", bloc.state.value.searchQuery)
        assertEquals(models, bloc.state.value.models)
        bloc.dispatch(AddVehicleEvent.ModelFocusChanged(false))
        assertFalse(bloc.state.value.modelDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.ModelFocusChanged(true))
        bloc.dispatch(AddVehicleEvent.SelectModel(models.first().id))
        runCurrent()
        assertEquals(models.first(), bloc.state.value.selectedModel)
        assertFalse(bloc.state.value.modelDropdownExpanded)
    }

    @Test
    fun selectedMakeIsRetainedWhileModelInputIsIndependentAndMakeCanBeChanged() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SearchChanged("toy"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        assertEquals("Toyota", bloc.state.value.selectedMake?.name)
        assertEquals("", bloc.state.value.searchQuery)
        assertEquals(AddVehicleStep.Model, bloc.state.value.step)
        runCurrent()
        assertEquals("Toyota", service.requestedModelMake)
        assertFalse(bloc.state.value.makeDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.ModelDropdownChanged(true))
        assertTrue(bloc.state.value.modelDropdownExpanded)
        bloc.dispatch(AddVehicleEvent.SearchChanged("Corolla"))
        assertEquals("Toyota", bloc.state.value.selectedMake?.name)
        assertEquals("Corolla", bloc.state.value.searchQuery)
        bloc.dispatch(AddVehicleEvent.MakeChanged("for"))
        assertEquals(AddVehicleStep.Class, bloc.state.value.step)
        assertNull(bloc.state.value.selectedMake)
        assertTrue(bloc.state.value.models.isEmpty())
        assertEquals(listOf("Ford"), bloc.state.value.filteredMakes.map { it.name })
        bloc.dispatch(AddVehicleEvent.SelectMake("2"))
        runCurrent()
        assertEquals("Ford", bloc.state.value.selectedMake?.name)
        assertEquals("Ford", service.requestedModelMake)
        assertEquals("", bloc.state.value.searchQuery)
        assertEquals(AddVehicleStep.Model, bloc.state.value.step)
        assertEquals(1, service.makeSearchCalls)
    }

    @Test
    fun makeSearchFailureEnablesRetryAndEditingClearsError() = runTest {
        val response = kotlinx.coroutines.CompletableDeferred<Unit>()
        val service = FakeGarageService().apply { makeSearchResponse = response }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SearchChanged("Toyota"))
        runCurrent()
        response.completeExceptionally(GarageException(GarageException.Kind.Failed))
        runCurrent()
        assertFalse(bloc.state.value.makeLoading)
        assertTrue(bloc.state.value.makeError.isNotEmpty())
        assertFalse(bloc.state.value.makeSearched)
        service.makeSearchResponse = null
        bloc.dispatch(AddVehicleEvent.SearchChanged("Toyot"))
        assertEquals("", bloc.state.value.makeError)
        runCurrent()
        assertTrue(bloc.state.value.makeSearched)
        bloc.dispatch(AddVehicleEvent.SearchChanged("Ford"))
        assertEquals("", bloc.state.value.makeError)
    }

    @Test
    fun shortQueryFiltersPendingResultsAndModelSearchDoesNotFetchMakes() = runTest {
        val response = kotlinx.coroutines.CompletableDeferred<Unit>()
        val service = FakeGarageService().apply { makeSearchResponse = response }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SearchChanged("Toy"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SearchChanged("To"))
        response.complete(Unit)
        runCurrent()
        assertEquals(listOf("Toyota"), bloc.state.value.filteredMakes.map { it.name })
        bloc.dispatch(AddVehicleEvent.SearchChanged("toy"))
        assertEquals(listOf("Toyota"), bloc.state.value.filteredMakes.map { it.name })
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SearchChanged("Corolla"))
        runCurrent()
        assertEquals(1, service.makeSearchCalls)
        assertEquals("Corolla", bloc.state.value.searchQuery)
    }

    @Test
    fun makeSearchWithoutSessionReportsErrorWithoutCallingService() = runTest {
        val service = FakeGarageService()
        var session: AuthSession? = testSession()
        val bloc = AddVehicleBloc(service, { session }, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        session = null
        bloc.dispatch(AddVehicleEvent.SearchChanged("Toyota"))
        runCurrent()
        assertEquals(0, service.makeSearchCalls)
        assertTrue(bloc.state.value.makeError.isNotEmpty())
        assertFalse(bloc.state.value.makeLoading)
    }

    @Test
    fun previousMakeSearchCannotCompleteNewWizardRequestForSameMake() = runTest {
        val oldResponse = kotlinx.coroutines.CompletableDeferred<Unit>()
        val service = FakeGarageService().apply { makeSearchResponse = oldResponse }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SearchChanged("Toyota"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.Close)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        val newResponse = kotlinx.coroutines.CompletableDeferred<Unit>()
        service.makeSearchResponse = newResponse
        bloc.dispatch(AddVehicleEvent.SearchChanged("Toyota"))
        runCurrent()
        oldResponse.complete(Unit)
        runCurrent()
        assertTrue(bloc.state.value.makeLoading)
        assertFalse(bloc.state.value.makeSearched)
        newResponse.complete(Unit)
        runCurrent()
        assertFalse(bloc.state.value.makeLoading)
        assertTrue(bloc.state.value.makeSearched)
    }

    @Test
    fun wizardFlowAddsVehicle() = runTest {
        val bloc = AddVehicleBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectClass("c"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("m"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        assertEquals(AddVehicleStep.Details, bloc.state.value.step)
        assertFalse(bloc.state.value.canSubmit)
        bloc.dispatch(AddVehicleEvent.SelectYear(1999))
        assertNull(bloc.state.value.year)
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectTrim("tr"))
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        assertNotNull(bloc.state.value.added)
        assertEquals(AddVehicleStep.Review, bloc.state.value.step)
        assertTrue(bloc.state.value.open)
        bloc.dispatch(AddVehicleEvent.Close)
        assertFalse(bloc.state.value.open)
    }

    @Test
    fun backStepsThroughWizardAndCloses() = runTest {
        val bloc = AddVehicleBloc(FakeGarageService(), ::testSession, backgroundScope)
        assertFalse(bloc.handleBack())
        bloc.dispatch(AddVehicleEvent.Open)
        assertTrue(bloc.handleBack())
        assertTrue(bloc.state.value.open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectClass("c"))
        bloc.dispatch(AddVehicleEvent.SelectMake("m"))
        runCurrent()
        assertTrue(bloc.handleBack())
        assertEquals(AddVehicleStep.Class, bloc.state.value.step)
        assertTrue(bloc.handleBack())
        assertFalse(bloc.state.value.open)
    }

    @Test
    fun catalogFailureShowsRetry() = runTest {
        val service = FakeGarageService().apply { fail = true }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        assertTrue(bloc.state.value.error.isNotEmpty())
        service.fail = false
        bloc.dispatch(AddVehicleEvent.Retry)
        runCurrent()
        assertEquals(1, bloc.state.value.classes.size)
    }

    @Test
    fun modelLoadFailureCanBeRetried() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectClass("c"))
        runCurrent()
        service.failModels = true
        bloc.dispatch(AddVehicleEvent.SelectMake("m"))
        runCurrent()
        assertTrue(bloc.state.value.error.isNotEmpty())

        service.failModels = false
        bloc.dispatch(AddVehicleEvent.Retry)
        runCurrent()

        assertEquals(1, bloc.state.value.models.size)
        assertEquals("", bloc.state.value.error)
    }

    @Test
    fun failedAddCanBeRetriedFromDetails() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectClass("c"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("m"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectYear(2021))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectTrim("tr"))
        service.failAddVehicle = true
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()
        assertEquals(AddVehicleStep.Details, bloc.state.value.step)
        assertTrue(bloc.state.value.error.isNotEmpty())

        service.failAddVehicle = false
        bloc.dispatch(AddVehicleEvent.Submit)
        runCurrent()

        assertEquals(AddVehicleStep.Review, bloc.state.value.step)
        assertNotNull(bloc.state.value.added)
    }

    @Test
    fun catalogSearchIsOwnedByBlocAndClearedForNextStep() = runTest {
        val bloc = AddVehicleBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SearchChanged("легк"))
        assertEquals("легк", bloc.state.value.searchQuery)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectClass("c"))
        assertEquals("", bloc.state.value.searchQuery)
        bloc.dispatch(AddVehicleEvent.SelectMake("2"))
        runCurrent()
        assertEquals(AddVehicleStep.Model, bloc.state.value.step)
    }

    @Test
    fun vinInputIsStoredSeparatelyFromCatalogSearch() = runTest {
        val bloc = AddVehicleBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()

        bloc.dispatch(AddVehicleEvent.VinChanged("1FA6P8TH0J5100001"))
        bloc.dispatch(AddVehicleEvent.SearchChanged("Ford"))

        assertEquals("1FA6P8TH0J5100001", bloc.state.value.vinQuery)
        assertEquals("Ford", bloc.state.value.searchQuery)
    }

    @Test
    fun searchingVinCallsServiceWithTrimmedVin() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("  1GTG6CEN0L1139305  "))

        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()

        assertEquals("1GTG6CEN0L1139305", service.requestedVin)
        assertTrue(bloc.state.value.vinSearched)
        assertFalse(bloc.state.value.vinLoading)
        assertEquals("Toyota", bloc.state.value.selectedMake?.name)
        assertEquals("Focus", bloc.state.value.selectedModel?.name)
        assertEquals("Focus", bloc.state.value.searchQuery)
        assertEquals(2021, bloc.state.value.year)
        assertEquals(AddVehicleTab.General, bloc.state.value.selectedTab)
        assertFalse(bloc.state.value.vinDialogOpen)
    }

    @Test
    fun vinDialogCancellationIgnoresDelayedResponseAndPreservesCurrentVehicle() = runTest {
        val response = kotlinx.coroutines.CompletableDeferred<Unit>()
        val service = FakeGarageService().apply { vinDecodeResponse = response }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.OpenVinDialog)
        assertTrue(bloc.state.value.vinDialogOpen)
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        assertTrue(bloc.state.value.vinLoading)
        assertFalse(bloc.state.value.vinDialogOpen)
        bloc.dispatch(AddVehicleEvent.DismissVinDialog)
        assertTrue(bloc.state.value.open)
        assertFalse(bloc.state.value.vinDialogOpen)
        response.complete(Unit)
        runCurrent()
        assertNull(bloc.state.value.selectedMake)
        assertNull(bloc.state.value.year)
        assertFalse(bloc.state.value.vinLoading)
    }

    @Test
    fun vinLookupFailurePreservesSelectionsAndKeepsDialogOpenForRetry() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectMake("1"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.SelectModel("mo"))
        runCurrent()
        bloc.dispatch(AddVehicleEvent.OpenVinDialog)
        val selected = bloc.state.value.selectedModel
        service.decodedVin = service.decodedVin.copy(model = "Missing model")
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        assertEquals(selected, bloc.state.value.selectedModel)
        assertTrue(bloc.state.value.vinDialogOpen)
        assertTrue(bloc.state.value.vinError.isNotEmpty())
        assertTrue(bloc.state.value.canSearchVin)
        service.decodedVin = service.decodedVin.copy(model = "Focus")
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        assertFalse(bloc.state.value.vinDialogOpen)
        assertEquals(2021, bloc.state.value.year)
        assertEquals(AddVehicleTab.General, bloc.state.value.selectedTab)
    }

    @Test
    fun failedVinSearchShowsRetryableError() = runTest {
        val service = FakeGarageService().apply { failVinDecode = true }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))

        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()

        assertTrue(bloc.state.value.vinError.isNotEmpty())
        assertFalse(bloc.state.value.vinSearched)
        assertFalse(bloc.state.value.vinLoading)
    }

    @Test
    fun vinInputRejectsCyrillicForbiddenLettersAndSpecialCharacters() = runTest {
        val bloc = AddVehicleBloc(FakeGarageService(), ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()

        bloc.dispatch(AddVehicleEvent.VinChanged("іІоОqQiIoOАБВїєґ 1-gt!g6cen0l1139305"))

        assertEquals("1GTG6CEN0L1139305", bloc.state.value.vinQuery)
        assertTrue(bloc.state.value.canSearchVin)
        bloc.dispatch(AddVehicleEvent.VinChanged("12345678901234567890"))
        assertEquals("12345678901234567", bloc.state.value.vinQuery)
    }

    @Test
    fun vinSearchRequiresExactlySeventeenAllowedCharacters() = runTest {
        val service = FakeGarageService()
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()

        for (length in 0..16) {
            bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305".take(length)))
            assertFalse(bloc.state.value.canSearchVin)
            bloc.dispatch(AddVehicleEvent.SearchVin)
            runCurrent()
            assertNull(service.requestedVin)
        }

        bloc.dispatch(AddVehicleEvent.VinChanged("1FA6P8TH0J5100001"))
        assertTrue(bloc.state.value.canSearchVin)
        bloc.dispatch(AddVehicleEvent.SearchVin)
        assertFalse(bloc.state.value.canSearchVin)
        runCurrent()
        assertEquals("1FA6P8TH0J5100001", service.requestedVin)

        for (invalidVin in listOf(
            "1GTG6CEN0L113930",
            "1GTG6CEN0L11393055",
            "IGTG6CEN0L1139305",
            "OGTG6CEN0L1139305",
            "QGTG6CEN0L1139305",
            "АGTG6CEN0L1139305",
        )) {
            assertFalse(AddVehicleState(open = true, vinQuery = invalidVin).canSearchVin)
        }
    }

    @Test
    fun vinSearchStaysDisabledUntilResponseArrives() = runTest {
        val response = kotlinx.coroutines.CompletableDeferred<Unit>()
        val service = FakeGarageService().apply { vinDecodeResponse = response }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()

        assertTrue(bloc.state.value.vinLoading)
        assertFalse(bloc.state.value.vinDialogOpen)
        assertFalse(bloc.state.value.canSearchVin)
        assertFalse(bloc.state.value.vinSearched)
        bloc.dispatch(AddVehicleEvent.VinChanged("1FA6P8TH0J5100001"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        assertEquals("1GTG6CEN0L1139305", bloc.state.value.vinQuery)
        assertTrue(bloc.state.value.vinLoading)
        assertFalse(bloc.state.value.canSearchVin)
        assertEquals(1, service.vinDecodeCalls)

        response.complete(Unit)
        runCurrent()
        assertFalse(bloc.state.value.vinLoading)
        assertTrue(bloc.state.value.canSearchVin)
        assertTrue(bloc.state.value.vinSearched)
    }

    @Test
    fun vinSearchIsEnabledAgainAfterDelayedFailure() = runTest {
        val response = kotlinx.coroutines.CompletableDeferred<Unit>()
        val service = FakeGarageService().apply { vinDecodeResponse = response }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        bloc.dispatch(AddVehicleEvent.VinChanged("1GTG6CEN0L1139305"))
        bloc.dispatch(AddVehicleEvent.SearchVin)
        runCurrent()
        assertFalse(bloc.state.value.canSearchVin)

        response.completeExceptionally(GarageException(GarageException.Kind.Failed))
        runCurrent()
        assertFalse(bloc.state.value.vinLoading)
        assertTrue(bloc.state.value.canSearchVin)
        assertTrue(bloc.state.value.vinError.isNotEmpty())
        assertFalse(bloc.state.value.vinSearched)
    }

    @Test
    fun catalogGroupSelectsOnlyClassesFromThatGroupAndLoadsTheirMakes() = runTest {
        val service = FakeGarageService().apply {
            catalogClasses = listOf(
                VehicleClassItem("passenger", "Легкові"),
                VehicleClassItem("truck", "Вантажні"),
            )
        }
        val bloc = AddVehicleBloc(service, ::testSession, backgroundScope)
        bloc.dispatch(AddVehicleEvent.Open)
        runCurrent()
        assertEquals("passenger", bloc.state.value.selectedClass?.id)

        bloc.dispatch(AddVehicleEvent.SelectCatalogGroup(VehicleCatalogGroup.Cargo))
        runCurrent()

        assertEquals("truck", bloc.state.value.selectedClass?.id)
        assertEquals(VehicleCatalogGroup.Cargo, bloc.state.value.selectedCatalogGroup)
        assertEquals("truck", service.requestedMakeClassId)
        assertEquals("Ford", bloc.state.value.makes.single().name)
    }
}
