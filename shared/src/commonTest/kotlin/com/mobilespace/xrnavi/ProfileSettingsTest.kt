package com.mobilespace.xrnavi

import kotlin.test.*
import com.mobilespace.xrnavi.data.routepreferences.decodeRoutePreferences
import com.mobilespace.xrnavi.data.routepreferences.encodeRoutePreferences
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferences
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferencesDraft
import com.mobilespace.xrnavi.domain.routepreferences.RoutePreferencesNotice
import com.mobilespace.xrnavi.domain.routepreferences.applyRoutePreferences
import com.mobilespace.xrnavi.data.vehicleconfigurations.EmptyVehicleConfigurationRepository
import com.mobilespace.xrnavi.domain.vehicleconfigurations.VehicleConfiguration
import com.mobilespace.xrnavi.domain.vehicleconfigurations.VehicleConfigurationAvailability
import com.mobilespace.xrnavi.domain.vehicleconfigurations.VehicleConfigurationDraft
import com.mobilespace.xrnavi.domain.vehicleconfigurations.VehicleConfigurationNotice
import com.mobilespace.xrnavi.domain.theme.ThemePreference
import com.mobilespace.xrnavi.domain.theme.ThemePreferenceStore
import com.mobilespace.xrnavi.presentation.theme.ThemeSettingsBloc
import com.mobilespace.xrnavi.presentation.theme.ThemeSettingsEvent
import com.mobilespace.xrnavi.presentation.theme.ThemeSettingsNotice
import com.mobilespace.xrnavi.domain.vehicleconfigurations.applyVehicleConfiguration
import com.mobilespace.xrnavi.domain.vehicleconfigurations.defaultVehicleConfigurationSelections
import com.mobilespace.xrnavi.domain.vehicleconfigurations.vehicleConfigurationsFor

private const val TEST_VEHICLE_ID = "test-vehicle"

