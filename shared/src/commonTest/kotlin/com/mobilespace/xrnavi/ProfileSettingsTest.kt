package com.mobilespace.xrnavi

import kotlin.test.*

class ProfileSettingsTest {
    @Test
    fun layoutsFollowAvailableWindowDimensionsRatherThanOrientationAlone() {
        assertEquals(AccountLayout.PhonePortrait, AccountLayout.forWindow(402f, 780f))
        assertEquals(AccountLayout.PhoneLandscape, AccountLayout.forWindow(780f, 350f))
        assertEquals(AccountLayout.TabletPortrait, AccountLayout.forWindow(716f, 940f))
        assertEquals(AccountLayout.TabletLandscape, AccountLayout.forWindow(1024f, 680f))
        assertEquals(AccountLayout.PhonePortrait, AccountLayout.forWindow(480f, 940f))
        assertEquals(AccountLayout.PhoneLandscape, AccountLayout.forWindow(1024f, 480f))
        assertEquals(AccountLayout.TabletPortrait, AccountLayout.forWindow(600f, 600f))
    }

    @Test
    fun profileIsExplicitlyUnauthenticatedWithNoPersonalData() {
        val profile = DemoProfileState("Легкове", "Особистий")
        assertEquals("Демо-профіль", profile.title)
        assertEquals("Без авторизації", profile.identity)
        assertTrue(profile.accountDetails.contains("відсутні"))
        assertTrue(profile.organization.contains("не підтверджено"))
        assertEquals(AppScreen.SignIn, initialScreenForSession(false))
    }

    @Test
    fun profileSelectionMatchesTheSelectedVehicleAcrossCategories() {
        assertEquals(demoVehicle, DemoProfileState("Легкове", "Особистий").selectedVehicle)
        listOf("Вантажне", "Спецтехніка").forEach { category ->
            val vehicle = demoVehicleForClass(category)
            assertNotNull(vehicle)
            assertEquals(category, DemoProfileState(category, "Особистий", vehicle.id).selectedVehicle?.category)
        }
    }

    @Test
    fun garageFiltersMoreThanThreeUniqueDemoProfilesByActualCategory() {
        assertEquals(4, garageVehicles(GarageFilter.All).size)
        assertEquals(2, garageVehicles(GarageFilter.Passenger).size)
        assertEquals(listOf("Вантажне"), garageVehicles(GarageFilter.Cargo).map { it.category }.distinct())
        assertEquals(listOf("Спецтехніка"), garageVehicles(GarageFilter.Special).map { it.category }.distinct())
        assertEquals(4, garageVehicles(GarageFilter.All).map { it.id }.distinct().size)
        assertTrue(garageVehicles(GarageFilter.All).all { it.specification.contains("не задані") || it.id == demoVehicle.id })
    }

    @Test
    fun explicitGarageSelectionInvalidatesPreviewWhenVehicleChangesEvenWithinCategory() {
        val repeatedSelection = selectGarageVehicle(demoVehicle.id, demoVehicle)
        assertEquals(demoVehicle.id, repeatedSelection.vehicleId)
        assertEquals("Легкове", repeatedSelection.vehicleClass)
        assertFalse(repeatedSelection.changed)

        val secondPassengerVehicle = garageVehicles(GarageFilter.Passenger).last()
        val changedPassenger = selectGarageVehicle(demoVehicle.id, secondPassengerVehicle)
        assertEquals("Легкове", changedPassenger.vehicleClass)
        assertEquals(secondPassengerVehicle.id, changedPassenger.vehicleId)
        assertTrue(changedPassenger.changed)

        val changedSelection = selectGarageVehicle(demoVehicle.id, demoVehicleForClass("Вантажне")!!)
        assertEquals("Вантажне", changedSelection.vehicleClass)
        assertTrue(changedSelection.changed)
    }

    @Test
    fun garageProfilesStayExplicitlyDemoAndRealVehicleActionsRemainUnavailable() {
        assertEquals(4, garageVehicles(GarageFilter.All).size)
        assertTrue(demoVehicle.name == "Ford Mustang GT")
        assertTrue(garageVehicles(GarageFilter.All).all { it.name.contains("демо") || it.id == demoVehicle.id })
        assertTrue(garageAddUnavailableExplanation.contains("не підключено"))
        assertTrue(garageEditUnavailableExplanation.contains("не підключені"))
        assertTrue(garageCorporateUnavailableExplanation.contains("не підключено"))
    }

    @Test
    fun profileDoesNotInventStatisticsOrMembership() {
        val profile = DemoProfileState("Легкове", "Корпоративний")
        assertTrue(profile.statisticsExplanation.contains("відсутні"))
        assertTrue(profile.organization.contains("не підключено"))
        assertFalse(profile.organization.contains("Dnipro Logistics"))
    }

