package com.mobilespace.xrnavi

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class ProfileSettingsRuntimeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation
    private val context get() = instrumentation.targetContext
    private val output get() = File(context.getExternalFilesDir(null), "account-runtime").apply { mkdirs() }

    @Test
    fun garageVehicleProfileIsReadOnlyAndBackReturnsToGarage() {
        val readers = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        assumeTrue("Ordinary touch test must run separately from screen readers", readers.isEmpty())
        val scenario = launchFreshApp()
        try {
            tap("Продовжити без входу · деморежим")
            tap("Вибрати початкову точку")
            tap("площа Ринок, 1")
            tap("Знайти адресу")
            tap("площа Ринок, 10")
            capture("cargo-before-preview")
            tap("Переглянути маршрут")
            tap("Гараж")
            waitFor("Garage profile card") { find("Ford Mustang GT") != null }
            reveal("Переглянути профіль Ford Mustang GT")
            tap("Переглянути профіль Ford Mustang GT")
            waitFor("S11 vehicle profile") { find("Профіль автомобіля") != null }
            reveal("Категорія: Легкове")
            waitFor("S11 category") { find("Категорія: Легкове") != null }
            reveal("Легковий · 1,4 м · 1,8 т")
            waitFor("S11 specification") { find("Легковий · 1,4 м · 1,8 т") != null }
            reveal("не визначає 1,8 т як максимальну допустиму масу")
            waitFor("S11 mass disclaimer") {
                find("не визначає 1,8 т як максимальну допустиму масу") != null
            }
            assertNull(find("Запросити диспетчера"))
            capture("S11-vehicle-profile")

            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            waitFor("Garage after system Back") { find("Мій гараж") != null }
            assertNotNull(find("Обрано для демомаршруту"))
        } finally {
            scenario.close()
        }
    }

    @Test
    fun explicitCargoSelectionUpdatesTheMapProfileWithoutStartingNavigation() {
        val readers = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        assumeTrue("Ordinary touch test must run separately from screen readers", readers.isEmpty())
        val scenario = launchFreshApp()
        try {
            tap("Продовжити без входу · деморежим")
            tap("Гараж")
            waitFor("Garage demo profiles") { find("Доступні демопрофілі") != null }
            listOf(
                "Ford Mustang GT",
                "Легковий автомобіль · демо 02",
                "Вантажний транспорт · демо 01",
                "Спецтехніка · демо 01"
            ).forEach {
                reveal(it)
                assertNotNull("All four Garage profiles must be listed: $it", find(it))
            }
            scrollTop()
            tap("Спецтехніка")
            reveal("Обрати Спецтехніка · демо 01")
            waitFor("special category profile remains available") {
                find("Обрати Спецтехніка · демо 01") != null
            }
            assertNull("Filtering must not select the special profile", find("Обрано для демомаршруту"))
            scrollTop()
            tap("Легкове")
            waitFor("passenger category") { find("Легкове транспорт") != null }
            if (find("ОБРАНО ДЛЯ ДЕМОМАРШРУТУ") == null) {
                reveal("Обрати Ford Mustang GT")
                tap("Обрати Ford Mustang GT")
                waitFor("Map after establishing the Mustang baseline") {
                    find("Вибрати початкову точку") != null
                }
                tap("Гараж")
                waitFor("Garage after establishing the Mustang baseline") {
                    find("Мій гараж") != null
                }
            }
            scrollTop()
            assertNotNull("Test precondition must select the Mustang", find("ОБРАНО ДЛЯ ДЕМОМАРШРУТУ"))
            tap("Вантажне")
            waitFor("cargo category heading") { find("Вантажне транспорт") != null }
            reveal("Обрати Вантажний транспорт · демо 01")
            waitFor("Cargo profile remains available without becoming selected") {
                find("Обрати Вантажний транспорт · демо 01") != null
            }
            assertNull("Filtering must not select the cargo profile", find("Обрано для демомаршруту"))
            scrollTop()
            val cargoFilterNodes = nodes().filter {
                it.text?.toString() == "Вантажне"
            }
            assertTrue(
                "Cargo filter must be selected: " + cargoFilterNodes.joinToString {
                    "text=${it.text}, desc=${it.contentDescription}, selected=${it.isSelected}, " +
                        "checked=${it.isChecked}, state=${it.stateDescription}, clickable=${it.isClickable}"
                },
                cargoFilterNodes.any { it.isSelected || it.stateDescription == "Вибрано" }
            )
            waitFor("cargo demo vehicle") { find("Вантажний транспорт · демо 01") != null }
            tap("Обрати Вантажний транспорт · демо 01")
            waitFor("Map after explicit cargo selection") {
                find("Вибрати початкову точку") != null
            }
            assertNull(find("Почати маршрут"))

            tap("Вибрати початкову точку")
            tap("площа Ринок, 1")
            tap("Знайти адресу")
            tap("площа Ринок, 10")
            tap("Переглянути маршрут")
            waitFor("cargo route preview") { find("Почати маршрут") != null }
            assertNotNull(find("Вантажний транспорт · демо 01"))
            tap("Почати маршрут")
            waitFor("honest demo navigation warning") {
                find("Маршрут і навігацію не підключено") != null
            }
            assertNull(find("Навігацію запущено"))
            capture("garage-cargo-demo-preview-warning")
        } finally {
            scenario.close()
        }
    }

    @Test
    fun vehicleConfigurationsRequireApplyAndPreserveMapStateUntilParametersChange() {
        val readers = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        assumeTrue("Ordinary touch test must run separately from screen readers", readers.isEmpty())
        val scenario = launchFreshApp()
        try {
            waitFor("active app window before recording reproduction conditions") {
                !rootBounds().isEmpty
            }
            val appearance = context.getSharedPreferences("xrnavi_appearance", Context.MODE_PRIVATE)
                .getString("appearance.theme", "system")
            File(output, "S16-reproduction-conditions.txt").writeText(
                "Device: ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}\n" +
                    "Font scale: ${context.resources.configuration.fontScale}\n" +
                    "Accessibility services: $readers\n" +
                    "UI mode: ${context.resources.configuration.uiMode}; app theme: $appearance\n" +
                    "Orientation: ${context.resources.configuration.orientation}\n" +
                    "Display size: ${rootBounds().width()}x${rootBounds().height()}\n" +
                    "Window orientation policy: ${readOrientationOverride()}\n" +
                    "Rotation override: ${readDeviceSetting("global", "ignore_orientation_request")}\n" +
                    "Auto-rotate: ${readDeviceSetting("system", "accelerometer_rotation")}\n" +
                    "User rotation: ${readDeviceSetting("system", "user_rotation")}\n"
            )
            tap("Продовжити без входу · деморежим")
            tap("Вибрати початкову точку")
            tap("площа Ринок, 1")
            tap("Знайти адресу")
            tap("площа Ринок, 10")
            tap("Переглянути маршрут")
            waitFor("initial route preview") { find("Почати маршрут") != null }
            capture("S16-before-configuration")

            tap("Гараж")
            tap("Переглянути профіль Ford Mustang GT")
            tap("Переглянути конфігурації Ford Mustang GT")
            waitFor("vehicle-bound S16 configurations") {
                find("Синтетичний демосценарій 02") != null
            }
            waitFor("configuration count from current vehicle data") {
                find("Конфігурації · 3") != null
            }
            assertNotNull(find("Застосовано"))
            reveal("Синтетичний демосценарій 02")
            tap("Синтетичний демосценарій 02")
            waitFor("explicitly selected unapplied configuration") {
                find("Обрано · ще не застосовано") != null
            }
            assertNotNull(find("Застосовано зараз"))
            capture("S16-configuration-draft")
            reveal("Демоконфігурація з неповними даними")
            tap("Демоконфігурація з неповними даними")
            waitFor("incomplete configuration reason beside disabled Apply") {
                find("Застосування недоступне") != null && find("неповні дані") != null
            }
            capture("S16-incomplete-configuration")
            val incompleteApplyCandidates = nodes().filter {
                it.contentDescription?.contains("Застосувати конфігурацію") == true ||
                    it.text?.contains("Застосувати конфігурацію") == true
            }
            val incompleteApply = requireNotNull(
                incompleteApplyCandidates.firstOrNull { it.isClickable }
                    ?: incompleteApplyCandidates.firstOrNull()
            ) { "Incomplete configuration must expose an apply action" }
            assertFalse("Incomplete Apply must not be clickable", incompleteApply.isClickable)
            tap("Застосувати конфігурацію")
            waitFor("incomplete selection remains unapplied") {
                find("Застосування недоступне") != null &&
                    find("Обрано, ще не застосовано") != null
            }
            tap("Скасувати")
            waitFor("vehicle profile after cancel") { find("Профіль автомобіля") != null }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            waitFor("Garage after cancel") { find("Мій гараж") != null }
            capture("S16-garage-before-inactive-profile")

            tap("Переглянути профіль Легковий автомобіль · демо 02")
            waitFor("inactive vehicle profile") {
                find("Профіль автомобіля") != null &&
                    find("Легковий автомобіль · демо 02") != null
            }
            capture("S16-inactive-vehicle-profile")
            tap("Переглянути конфігурації Легковий автомобіль · демо 02")
            waitFor("inactive vehicle configuration view") {
                find("Спочатку явно оберіть його в гаражі") != null
            }
            reveal("Застосувати конфігурацію")
            capture("S16-inactive-vehicle")
            val applyActionCandidates = nodes().filter {
                it.contentDescription?.contains("Застосувати конфігурацію") == true ||
                    it.text?.contains("Застосувати конфігурацію") == true
            }
            val applyAction = requireNotNull(
                applyActionCandidates.firstOrNull { it.isClickable }
                    ?: applyActionCandidates.firstOrNull()
            ) { "Inactive profile must expose an apply action" }
            assertFalse("Inactive profile configuration must not be clickable", applyAction.isClickable)
            tap("Повернутися до гаража")

            tap("Мапа")
            waitFor("preview preserved after browse and cancel") {
                find("Почати маршрут") != null
            }
            assertNotNull(find("площа Ринок, 1"))
            assertNotNull(find("площа Ринок, 10"))

            tap("Гараж")
            tap("Переглянути профіль Ford Mustang GT")
            tap("Переглянути конфігурації Ford Mustang GT")
            reveal("Синтетичний демосценарій 02")
            tap("Синтетичний демосценарій 02")
            tap("Застосувати конфігурацію")
            waitFor("changed configuration invalidates preview") {
                find("Переглянути маршрут") != null
            }
            assertNull(find("Почати маршрут"))
            assertNotNull(find("площа Ринок, 1"))
            assertNotNull(find("площа Ринок, 10"))
            tap("Панель маршруту")
            tap("Панель маршруту")
            waitFor("expanded Map panel before summary assertion") {
                routePanelNode()?.stateDescription?.toString() == "Розгорнута"
            }
            capture("S16-map-expanded-after-configuration-apply")
            try {
                revealMapSheetContent("Габарити: Не задано · маса: Не задано")
            } catch (failure: AssertionError) {
                capture("S16-map-summary-lookup-failure")
                throw failure
            }
            revealMapSheetContent("Конфігурація: Синтетичний демосценарій 02")
            reveal("Ford Mustang GT")
            capture("S16-map-summary-after-scroll")
            reveal("Параметри конфігурації змінено")
            assertNotNull(find("Конфігурація: Синтетичний демосценарій 02"))
            assertNotNull(find("Габарити: Не задано · маса: Не задано"))
            capture("S16-applied-configuration-map")

            tap("Переглянути маршрут")
            waitFor("new preview after explicit request") { find("Почати маршрут") != null }
            assertNotNull(find("площа Ринок, 1"))
            assertNotNull(find("площа Ринок, 10"))

            tap("Гараж")
            tap("Переглянути профіль Ford Mustang GT")
            tap("Переглянути конфігурації Ford Mustang GT")
            tap("Застосувати конфігурацію")
            waitFor("reapplying unchanged configuration preserves preview") {
                find("Почати маршрут") != null
            }
            assertNotNull(find("площа Ринок, 1"))
            assertNotNull(find("площа Ринок, 10"))
            capture("S16-preview-preserved-same-configuration")
        } finally {
            scenario.close()
        }
    }

    @Test
    fun routePreferencesDraftApplyAndPreviewAcrossThemesAndOrientations() {
        val readers = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        assumeTrue("Ordinary touch test must run separately from screen readers", readers.isEmpty())
        val preferences = context.getSharedPreferences("xrnavi_appearance", Context.MODE_PRIVATE)
        val originalTheme = preferences.getString("appearance.theme", null)
        val portraitOnly = InstrumentationRegistry.getArguments().getString("portraitOnly") == "true"
        val orientations = listOf(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT to "portrait",
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE to "landscape"
        ).filter { !portraitOnly || it.second == "portrait" }
        try {
            for ((storedTheme, themeLabel) in listOf("Light" to "Світла", "Dark" to "Темна")) {
                for ((orientation, orientationLabel) in orientations) {
                    val scenario = openRouteSettingsForS04(
                        themeLabel,
                        orientation,
                        orientationLabel
                    )
                    try {
                        tap("Уникати платних доріг")
                        waitFor("S04 local draft is visibly changed") {
                            find("Зміни ще не застосовано") != null
                        }
                        assertTrue(
                            "Toll preference did not become checked: ${routeSwitchDiagnostics("Уникати платних доріг")}",
                            nodes().any {
                                it.contentDescription?.toString() == "Уникати платних доріг" &&
                                    it.isChecked
                            }
                        )
                        capture("S04-$orientationLabel-$storedTheme-draft")
                        tap("Застосувати параметри")
                        waitFor("S04 changed preferences invalidate old preview") {
                            find("Переглянути маршрут") != null &&
                                find("Почати маршрут") == null
                        }
                        assertNotNull(find("площа Ринок, 1"))
                        assertNotNull(find("площа Ринок, 10"))
                        capture("S04-$orientationLabel-$storedTheme-applied")

                        tap("Переглянути маршрут")
                        waitFor("S04 explicit new preview") { find("Почати маршрут") != null }
                        tap("Почати маршрут")
                        waitFor("S04 navigation remains disconnected") {
                            find("навігацію не підключено") != null
                        }
                        capture("S04-$orientationLabel-$storedTheme-navigation-warning")
                    } finally {
                        scenario.onActivity {
                            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                        scenario.close()
                    }
                }
            }
        } finally {
            val editor = preferences.edit()
            if (originalTheme == null) editor.remove("appearance.theme")
            else editor.putString("appearance.theme", originalTheme)
            assertTrue("Original appearance preference must be restored", editor.commit())
        }
    }

    private fun openRouteSettingsForS04(
        themeLabel: String,
        orientation: Int,
        orientationLabel: String
    ): ActivityScenario<MainActivity> {
        val scenario = launchFreshApp()
        try {
            tap("Продовжити без входу · деморежим")
            tap("Налаштування")
            chooseTheme(themeLabel)
            tap("Мапа")
            scenario.onActivity { it.requestedOrientation = orientation }
            waitFor("S04 $orientationLabel orientation") {
                val bounds = rootBounds()
                if (orientationLabel == "landscape") {
                    bounds.width() > bounds.height()
                } else {
                    bounds.height() > bounds.width()
                }
            }
            tap("Вибрати початкову точку")
            tap("площа Ринок, 1")
            tap("Знайти адресу")
            tap("площа Ринок, 10")
            tap("Переглянути маршрут")
            waitFor("S04 route preview") { find("Почати маршрут") != null }
            for (attempt in 0 until 3) {
                if (find("Параметри маршруту та альтернативи") != null) break
                val panel = requireNotNull(routePanelNode()) {
                    "Route panel must expose an accessible expansion action"
                }
                val previousLevel = panel.stateDescription?.toString()
                assertTrue(
                    "Route panel accessibility action must be accepted",
                    panel.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                )
                var nextLevel: String? = null
                waitFor("S04 route panel changes level") {
                    nextLevel = routePanelNode()?.stateDescription?.toString()
                    nextLevel != previousLevel
                }
                if (nextLevel == "Розгорнута") {
                    waitFor("S04 expanded map action") {
                        find("Параметри маршруту та альтернативи") != null
                    }
                }
            }
            waitFor("S04 expanded map panel") {
                find("Параметри маршруту та альтернативи") != null
            }
            tap("Параметри маршруту та альтернативи")
            waitFor("S04 route settings screen") {
                find("Параметри маршруту") != null &&
                    find("Застосувати параметри") != null
            }
            capture("S04-$orientationLabel-$themeLabel-entry")
            revealRouteSettingsContent("Ford Mustang GT")
            revealRouteSettingsContent("площа Ринок, 1")
            revealRouteSettingsContent("площа Ринок, 10")
            revealRouteSettingsContent("Параметри збережені локально")
            capture("S04-$orientationLabel-$themeLabel-initial")
            return scenario
        } catch (failure: Throwable) {
            scenario.close()
            throw failure
        }
    }

    @Test
    fun themesRotationDialogsAndLocalPersistence() {
        val readers = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        assumeTrue("Ordinary touch test must run separately from screen readers", readers.isEmpty())
        val preferences = context.getSharedPreferences("xrnavi_appearance", Context.MODE_PRIVATE)
        val original = preferences.getString("appearance.theme", null)
        val fontScale = context.resources.configuration.fontScale
        val portraitOnly = InstrumentationRegistry.getArguments().getString("portraitOnly") == "true"
        File(output, "conditions.txt").writeText(
            "Device: ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}\n" +
                "Font scale: $fontScale\nEnabled accessibility services: none\n" +
                "Initial requested orientation: ${
                    InstrumentationRegistry.getArguments().getString("testOrientation") ?: "unspecified"
                }\n" +
                "Rotation: requested by test Activity only; system rotation settings unchanged\n" +
                "Portrait-only run: $portraitOnly\n"
        )
        val scenario = launchFreshApp()
        try {
            tap("Продовжити без входу · деморежим")
            for ((stored, label) in listOf("Light" to "Світла", "Dark" to "Темна")) {
                tap("Налаштування")
                chooseTheme(label)
                assertEquals(stored, preferences.getString("appearance.theme", null))
                val orientations = listOf(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT to "portrait",
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE to "landscape"
                ).filter { !portraitOnly || it.second == "portrait" }
                for ((orientation, suffix) in orientations) {
                    scenario.onActivity { it.requestedOrientation = orientation }
                    waitFor("window orientation $suffix") {
                        val bounds = rootBounds()
                        if (suffix == "portrait") bounds.height() > bounds.width()
                        else bounds.width() > bounds.height()
                    }

                    tap("Налаштування")
                    scrollTop()
                    capture("S07-$suffix-$stored")
                    reveal("Системний дозвіл не перевірявся.")
                    tap("Профіль")
                    scrollTop()
                    assertNotNull(find("Демо-профіль"))
                    assertNull(find("Пошту підтверджено"))
                    capture("S08-$suffix-$stored")
                    tap("Керувати гаражем")
                    waitFor("garage from profile") { find("Мій гараж") != null }
                    tap("Переглянути профіль Ford Mustang GT")
                    waitFor("S11 vehicle profile from garage") {
                        find("Профіль автомобіля") != null
                    }
                    reveal("Категорія: Легкове")
                    waitFor("S11 vehicle name") { find("Ford Mustang GT") != null }
                    waitFor("S11 vehicle category") {
                        find("Категорія: Легкове") != null
                    }
                    waitFor("S11 vehicle specification") {
                        find("Легковий · 1,4 м · 1,8 т") != null
                    }
                    val s11Bounds = rootBounds()
                    assertTrue(
                        "S11 viewport must remain $suffix after scrolling",
                        if (suffix == "landscape") s11Bounds.width() > s11Bounds.height()
                        else s11Bounds.height() > s11Bounds.width()
                    )
                    capture("S11-$suffix-$stored")
                    tap("Переглянути конфігурації Ford Mustang GT")
                    waitFor("S16 configurations in $suffix $stored") {
                        find("Синтетичний демосценарій 02") != null
                    }
                    assertNotNull(
                        "Configuration count must reflect the current vehicle data",
                        find("Конфігурації · 3")
                    )
                    capture("S16-$suffix-$stored")
                    reveal("Синтетичний демосценарій 02")
                    tap("Синтетичний демосценарій 02")
                    waitFor("draft state in $suffix $stored") {
                        find("Обрано · ще не застосовано") != null
                    }
                    capture("S16-draft-$suffix-$stored")
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                    waitFor("S11 after S16 back") { find("Профіль автомобіля") != null }
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                    waitFor("garage after S11") { find("Мій гараж") != null }
                    tap("Назад до попереднього розділу")
                    reveal("Історія поїздок і статистика відсутні; серверні дані не отримувалися.")
                    capture("disclosure-$suffix-$stored")
                    tap("Налаштування")
                    tap("Назад до попереднього розділу")
                    scrollTop()
                    capture("returned-$suffix-$stored")
                    assertNotNull(
                        "Back from settings must return to the previous Map section",
                        find("Вибрати початкову точку")
                    )
                }
                scenario.recreate()
                tap("Налаштування")
                tap("Тема застосунку")
                capture("picker-restored-$stored")
                val selected = find(label)
                assertNotNull("Theme option should exist after Activity recreation", selected)
                val labelBounds = Rect().also(selected!!::getBoundsInScreen)
                assertTrue("Theme radio should remain checked", nodes().any { node ->
                    val bounds = Rect().also(node::getBoundsInScreen)
                    (node.isChecked || node.isSelected) &&
                        bounds.centerY() in labelBounds.top..labelBounds.bottom
                })
                tap("Зрозуміло")
                capture("restored-$stored")
            }
            chooseTheme("Як у системі")
            assertEquals("System", preferences.getString("appearance.theme", null))
            tap("Мапа")
            capture("map-returned")
        } finally {
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
            scenario.close()
            val editor = preferences.edit()
            if (original == null) editor.remove("appearance.theme")
            else editor.putString("appearance.theme", original)
            assertTrue("Original local appearance preference must be restored", editor.commit())
        }
    }

    @Test
    fun savedPlacesSearchProfileAndLocalRestoreAcrossThemesAndOrientations() {
        val readers = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        assumeTrue("Ordinary touch test must run separately from screen readers", readers.isEmpty())
        val appearance = context.getSharedPreferences("xrnavi_appearance", Context.MODE_PRIVATE)
        val savedPlaces = context.getSharedPreferences("xrnavi_saved_places", Context.MODE_PRIVATE)
        val originalTheme = appearance.getString("appearance.theme", null)
        val originalPlaces = savedPlaces.getString("saved.places.v1", null)
        assertTrue(savedPlaces.edit().remove("saved.places.v1").commit())
        assertTrue(appearance.edit().putString("appearance.theme", "Light").commit())
        val orientationArgument =
            InstrumentationRegistry.getArguments().getString("testOrientation")
        val scenario = ActivityScenario.launch<MainActivity>(
            Intent(context, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        try {
            waitFor("S37 active app window") { !rootBounds().isEmpty }
            val bounds = rootBounds()
            val orientationLabel = if (bounds.width() > bounds.height()) "landscape" else "portrait"
            assertEquals(
                "S37 runtime run must match its recorded device orientation",
                orientationArgument ?: orientationLabel,
                orientationLabel
            )
            File(output, "S37-conditions-$orientationLabel.txt").writeText(
                "Device: ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}\n" +
                    "Font scale: ${context.resources.configuration.fontScale}\n" +
                    "Enabled accessibility services: $readers\n" +
                    "Orientation: $orientationLabel\nTheme: Light, then Dark, then Light via the app UI\n"
            )
            tap("Продовжити без входу · деморежим")
            tap("Вибрати початкову точку")
            waitFor("S37 search") { find("Обране · збережене локально") != null }
            tap("Зберегти")
            waitFor("S37 add confirmed demo result") {
                find("Адресу додано до локального обраного.") != null
            }
            tap("Збережено")
            waitFor("S37 duplicate address prompt") { find("Адресу вже збережено") != null }
            tap("Відкрити обране")
            waitFor("S37 saved address list") {
                find("площа Ринок, 1") != null &&
                    find("Хмарну синхронізацію не підключено") != null
            }
            capture("S37-light-$orientationLabel-saved-list")
            tap("Тема")
            assertEquals("Dark", appearance.getString("appearance.theme", null))
            capture("S37-dark-$orientationLabel-saved-list")
            tap("Тема")
            assertEquals("Light", appearance.getString("appearance.theme", null))
            capture("S37-light-$orientationLabel-restored-list")

            scenario.recreate()
            waitFor("S37 locally persisted address after Activity recreation") {
                find("площа Ринок, 1") != null && find("Збережені місця") != null
            }
            tap("Назад")
            waitFor("S37 returns to search without selecting") {
                find("Обране · збережене локально") != null
            }
            tap("Скасувати пошук")
            tap("Профіль")
            waitFor("S37 profile entry") { find("Мій профіль") != null }
            reveal("Збережені місця")
            tap("Збережені місця")
            waitFor("S37 profile-managed saved place") {
                find("Використати як призначення") != null
            }
            tap("Перейменувати ярлик")
            waitFor("S37 rename dialog") {
                nodes().any { it.isEditable && it.isVisibleToUser }
            }
            val labelInput = requireNotNull(nodes().firstOrNull { it.isEditable && it.isVisibleToUser })
            val labelArgs = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    "Дім"
                )
            }
            assertTrue(
                "S37 label editor must accept an explicit user label",
                labelInput.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT.id, labelArgs)
            )
            tap("Зберегти")
            waitFor("S37 renamed label") { find("Дім") != null }
            tap("Видалити")
            waitFor("S37 delete confirmation") { find("Видалити з обраного?") != null }
            tap("Скасувати")
            assertNotNull(find("Дім"))
            tap("Використати як призначення")
            waitFor("S37 profile destination action handles missing Origin") {
                find("площа Ринок, 1") != null && find("Вибрати початкову точку") != null
            }
            capture("S37-profile-destination-without-origin")

            tap("Вибрати початкову точку")
            tap("Обране · збережене локально")
            waitFor("S37 address selectable for Origin") { find("Вибрати адресу") != null }
            tap("Вибрати адресу")
            waitFor("S37 Origin selection returns to Map") {
                find("площа Ринок, 1") != null &&
                    find("Редагувати адресу призначення") != null
            }
            tap("Редагувати адресу призначення")
            tap("площа Ринок, 10")
            waitFor("S37 regular destination selection preserves Origin") {
                find("площа Ринок, 1") != null && find("площа Ринок, 10") != null
            }
            capture("S37-origin-and-destination-selected")

            tap("Профіль")
            waitFor("S37 profile entry with a confirmed route") { find("Мій профіль") != null }
            reveal("Збережені місця")
            tap("Збережені місця")
            waitFor("S37 saved place available to replace destination") {
                find("Використати як призначення") != null
            }
            tap("Використати як призначення")
            waitFor("S37 profile destination action preserves Origin and replaces Destination") {
                val originAddress = nodes().count {
                    it.text?.toString() == "площа Ринок, 1" && it.isVisibleToUser
                }
                originAddress >= 2 &&
                    nodes().none { it.text?.toString() == "площа Ринок, 10" && it.isVisibleToUser }
            }
            capture("S37-profile-destination-action")
            tap("Профіль")
            waitFor("S37 profile after destination use") { find("Мій профіль") != null }
            reveal("Збережені місця")
            tap("Збережені місця")
            waitFor("S37 delete saved place") { find("Видалити") != null }
            tap("Видалити")
            waitFor("S37 delete confirmation") { find("Видалити з обраного?") != null }
            clickTextAction("Видалити")
            waitFor("S37 empty state after deletion") {
                find("Поки немає збережених місць") != null
            }
            capture("S37-profile-deletion-empty-state")
        } finally {
            scenario.close()
            val savedEditor = savedPlaces.edit()
            if (originalPlaces == null) savedEditor.remove("saved.places.v1")
            else savedEditor.putString("saved.places.v1", originalPlaces)
            assertTrue("Original local saved places must be restored", savedEditor.commit())
            val themeEditor = appearance.edit()
            if (originalTheme == null) themeEditor.remove("appearance.theme")
            else themeEditor.putString("appearance.theme", originalTheme)
            assertTrue("Original theme preference must be restored", themeEditor.commit())
        }
    }

    private fun chooseTheme(label: String) {
        tap("Тема застосунку")
        waitFor("theme picker") { find("Системна тема не змінюється.") != null }
        tap(label)
        tap("Зрозуміло")
    }

    private fun launchFreshApp(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch<MainActivity>(
            Intent(context, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        val orientation = when (
            InstrumentationRegistry.getArguments().getString("testOrientation")
        ) {
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "landscape" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> null
        }
        if (orientation != null) {
            scenario.onActivity { it.requestedOrientation = orientation }
            waitFor("requested test orientation") {
                val bounds = rootBounds()
                if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    bounds.width() > bounds.height()
                } else {
                    bounds.height() > bounds.width()
                }
            }
        }
        return scenario
    }

    private fun readDeviceSetting(namespace: String, name: String): String {
        val descriptor = automation.executeShellCommand("settings get $namespace $name")
        return FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText().trim() }
    }

    private fun readOrientationOverride(): String {
        val descriptor = automation.executeShellCommand("dumpsys window")
        val output = FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
        return output.lineSequence()
            .firstOrNull { "mSetIgnoreOrientationRequest" in it }
            ?.trim()
            ?: "not reported by WindowManager"
    }

    private fun nodes(): List<AccessibilityNodeInfo> {
        val root = automation.rootInActiveWindow ?: return emptyList()
        root.refresh()
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun visit(node: AccessibilityNodeInfo) {
            node.refresh()
            result += node
            for (index in 0 until node.childCount) node.getChild(index)?.let(::visit)
        }
        visit(root)
        return result
    }

    private fun find(text: String): AccessibilityNodeInfo? {
        val window = rootBounds()
        val matches = nodes().filter {
            val bounds = Rect().also(it::getBoundsInScreen)
            it.isVisibleToUser && !bounds.isEmpty && Rect.intersects(bounds, window) && (
                it.text?.contains(text) == true || it.contentDescription?.contains(text) == true
            )
        }
        return matches.firstOrNull { it.isClickable } ?: matches.firstOrNull()
    }

    private fun routeSwitchDiagnostics(label: String): String =
        nodes().filter {
            it.contentDescription?.toString() == label ||
                it.text?.toString()?.contains(label) == true
        }.joinToString { node ->
            "text=${node.text}, description=${node.contentDescription}, checked=${node.isChecked}, " +
                "state=${node.stateDescription}, clickable=${node.isClickable}"
        }

    private fun routePanelNode(): AccessibilityNodeInfo? =
        nodes().firstOrNull {
            it.isClickable &&
                it.stateDescription?.toString() in setOf(
                    "Згорнута",
                    "Частково розгорнута",
                    "Розгорнута"
                )
        }

    private fun rootBounds(): Rect = Rect().also {
        automation.rootInActiveWindow?.getBoundsInScreen(it)
    }

    private fun tap(text: String) {
        reveal(text)
        val target = requireNotNull(find(text))
        val bounds = Rect()
        var stableFrames = 0
        waitFor("stable target bounds: $text") {
            val current = Rect()
            target.getBoundsInScreen(current)
            stableFrames = if (current == bounds && !current.isEmpty) stableFrames + 1 else 0
            bounds.set(current)
            stableFrames >= 5
        }
        assertFalse("Nonempty target bounds: $text", bounds.isEmpty)
        injectShellInput(
            "input tap ${bounds.exactCenterX().roundToInt()} ${bounds.exactCenterY().roundToInt()}"
        )
        instrumentation.waitForIdleSync()
        automation.waitForIdle(300, 5_000)
    }

    private fun clickTextAction(text: String) {
        val textNode = requireNotNull(nodes().firstOrNull {
            it.isVisibleToUser && it.text?.toString() == text
        }) { "Visible text action not found: $text" }
        var actionNode: AccessibilityNodeInfo? = textNode
        while (actionNode != null && !actionNode.isClickable) {
            actionNode = actionNode.parent
        }
        assertTrue(
            "Text action must expose a clickable ancestor: $text",
            actionNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
        )
    }

    private fun reveal(text: String) {
        if (find(text) != null) return
        for (direction in listOf(
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        )) {
            repeat(16) {
                if (find(text) != null) return
                swipeContent(forward = direction == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            }
        }
        fail("UI target not found: $text\n${nodes().map { it.text ?: it.contentDescription }}")
    }

    private fun revealMapSheetContent(text: String) {
        repeat(8) {
            if (find(text) != null) return
            val scrollable = requireNotNull(nodes().firstOrNull { node ->
                if (!node.isScrollable) return@firstOrNull false
                val bounds = Rect().also(node::getBoundsInScreen)
                !bounds.isEmpty && Rect.intersects(bounds, rootBounds())
            }) { "Map sheet did not expose a scrollable content container for: $text" }
            val bounds = Rect().also(scrollable::getBoundsInScreen)
            val x = bounds.centerX()
            val startY = bounds.bottom - 16
            val endY = (bounds.top + bounds.height() / 2).coerceAtLeast(bounds.top + 16)
            injectShellInput(
                "input swipe $x $startY $x $endY 320"
            )
            instrumentation.waitForIdleSync()
            automation.waitForIdle(300, 5_000)
        }
        fail("Map sheet content not found after scrolling: $text\n${nodes().map { it.text ?: it.contentDescription }}")
    }

    private fun revealRouteSettingsContent(text: String) {
        if (find(text) != null) return
        repeat(8) {
            swipeRouteSettingsContent(forward = true)
            if (find(text) != null) return
        }
        fail("Route settings content not found after scrolling: $text\n" +
            nodes().map { it.text ?: it.contentDescription })
    }

    private fun swipeRouteSettingsContent(forward: Boolean) {
        val bounds = rootBounds()
        val x = bounds.width() * 0.5f
        val start = bounds.height() * if (forward) 0.68f else 0.42f
        val end = bounds.height() * if (forward) 0.42f else 0.68f
        injectShellInput(
            "input swipe ${x.roundToInt()} ${start.roundToInt()} ${x.roundToInt()} ${end.roundToInt()} 420"
        )
        instrumentation.waitForIdleSync()
        automation.waitForIdle(300, 5_000)
    }

    private fun scrollTop() {
        repeat(6) { swipeContent(forward = false) }
    }

    private fun swipeContent(forward: Boolean) {
        val bounds = rootBounds()
        val x = bounds.width() * 0.65f
        val start = bounds.height() * if (forward) 0.76f else 0.3f
        val end = bounds.height() * if (forward) 0.3f else 0.76f
        injectShellInput(
            "input swipe ${x.roundToInt()} ${start.roundToInt()} ${x.roundToInt()} ${end.roundToInt()} 420"
        )
        automation.waitForIdle(300, 5_000)
    }

    private fun injectShellInput(command: String) {
        val descriptor = automation.executeShellCommand(command)
        FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
    }

    private fun waitFor(description: String, predicate: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (predicate()) return
            SystemClock.sleep(100)
        }
        fail(
            "Timed out waiting for $description\n" +
                nodes().joinToString("\n") {
                    val bounds = Rect().also(it::getBoundsInScreen)
                    "${it.text ?: it.contentDescription} | visible=${it.isVisibleToUser} " +
                        "clickable=${it.isClickable} enabled=${it.isEnabled} " +
                        "state=${it.stateDescription} selected=${it.isSelected} bounds=$bounds"
                }
        )
    }

    private fun capture(name: String) {
        SystemClock.sleep(450)
        instrumentation.waitForIdleSync()
        automation.waitForIdle(300, 5_000)
        val bitmap = requireNotNull(automation.takeScreenshot()) { "Screenshot unavailable" }
        File(output, "$name.png").outputStream().use {
            assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it))
        }
        bitmap.recycle()
        File(output, "$name.txt").writeText(
            nodes().joinToString("\n") { node ->
                val bounds = Rect().also(node::getBoundsInScreen)
                "${node.text?.toString().orEmpty()} | ${node.contentDescription?.toString().orEmpty()} | $bounds" +
                    " | checked=${node.isChecked} enabled=${node.isEnabled} " +
                    "state=${node.stateDescription} selected=${node.isSelected} " +
                    "scrollable=${node.isScrollable}"
            }
        )
    }
}