class ProfileSettingsTest {
    @Test
    fun firstLaunchUsesSystemWithoutWritingAnything() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsBloc(store)
        assertEquals(ThemePreference.System, model.state.value.preference)
        assertTrue(store.values.isEmpty())
    }

    @Test
    fun allThemeModesAreStoredAndRestored() {
        val store = MemoryThemeStore()
        ThemePreference.entries.forEach {
            ThemeSettingsBloc(store).dispatch(ThemeSettingsEvent.Select(it))
            assertEquals(it, ThemeSettingsBloc(store).state.value.preference)
            assertEquals(it.name, store.values[Constants.THEME_PREFERENCE_KEY])
        }
    }

    @Test
    fun systemModeTracksBothSystemValuesWithoutPersistingThem() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsBloc(store)
        assertFalse(model.state.value.preference.isDark(false))
        assertTrue(model.state.value.preference.isDark(true))
        assertFalse(model.state.value.preference.isDark(false))
        assertTrue(store.values.isEmpty())
    }

    @Test
    fun explicitModesIgnoreSystemAppearance() {
        listOf(false, true).forEach {
            assertFalse(ThemePreference.Light.isDark(it))
            assertTrue(ThemePreference.Dark.isDark(it))
        }
    }

    @Test
    fun legacyToggleAndSettingsUseTheSamePreference() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsBloc(store)
        model.dispatch(ThemeSettingsEvent.Toggle(systemDark = false))
        assertEquals(ThemePreference.Dark, model.state.value.preference)
        model.dispatch(ThemeSettingsEvent.Toggle(systemDark = true))
        assertEquals(ThemePreference.Light, model.state.value.preference)
        model.dispatch(ThemeSettingsEvent.Select(ThemePreference.System))
        model.dispatch(ThemeSettingsEvent.Toggle(systemDark = true))
        assertEquals(ThemePreference.Light, ThemeSettingsBloc(store).state.value.preference)
    }

    @Test
    fun failedSaveDoesNotClaimSuccessOrChangeTheme() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsBloc(store)
        model.dispatch(ThemeSettingsEvent.Select(ThemePreference.Dark))
        store.writable = false
        model.dispatch(ThemeSettingsEvent.Select(ThemePreference.Light))
        assertEquals(ThemePreference.Dark, model.state.value.preference)
        assertEquals(ThemeSettingsNotice.SaveFailed, model.state.value.notice)
        store.writable = true
        model.dispatch(ThemeSettingsEvent.Select(ThemePreference.Light))
        assertNull(model.state.value.notice)
    }

    @Test
    fun unknownStoredValueHasAnExplicitWarning() {
        val store = MemoryThemeStore()
        store.values[Constants.THEME_PREFERENCE_KEY] = "unknown"
        val model = ThemeSettingsBloc(store)
        assertEquals(ThemePreference.System, model.state.value.preference)
        assertEquals(
            ThemeSettingsNotice.UnknownStoredPreference,
            model.state.value.notice
        )
        model.dispatch(ThemeSettingsEvent.Select(ThemePreference.System))
        assertNull(model.state.value.notice)
    }

    @Test
    fun persistenceContractOnlyAcceptsThemeAndWritesOneNonsecretKey() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsBloc(store)
        ThemePreference.entries.forEach {
            model.dispatch(ThemeSettingsEvent.Select(it))
        }
        assertEquals(setOf("appearance.theme"), store.values.keys)
        assertTrue(store.values.values.all { value -> ThemePreference.entries.any { it.name == value } })
    }

    @Test
    fun configurationsRemainEmptyUntilRepositoryProvidesData() {
        val configurations = EmptyVehicleConfigurationRepository.configurations()
        assertTrue(vehicleConfigurationsFor(TEST_VEHICLE_ID, configurations).isEmpty())
        assertTrue(defaultVehicleConfigurationSelections(configurations).isEmpty())
    }

    @Test
    fun unregisteredConfigurationCannotBeApplied() {
        val configuration = VehicleConfiguration(
            id = "unregistered-configuration",
            vehicleId = TEST_VEHICLE_ID,
            name = "Test-only configuration",
            summary = "Test-only data",
            availability = VehicleConfigurationAvailability.Available
        )
        val initial = mapOf(TEST_VEHICLE_ID to "previous-selection")
        val result = applyVehicleConfiguration(
            activeVehicleId = TEST_VEHICLE_ID,
            currentSelections = initial,
            configuration = configuration,
            availableConfigurations = EmptyVehicleConfigurationRepository.configurations()
        )

        assertFalse(result.applied)
        assertFalse(result.parametersChanged)
        assertEquals(initial, result.selections)
        assertEquals(VehicleConfigurationNotice.NotConnected, result.notice)
    }

    @Test
    fun availableConfigurationDefaultsAndApplyResultsStayVehicleScoped() {
        val available = VehicleConfiguration(
            id = "available-configuration",
            vehicleId = TEST_VEHICLE_ID,
            name = "Available configuration",
            summary = "Verified configuration",
            availability = VehicleConfigurationAvailability.Available
        )
        val incomplete = VehicleConfiguration(
            id = "incomplete-configuration",
            vehicleId = "other-vehicle",
            name = "Incomplete configuration",
            summary = "Incomplete data",
            availability = VehicleConfigurationAvailability.Incomplete
        )
        val catalog = listOf(available, incomplete)

        assertEquals(mapOf(TEST_VEHICLE_ID to available.id), defaultVehicleConfigurationSelections(catalog))

        val applied = applyVehicleConfiguration(
            activeVehicleId = TEST_VEHICLE_ID,
            currentSelections = mapOf(TEST_VEHICLE_ID to "previous-configuration"),
            configuration = available,
            availableConfigurations = catalog
        )
        assertTrue(applied.applied)
        assertTrue(applied.parametersChanged)
        assertEquals(mapOf(TEST_VEHICLE_ID to available.id), applied.selections)
        assertEquals(VehicleConfigurationNotice.AppliedWithUnconfirmedParameters, applied.notice)

        val unchanged = applyVehicleConfiguration(
            activeVehicleId = TEST_VEHICLE_ID,
            currentSelections = applied.selections,
            configuration = available,
            availableConfigurations = catalog
        )
        assertTrue(unchanged.applied)
        assertFalse(unchanged.parametersChanged)
        assertEquals(VehicleConfigurationNotice.AlreadyApplied, unchanged.notice)

        val rejected = applyVehicleConfiguration(
            activeVehicleId = "other-vehicle",
            currentSelections = applied.selections,
            configuration = incomplete,
            availableConfigurations = catalog
        )
        assertFalse(rejected.applied)
        assertEquals(VehicleConfigurationNotice.Incomplete, rejected.notice)
        assertEquals(applied.selections, rejected.selections)
    }

    @Test
    fun configurationDraftCancelRestoresPreviouslyAppliedSelection() {
        val configuration = VehicleConfiguration(
            id = "test-draft",
            vehicleId = TEST_VEHICLE_ID,
            name = "Test-only configuration",
            summary = "Test-only data",
            availability = VehicleConfigurationAvailability.Available
        )
        val draft = VehicleConfigurationDraft(TEST_VEHICLE_ID, "applied", "applied")
            .select(configuration)

        assertTrue(draft.hasPendingChange)
        assertEquals(configuration.id, draft.selectedConfigurationId)
        assertEquals("applied", draft.cancel().selectedConfigurationId)
        assertFalse(draft.cancel().hasPendingChange)
    }

    @Test
    fun routePreferencesRemainDraftUntilExplicitApplyAndCancelDropsChanges() {
        val vehicleId = TEST_VEHICLE_ID
        val applied = RoutePreferences(avoidTolls = true)
        val draft = RoutePreferencesDraft(vehicleId, applied, applied)
            .update(applied.copy(avoidFerries = true))
        val stored = mapOf(vehicleId to applied)

        assertTrue(draft.hasPendingChanges)
        assertEquals(applied, stored[vehicleId])
        assertEquals(applied, draft.cancel().selected)
        assertEquals(stored, applyRoutePreferences(
            activeVehicleId = vehicleId,
            preferencesByVehicle = stored,
            draft = draft.cancel(),
            activeConfigurationAvailable = true
        ).preferencesByVehicle)
    }

    @Test
    fun routePreferencesApplyOnlyToActiveVehicleAndKeepIndependentVehicleValues() {
        val initial = mapOf(
            TEST_VEHICLE_ID to RoutePreferences(avoidTolls = true),
            "cargo-vehicle" to RoutePreferences(avoidFerries = true)
        )
        val draft = RoutePreferencesDraft(
            vehicleId = TEST_VEHICLE_ID,
            applied = initial.getValue(TEST_VEHICLE_ID),
            selected = RoutePreferences(avoidUnpavedRoads = true)
        )

        val result = applyRoutePreferences(
            activeVehicleId = TEST_VEHICLE_ID,
            preferencesByVehicle = initial,
            draft = draft,
            activeConfigurationAvailable = true
        )
        assertTrue(result.applied)
        assertTrue(result.changed)
        assertEquals(RoutePreferences(avoidUnpavedRoads = true), result.preferencesByVehicle[TEST_VEHICLE_ID])
        assertEquals(RoutePreferences(avoidFerries = true), result.preferencesByVehicle["cargo-vehicle"])
        val addresses = AddressSearchState(
            origin = "площа Ринок, 1",
            destination = "площа Ринок, 10"
        )
        assertEquals("площа Ринок, 1", addresses.origin)
        assertEquals("площа Ринок, 10", addresses.destination)
    }

    @Test
    fun routePreferencesRejectWrongVehicleAndUnavailableConfiguration() {
        val preferences = mapOf(TEST_VEHICLE_ID to RoutePreferences(avoidTolls = true))
        val wrongVehicleDraft = RoutePreferencesDraft(
            vehicleId = "cargo-vehicle",
            applied = RoutePreferences(),
            selected = RoutePreferences(avoidFerries = true)
        )
        val wrongVehicle = applyRoutePreferences(
            activeVehicleId = TEST_VEHICLE_ID,
            preferencesByVehicle = preferences,
            draft = wrongVehicleDraft,
            activeConfigurationAvailable = true
        )
        assertFalse(wrongVehicle.applied)
        assertEquals(preferences, wrongVehicle.preferencesByVehicle)

        val unavailable = applyRoutePreferences(
            activeVehicleId = TEST_VEHICLE_ID,
            preferencesByVehicle = preferences,
            draft = RoutePreferencesDraft(
                TEST_VEHICLE_ID,
                preferences.getValue(TEST_VEHICLE_ID),
                RoutePreferences(avoidUnpavedRoads = true)
            ),
            activeConfigurationAvailable = false
        )
        assertFalse(unavailable.applied)
        assertEquals(preferences, unavailable.preferencesByVehicle)
        assertEquals(RoutePreferencesNotice.ConfigurationUnavailable, unavailable.notice)
    }

    @Test
    fun routePreferencesPersistencePreservesVehicleIsolationAndDefaultValuesAreNotInvented() {
        val values = mapOf(
            TEST_VEHICLE_ID to RoutePreferences(avoidTolls = true),
            "cargo-vehicle" to RoutePreferences(avoidFerries = true)
        )
        val restored = decodeRoutePreferences(encodeRoutePreferences(values))
        assertEquals(values, restored)
        assertEquals(RoutePreferences(), restored["special-vehicle"] ?: RoutePreferences())
        assertTrue(RoutePreferences().isDefault)
    }

    @Test
    fun routeSettingsReturnToMapAfterApply() {
        assertEquals(
            AppScreen.Map,
            routeSettingsReturnScreen(AppScreen.Map, preferencesChanged = true)
        )
        assertEquals(
            AppScreen.Map,
            routeSettingsReturnScreen(AppScreen.Map, preferencesChanged = false)
        )
        assertEquals(
            AppScreen.Map,
            routeSettingsReturnScreen(AppScreen.Map, preferencesChanged = false)
        )
    }

    @Test
    fun routePreferencesReapplyingSameValuesKeepsStateAndReturnsToMap() {
        val applied = RoutePreferences(avoidTolls = true)
        val stored = mapOf(TEST_VEHICLE_ID to applied)
        val result = applyRoutePreferences(
            activeVehicleId = TEST_VEHICLE_ID,
            preferencesByVehicle = stored,
            draft = RoutePreferencesDraft(TEST_VEHICLE_ID, applied, applied),
            activeConfigurationAvailable = true
        )

        assertTrue(result.applied)
        assertFalse(result.changed)
        assertEquals(stored, result.preferencesByVehicle)
        assertEquals(
            AppScreen.Map,
            routeSettingsReturnScreen(AppScreen.Map, result.changed)
        )
    }

    private class MemoryThemeStore : ThemePreferenceStore {
        val values = mutableMapOf<String, String>()
        var writable = true
        override fun read(): String? = values[Constants.THEME_PREFERENCE_KEY]
        override fun write(theme: ThemePreference): Boolean {
            if (!writable) return false
            values[Constants.THEME_PREFERENCE_KEY] = theme.name
            return true
        }
    }
}
