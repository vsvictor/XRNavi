package com.mobilespace.xrnavi

import androidx.compose.runtime.remember
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExistingScreensRenderingTest {
    @Test
    fun rendersExistingScreensInEightDesignVariants() {
        val cases = mutableListOf(
            DesignCase("S01-SignIn") {
                SignInScreen("", {}, "", {}, false, {}, {}, false, "", {}, {}, {}, {})
            },
            DesignCase("S02-Registration") {
                RegistrationScreen(remember { RegistrationViewModel() }, {})
            },
            DesignCase("S04-RouteSettings") {
                RouteSettingsScreen(
                    "Київ · приклад", "Львів · приклад", demoVehicle,
                    RoutePreferences(), false, false, "", {}, {}, {}, {}, {}, {})
            },
            DesignCase("S18-RouteStops") {
                RouteStopsScreen(
                    "Київ · приклад", "Львів · приклад", emptyList(), "",
                    true, {}, {}, {}, {}, {}, {}, {}, {})
            },
            DesignCase("S19-StopPlaces") {
                StopPlacesScreen(StopPlacesState(), {}, {}, {})
            },
            DesignCase("S20-StopDetails") {
                StopPlacesScreen(StopPlacesState(selectedId = stopPlaces.first().id), {}, {}, {})
            },
            DesignCase("S34-AddressSearch") {
                SearchScreen(
                    true,
                    "Київ · приклад",
                    "",
                    "Легкове",
                    demoVehicle,
                    SearchTarget.Destination,
                    "",
                    SearchStatus.Idle,
                    "Особистий",
                    emptySet(),
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    { SavedPlaceAddStatus.Invalid },
                    {},
                    {},
                    {})
            },
            DesignCase("S37-SavedPlaces") {
                SavedPlacesScreen(
                    demoPlaces.take(2).map { it.toSavedPlace() }, null, true, true,
                    "", {}, {}, {}, { _, _ -> }, {}, {})
            },
            DesignCase("S36-RoutePreview") {
                CaptureMap(
                    preview = true,
                    level = SheetLevel.Expanded
                )
            },
            DesignCase("S47-NoRoute") {
                CaptureMap(
                    preview = false,
                    level = SheetLevel.Partial,
                    empty = true
                )
            }
        )
        SheetLevel.entries.forEach { level ->
            cases += DesignCase("S03-Map-${level.name}") { CaptureMap(false, level) }
        }
        listOf(
            "S07-Settings" to DemoSection.Settings,
            "S08-Profile" to DemoSection.Profile,
            "S10-Garage" to DemoSection.Garage,
            "S11-VehicleDetails" to DemoSection.VehicleDetails,
            "S16-Configurations" to DemoSection.VehicleConfigurations
        ).forEach { (name, section) ->
            cases += DesignCase(name) {
                val themeSettings = remember {
                    ThemeSettingsViewModel(object : ThemePreferenceStore {
                        override fun read(): String = ThemePreference.System.name
                        override fun write(theme: ThemePreference): Boolean = false
                    })
                }
                DemoAccountScreen(
                    section, DemoProfileState("Легкове", "Особистий", demoVehicle.id),
                    emptyMap(), themeSettings, {}, 0, {}, {}, {}, {}, {}, {}, {}, {})
            }
        }
        RecoveryStage.entries.forEach { stage ->
            cases += DesignCase("${stage.code}-Recovery") {
                val model =
                    remember { RecoveryViewModel(previewEnabled = true).apply { openPreview(stage) } }
                check(model.stage == stage) { "Recovery capture must render ${stage.code}" }
                RecoveryScreen(model, true, {})
            }
        }
        captureDesigns(cases, "existing-captures")
    }
}

@androidx.compose.runtime.Composable
private fun CaptureMap(preview: Boolean, level: SheetLevel, empty: Boolean = false) {
    MapScreen(
        preview, true, if (empty) "" else "Київ · приклад", if (empty) "" else "Львів · приклад",
        emptyList(), level, GeoStatus.NotConfigured, "", "Легкове", demoVehicle, "Особистий",
        {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
    )
}
