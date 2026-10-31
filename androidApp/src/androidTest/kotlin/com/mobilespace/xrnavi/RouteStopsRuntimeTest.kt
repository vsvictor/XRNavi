package com.mobilespace.xrnavi

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RouteStopsRuntimeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation
    private val context get() = instrumentation.targetContext
    private val interactionTrace = mutableListOf<String>()
    private val output
        get() = File(context.getExternalFilesDir(null), "route-stops-runtime").apply { mkdirs() }

    @Test
    fun routeStopsDraftSearchSaveCancelThemeAndRotationStayConsistent() {
        val enabledReaders = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        assumeTrue("Ordinary touch UI test must run separately from screen readers", enabledReaders.isEmpty())

        val stopsPreferences = context.getSharedPreferences("xrnavi_route_stops", Context.MODE_PRIVATE)
        val originalStops = stopsPreferences.getString("route.stops.v1", null)
        val savedPlacesPreferences =
            context.getSharedPreferences("xrnavi_saved_places", Context.MODE_PRIVATE)
        val originalSavedPlaces = savedPlacesPreferences.getString("saved.places.v1", null)
        assertTrue(
            "Temporary empty route plan must be applied",
            stopsPreferences.edit().putString("route.stops.v1", "1:0:").commit()
        )
        val savedPlace = encodeSavedPlace(
            address = "площа Ринок, 1",
            detail = "Львів · Львівська область · Ратуша",
            label = "Мій офіс"
        )
        assertTrue(
            "Temporary saved place must be applied",
            savedPlacesPreferences.edit().putString("saved.places.v1", savedPlace).commit()
        )
        val themePreferences = context.getSharedPreferences("xrnavi_appearance", Context.MODE_PRIVATE)
        val originalTheme = themePreferences.getString("appearance.theme", null)
        val originalRotation = Pair(
            Settings.System.getInt(context.contentResolver, "accelerometer_rotation", 1),
            Settings.System.getInt(context.contentResolver, "user_rotation", 0)
        )
        val themeEditor = themePreferences.edit().putString("appearance.theme", "Light")
        assertTrue("Temporary Light theme must be applied", themeEditor.commit())

        var scenario: ActivityScenario<MainActivity>? = null
        try {
            scenario = ActivityScenario.launch<MainActivity>(
                Intent(context, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            )
            scrollUntilVisible("Продовжити без входу · деморежим")
            tap("Продовжити без входу · деморежим")
            tap("Зупинки маршруту · 0")
            waitFor("S18 empty editor") { find("Проміжних зупинок немає") != null }
            assertNotNull(find("Додати зупинку"))
            waitForRenderedTheme(dark = false)
            capture("s18-light-empty", expectedDark = false)

            tap("Додати зупинку")
            tap("Мій офіс")
            waitFor("saved-place label and confirmed address") {
                find("Мій офіс") != null &&
                    find("площа Ринок, 1") != null &&
                    find("Замінити адресу") != null
            }
            capture("s18-light-first-stop")

            tap("Додати зупинку")
            tap("площа Ринок, 10")
            waitFor("second confirmed stop") { find("Проміжні зупинки: 2") != null }
            capture("s18-two-stops-before-reorder")
            tapInRow("площа Ринок, 10", "Вище")
            waitFor("first reordered stop") { findExact("площа Ринок, 10") != null }
            reveal("площа Ринок, 1")
            assertNotNull(findExact("площа Ринок, 1"))
            capture("s18-light-reordered")

            tapInRow("площа Ринок, 10", "Замінити адресу")
            tap("вулиця Хрещатик, 22")
            waitFor("replacement address keeps both stops") {
                find("Проміжні зупинки: 2") != null &&
                    findExact("вулиця Хрещатик, 22") != null
            }
            tapInRow("площа Ринок, 1", "Видалити")
            waitFor("one remaining stop") {
                find("Проміжні зупинки: 1") != null &&
                    find("вулиця Хрещатик, 22") != null
            }

            tap("Тема")
            waitFor("Dark editor theme") { isDarkTheme() }
            waitForRenderedTheme(dark = true)
            capture("s18-dark-draft", expectedDark = true)
            rotateTo(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            assertNotNull(find("Зберегти зупинки"))
            capture("s18-dark-landscape", expectedDark = true)
            reveal("вулиця Хрещатик, 22")
            assertNotNull(findExact("вулиця Хрещатик, 22"))
            assertNotNull(find("Скасувати"))
            assertNotNull(find("Зберегти зупинки"))
            tap("Тема")
            waitFor("Light editor theme in landscape") {
                context.getSharedPreferences("xrnavi_appearance", Context.MODE_PRIVATE)
                    .getString("appearance.theme", null) == "Light"
            }
            waitForRenderedTheme(dark = false)
            capture("s18-light-landscape", expectedDark = false)
            rotateTo(scenario, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            reveal("вулиця Хрещатик, 22")
            assertNotNull(find("вулиця Хрещатик, 22"))

            tap("Зберегти зупинки")
            tap("Панель маршруту")
            tap("Панель маршруту")
            waitFor("map after saving stops") {
                find("Зупинки маршруту · 1") != null &&
                    find("План збережено локально") != null &&
                    find("Багатоточковий перегляд ще не підключено") != null
            }
            assertNotNull(find("Редагувати зупинки"))
            assertNullText("540 км")
            assertNullText("Почати маршрут")
            capture("s18-saved-multipoint-blocked")

            tap("Зупинки маршруту · 1")
            waitFor("saved stop in editor") { find("вулиця Хрещатик, 22") != null }
            tap("Додати зупинку")
            tap("площа Ринок, 10")
            tap("Скасувати")
            waitFor("map after cancel") { find("Зупинки маршруту · 1") != null }
            assertNotNull(find("Багатоточковий перегляд ще не підключено"))
            capture("s18-cancel-keeps-applied-plan")
        } finally {
            scenario?.onActivity {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            scenario?.close()
            restoreRotationSettings(originalRotation)
            val stopEditor = stopsPreferences.edit()
            if (originalStops == null) stopEditor.remove("route.stops.v1")
            else stopEditor.putString("route.stops.v1", originalStops)
            assertTrue("Original local route stops must be restored", stopEditor.commit())
            val savedPlacesEditor = savedPlacesPreferences.edit()
            if (originalSavedPlaces == null) savedPlacesEditor.remove("saved.places.v1")
            else savedPlacesEditor.putString("saved.places.v1", originalSavedPlaces)
            assertTrue("Original saved places must be restored", savedPlacesEditor.commit())
            val restoreTheme = themePreferences.edit()
            if (originalTheme == null) restoreTheme.remove("appearance.theme")
            else restoreTheme.putString("appearance.theme", originalTheme)
            assertTrue("Original theme preference must be restored", restoreTheme.commit())
        }
    }

    private fun rotateTo(scenario: ActivityScenario<MainActivity>, orientation: Int) {
        scenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        forceDeviceOrientation(orientation)
        waitFor("requested orientation") {
            val bounds = rootBounds()
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                bounds.width() > bounds.height()
            } else {
                bounds.height() > bounds.width()
            }
        }
    }

    private fun forceDeviceOrientation(orientation: Int) {
        val rotation = when (orientation) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> 1
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> 0
            else -> error("Unsupported test orientation: $orientation")
        }
        injectShellInput("settings put system accelerometer_rotation 0")
        injectShellInput("settings put system user_rotation $rotation")
    }

    private fun restoreRotationSettings(settings: Pair<Int, Int>) {
        injectShellInput("settings put system user_rotation ${settings.second}")
        injectShellInput("settings put system accelerometer_rotation ${settings.first}")
    }

    private fun tap(text: String) {
        waitFor("visible control: $text") { find(text) != null }
        tapNode(requireNotNull(find(text)))
    }

    private fun tapInRow(rowText: String, actionText: String) {
        repeat(8) {
            reveal(rowText)
            val row = requireNotNull(findExact(rowText))
            val rowBounds = Rect().also(row::getBoundsInScreen)
            val scrollContainer = nodes().firstOrNull { it.isScrollable && it.isVisibleToUser }
            val scrollBounds = scrollContainer?.let { Rect().also(it::getBoundsInScreen) } ?: rootBounds()
            val maxActionDistancePx = (250 * context.resources.displayMetrics.density).toInt()
            val action = nodes().filter {
                val bounds = Rect().also(it::getBoundsInScreen)
                it.isVisibleToUser && it.isClickable && Rect.intersects(bounds, scrollBounds) &&
                    bounds.centerY() >= rowBounds.centerY() &&
                    bounds.centerY() - rowBounds.centerY() <= maxActionDistancePx &&
                    it.text?.toString() == actionText
            }.minByOrNull {
                kotlin.math.abs(
                    Rect().also(it::getBoundsInScreen).centerY() - rowBounds.centerY()
                )
            }
            if (action != null) {
                val actionBounds = Rect().also(action::getBoundsInScreen)
                interactionTrace += "$rowText at $rowBounds -> $actionText at $actionBounds"
                tapNode(action)
                return
            }
            if (!scrollBounds.isEmpty) {
                val x = scrollBounds.centerX()
                injectShellInput(
                    "input swipe $x ${scrollBounds.bottom - 40} $x ${scrollBounds.top + scrollBounds.height() / 2} 350"
                )
                instrumentation.waitForIdleSync()
                automation.waitForIdle(300, 5_000)
            }
        }
        throw AssertionError("No visible $actionText action for $rowText")
    }

    private fun tapNode(node: AccessibilityNodeInfo) {
        if (node.isClickable) {
            assertTrue("Expected clickable control", node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        } else {
            val bounds = Rect().also(node::getBoundsInScreen)
            injectShellInput("input tap ${bounds.centerX()} ${bounds.centerY()}")
        }
        instrumentation.waitForIdleSync()
        automation.waitForIdle(300, 5_000)
    }

    private fun find(text: String): AccessibilityNodeInfo? {
        val window = rootBounds()
        return nodes().firstOrNull {
            val bounds = Rect().also(it::getBoundsInScreen)
            it.isVisibleToUser && !bounds.isEmpty && Rect.intersects(bounds, window) &&
                it.isClickable &&
                (it.text?.toString()?.contains(text) == true ||
                    it.contentDescription?.toString()?.contains(text) == true)
        } ?: nodes().firstOrNull {
            val bounds = Rect().also(it::getBoundsInScreen)
            it.isVisibleToUser && !bounds.isEmpty && Rect.intersects(bounds, window) &&
                (it.text?.toString()?.contains(text) == true ||
                    it.contentDescription?.toString()?.contains(text) == true)
        }
    }

    private fun findExact(text: String): AccessibilityNodeInfo? {
        val window = rootBounds()
        return nodes().firstOrNull {
            val bounds = Rect().also(it::getBoundsInScreen)
            it.isVisibleToUser && !bounds.isEmpty && Rect.intersects(bounds, window) &&
                it.text?.toString() == text
        }
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

    private fun rootBounds(): Rect = Rect().also {
        automation.rootInActiveWindow?.getBoundsInScreen(it)
    }

    private fun waitFor(description: String, predicate: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (predicate()) return
            SystemClock.sleep(100)
        }
        throw AssertionError(
            "Timed out waiting for $description\n${interactionTrace.joinToString("\n")}\n" +
                nodes().joinToString("\n") { node ->
                    val bounds = Rect().also(node::getBoundsInScreen)
                    "${node.text ?: node.contentDescription} visible=${node.isVisibleToUser} " +
                        "clickable=${node.isClickable} enabled=${node.isEnabled} bounds=$bounds"
                }
        )
    }

    private fun reveal(text: String) {
        repeat(8) {
            val target = findExact(text)
            val scrollable = nodes().firstOrNull { it.isScrollable && it.isVisibleToUser }
            if (scrollable == null) {
                if (target != null) return
                throw AssertionError(
                    "No visible scroll container while looking for $text\n" +
                        nodes().joinToString("\n") { node ->
                            val bounds = Rect().also(node::getBoundsInScreen)
                            "${node.text ?: node.contentDescription} visible=${node.isVisibleToUser} " +
                                "clickable=${node.isClickable} bounds=$bounds"
                        }
                )
            }
            val bounds = Rect().also(scrollable::getBoundsInScreen)
            if (target != null) {
                val targetBounds = Rect().also(target::getBoundsInScreen)
                if (Rect.intersects(bounds, targetBounds)) return
            }
            val x = bounds.centerX()
            val startY = bounds.bottom - 40
            val endY = bounds.top + bounds.height() / 2
            injectShellInput("input swipe $x $startY $x $endY 350")
            instrumentation.waitForIdleSync()
            automation.waitForIdle(300, 5_000)
        }
        throw AssertionError(
            "Could not scroll to $text\n" +
                nodes().joinToString("\n") { it.text?.toString().orEmpty() }
        )
    }

    private fun scrollUntilVisible(text: String) {
        waitFor("active app window") { !rootBounds().isEmpty }
        repeat(16) {
            if (find(text) != null) return
            val bounds = rootBounds()
            if (!bounds.isEmpty) {
                val x = bounds.centerX()
                injectShellInput(
                    "input swipe $x ${bounds.bottom - 250} $x ${bounds.top + bounds.height() / 2} 350"
                )
                instrumentation.waitForIdleSync()
                automation.waitForIdle(300, 5_000)
            }
        }
        waitFor("visible control: $text") { find(text) != null }
    }

    private fun assertNullText(text: String) {
        assertFalse("Unexpected stale route result: $text", nodes().any {
            it.isVisibleToUser && it.text?.toString()?.contains(text) == true
        })
    }

    private fun isDarkTheme(): Boolean =
        context.getSharedPreferences("xrnavi_appearance", Context.MODE_PRIVATE)
            .getString("appearance.theme", null) == "Dark"

    private fun waitForRenderedTheme(dark: Boolean) {
        val expected = if (dark) android.graphics.Color.rgb(11, 18, 32)
        else android.graphics.Color.rgb(246, 248, 252)
        val deadline = SystemClock.uptimeMillis() + 5_000
        var consecutiveMatches = 0
        while (SystemClock.uptimeMillis() < deadline) {
            val screenshot = automation.takeScreenshot()
            val actual = screenshot?.let { it.getPixel(10, it.height / 2) }
            screenshot?.recycle()
            consecutiveMatches = if (actual == expected) consecutiveMatches + 1 else 0
            if (consecutiveMatches == 3) return
            SystemClock.sleep(100)
        }
        throw AssertionError(
            "Expected rendered ${if (dark) "Dark" else "Light"} theme background " +
                "0x${expected.toUInt().toString(16)}"
        )
    }

    private fun capture(name: String, expectedDark: Boolean? = null) {
        instrumentation.waitForIdleSync()
        automation.waitForIdle(300, 5_000)
        val bitmap = requireNotNull(automation.takeScreenshot()) { "Screenshot unavailable" }
        expectedDark?.let { dark ->
            val expected = if (dark) android.graphics.Color.rgb(11, 18, 32)
            else android.graphics.Color.rgb(246, 248, 252)
            assertTrue(
                "Captured ${if (dark) "Dark" else "Light"} theme background differs",
                bitmap.getPixel(10, bitmap.height / 2) == expected
            )
        }
        File(output, "$name.png").outputStream().use {
            assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it))
        }
        bitmap.recycle()
        File(output, "$name.txt").writeText(nodes().joinToString("\n") { node ->
            val bounds = Rect().also(node::getBoundsInScreen)
            "${node.text?.toString().orEmpty()} | ${node.contentDescription?.toString().orEmpty()} | $bounds"
        })
    }

    private fun injectShellInput(command: String) {
        val descriptor = automation.executeShellCommand(command)
        java.io.FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
    }

    private fun encodeSavedPlace(address: String, detail: String, label: String): String {
        fun encode(value: String) = "${value.length}:$value"
        return "1:${encode(address)}${encode(detail)}${encode(label)}"
    }
}
