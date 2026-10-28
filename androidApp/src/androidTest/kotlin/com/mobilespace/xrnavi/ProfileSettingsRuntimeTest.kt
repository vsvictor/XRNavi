package com.mobilespace.xrnavi

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Build
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
            tap("Переглянути профіль Ford Mustang GT")
            waitFor("S11 vehicle profile") { find("Профіль автомобіля") != null }
            assertNotNull(find("Профіль автомобіля"))
            assertNotNull(find("Категорія: Легкове"))
            assertNotNull(find("Легковий · 1,4 м · 1,8 т"))
            assertNotNull(find("не визначає 1,8 т як максимальну допустиму масу"))
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
            waitFor("special category profile remains available") {
                find("Обрати Спецтехніка · демо 01") != null &&
                    find("ДОСТУПНИЙ ДЕМОПРОФІЛЬ") != null
            }
            assertNull("Filtering must not select the special profile", find("Обрано для демомаршруту"))
            tap("Легкове")
            waitFor("passenger category") { find("Легкове транспорт") != null }
            if (find("Обрати Ford Mustang GT") != null) {
                tap("Обрати Ford Mustang GT")
                waitFor("Map after establishing the Mustang baseline") {
                    find("Вибрати початкову точку") != null
                }
                tap("Гараж")
                waitFor("Garage after establishing the Mustang baseline") {
                    find("Легкове транспорт") != null
                }
            }
            assertNotNull("Test precondition must select the Mustang", find("ОБРАНО ДЛЯ ДЕМОМАРШРУТУ"))
            tap("Вантажне")
            waitFor("cargo category heading") { find("Вантажне транспорт") != null }
            waitFor("Cargo profile remains available without becoming selected") {
                find("Обрати Вантажний транспорт · демо 01") != null &&
                    find("ДОСТУПНИЙ ДЕМОПРОФІЛЬ") != null
            }
            assertNotNull(
                "Cargo filter must show an available profile",
                find("Вантажний транспорт · демо 01 · доступний демопрофіль")
            )
            assertNull("Filtering must not select the cargo profile", find("Обрано для демомаршруту"))
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
                    assertNotNull(find("Ford Mustang GT"))
                    assertNotNull(find("Категорія: Легкове"))
                    assertNotNull(find("Легковий · 1,4 м · 1,8 т"))
                    capture("S11-$suffix-$stored")
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                    waitFor("garage after S11") { find("Мій гараж") != null }
                    tap("Назад до попереднього розділу")
                    reveal("Історія поїздок і статистика відсутні; серверні дані не отримувалися.")
                    capture("disclosure-$suffix-$stored")
                    tap("Налаштування")
                    tap("Назад до попереднього розділу")
                    scrollTop()
                    capture("returned-$suffix-$stored")
                    assertNotNull("Back from settings must return to the profile header", find("Мій профіль"))
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

    private fun chooseTheme(label: String) {
        tap("Тема застосунку")
        waitFor("theme picker") { find("Системна тема не змінюється.") != null }
        tap(label)
        tap("Зрозуміло")
    }

    private fun launchFreshApp(): ActivityScenario<MainActivity> = ActivityScenario.launch(
        Intent(context, MainActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
    )

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
        val matches = nodes().filter {
            it.isVisibleToUser && (
                it.text?.contains(text) == true || it.contentDescription?.contains(text) == true
            )
        }
        return matches.firstOrNull { it.isClickable } ?: matches.firstOrNull()
    }

    private fun rootBounds(): Rect = Rect().also {
        automation.rootInActiveWindow?.getBoundsInScreen(it)
    }

    private fun tap(text: String) {
        reveal(text)
        val matched = requireNotNull(find(text))
        var target = matched
        while (!target.isClickable && target.parent != null) {
            target = target.parent
        }
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
                        "clickable=${it.isClickable} selected=${it.isSelected} bounds=$bounds"
                }
        )
    }

    private fun capture(name: String) {
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
                    " | checked=${node.isChecked} selected=${node.isSelected}"
            }
        )
    }
}