    @Test
    fun firstLaunchUsesSystemWithoutWritingAnything() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsViewModel(store)
        assertEquals(ThemePreference.System, model.preference)
        assertTrue(store.values.isEmpty())
    }

    @Test
    fun allThemeModesAreStoredAndRestored() {
        val store = MemoryThemeStore()
        ThemePreference.entries.forEach {
            ThemeSettingsViewModel(store).select(it)
            assertEquals(it, ThemeSettingsViewModel(store).preference)
            assertEquals(it.name, store.values[themePreferenceKey])
        }
    }

    @Test
    fun systemModeTracksBothSystemValuesWithoutPersistingThem() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsViewModel(store)
        assertFalse(model.preference.isDark(false))
        assertTrue(model.preference.isDark(true))
        assertFalse(model.preference.isDark(false))
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
        val model = ThemeSettingsViewModel(store)
        model.toggle(systemDark = false)
        assertEquals(ThemePreference.Dark, model.preference)
        model.toggle(systemDark = true)
        assertEquals(ThemePreference.Light, model.preference)
        model.select(ThemePreference.System)
        model.toggle(systemDark = true)
        assertEquals(ThemePreference.Light, ThemeSettingsViewModel(store).preference)
    }

    @Test
    fun failedSaveDoesNotClaimSuccessOrChangeTheme() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsViewModel(store)
        model.select(ThemePreference.Dark)
        store.writable = false
        model.select(ThemePreference.Light)
        assertEquals(ThemePreference.Dark, model.preference)
        assertTrue(model.notice.contains("Не вдалося зберегти"))
        store.writable = true
        model.select(ThemePreference.Light)
        assertEquals("", model.notice)
    }

    @Test
    fun unknownStoredValueHasAnExplicitWarning() {
        val store = MemoryThemeStore()
        store.values[themePreferenceKey] = "unknown"
        val model = ThemeSettingsViewModel(store)
        assertEquals(ThemePreference.System, model.preference)
        assertTrue(model.notice.contains("не розпізнано"))
        model.select(ThemePreference.System)
        assertEquals("", model.notice)
    }

    @Test
    fun persistenceContractOnlyAcceptsThemeAndWritesOneNonsecretKey() {
        val store = MemoryThemeStore()
        val model = ThemeSettingsViewModel(store)
        ThemePreference.entries.forEach(model::select)
        assertEquals(setOf("appearance.theme"), store.values.keys)
        assertTrue(store.values.values.all { value -> ThemePreference.entries.any { it.name == value } })
    }

    @Test
    fun disconnectedSettingsHaveExplanationsAndNoEnabledSwitchState() {
        val personal = unavailablePersonalSettings
        assertEquals(
            listOf("Голосові підказки", "XR-навігація", "Сповіщення"),
            personal.map { it.title })
        (personal + unavailableOrganizationSettings).forEach {
            assertEquals("Не підключено", it.status)
            assertTrue(it.explanation.isNotBlank())
        }
        assertTrue(personal.last().explanation.contains("дозвіл не перевірявся"))
    }

    @Test
    fun sectionsReturnToMapWithoutAnAuthOrPreviewTransition() {
        var navigation = DemoSectionNavigation()
        navigation = navigation.open(DemoSection.Profile)
        assertEquals(DemoSection.Profile, navigation.section)
        navigation = navigation.open(DemoSection.Settings)
        navigation = navigation.back()
        assertEquals(DemoSection.Profile, navigation.section)
        navigation = navigation.back()
        assertEquals(DemoSection.Map, navigation.section)
    }

    @Test
    fun garageBackReturnsToTheScreenThatOpenedIt() {
        val fromMap = DemoSectionNavigation().open(DemoSection.Garage)
        assertEquals(DemoSection.Garage, fromMap.section)
        assertEquals(DemoSection.Map, fromMap.back().section)

        val fromProfile = DemoSectionNavigation()
            .open(DemoSection.Profile)
            .open(DemoSection.Garage)
        assertEquals(DemoSection.Profile, fromProfile.back().section)
        assertEquals(DemoSection.Map, fromProfile.back().back().section)
    }

    @Test
    fun vehicleDetailsAreNestedInGarageAndBackRestoresItsParent() {
        val garage = DemoSectionNavigation().open(DemoSection.Garage)
        val details = garage.open(DemoSection.VehicleDetails)

        assertEquals(DemoSection.VehicleDetails, details.section)
        assertEquals(garage, details.back())
        assertEquals(garage, details.open(DemoSection.Garage))

        val profileDetails = DemoSectionNavigation()
            .open(DemoSection.Profile)
            .open(DemoSection.VehicleDetails)
        assertEquals(DemoSection.Profile, profileDetails.back().section)
    }

    @Test
    fun vehicleDetailsUseTheSameSelectedProfileAsGarageAndMap() {
        val garageVehicle = garageVehicles(GarageFilter.All).first()
        val mapVehicle = demoVehicleForClass("Легкове")
        val detailsVehicle = assertNotNull(demoVehicleById(garageVehicle.id))

        assertSame(garageVehicle, detailsVehicle)
        assertSame(mapVehicle, detailsVehicle)
        assertEquals("Ford Mustang GT", detailsVehicle.name)
        assertEquals("Легкове", detailsVehicle.category)
        assertEquals("Легковий · 1,4 м · 1,8 т", detailsVehicle.specification)
        assertFalse(detailsVehicle.specification.contains("максимально допустима"))
    }

    @Test
    fun directSettingsReturnsToMapAndMapSelectionClearsBackTarget() {
        val settings = DemoSectionNavigation().open(DemoSection.Settings)
        assertEquals(DemoSection.Map, settings.back().section)
        val map = settings.open(DemoSection.Profile).open(DemoSection.Map)
        assertEquals(DemoSectionNavigation(), map)
    }

    private class MemoryThemeStore : ThemePreferenceStore {
        val values = mutableMapOf<String, String>()
        var writable = true
        override fun read(): String? = values[themePreferenceKey]
        override fun write(theme: ThemePreference): Boolean {
            if (!writable) return false
            values[themePreferenceKey] = theme.name
            return true
        }
    }
}
