package com.mobilespace.xrnavi

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.InputDevice
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProfileSettingsRuntimeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation
    private val context get() = instrumentation.targetContext
    private val output get() = File(context.getExternalFilesDir(null), "account-runtime").apply { mkdirs() }

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
        val scenario = ActivityScenario.launch(MainActivity::class.java)
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
                    tap("Керувати гаражем · не підключено")
                    waitFor("garage explanation") {
                        find("Повний гараж і редагування автомобілів не підключено.") != null
                    }
                    tap("Зрозуміло")
                    tap("Деталі демоавто · не підключено")
                    waitFor("car detail explanation") {
                        find("Деталі авто й редагування не підключено.") != null
                    }
                    capture("details-$suffix-$stored")
                    tap("Зрозуміло")
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

    private fun nodes(): List<AccessibilityNodeInfo> {
        val root = automation.rootInActiveWindow ?: return emptyList()
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun visit(node: AccessibilityNodeInfo) {
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
        val bounds = Rect()
        var stableFrames = 0
        waitFor("stable target bounds: $text") {
            val current = Rect()
            find(text)?.getBoundsInScreen(current)
            stableFrames = if (current == bounds && !current.isEmpty) stableFrames + 1 else 0
            bounds.set(current)
            stableFrames >= 2
        }
        assertFalse("Nonempty target bounds: $text", bounds.isEmpty)
        val now = SystemClock.uptimeMillis()
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
            val event = MotionEvent.obtain(
                now, SystemClock.uptimeMillis(), action, bounds.exactCenterX(), bounds.exactCenterY(), 0
            ).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
            assertTrue("Touch injection failed: $text", automation.injectInputEvent(event, true))
            event.recycle()
            if (action == MotionEvent.ACTION_DOWN) SystemClock.sleep(60)
        }
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
        val downTime = SystemClock.uptimeMillis()
        for (step in 0..20) {
            val action = when (step) {
                0 -> MotionEvent.ACTION_DOWN
                20 -> MotionEvent.ACTION_UP
                else -> MotionEvent.ACTION_MOVE
            }
            val event = MotionEvent.obtain(
                downTime, SystemClock.uptimeMillis(), action,
                x, start + (end - start) * step / 20f, 0
            ).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
            assertTrue("Scroll touch injection failed", automation.injectInputEvent(event, true))
            event.recycle()
            SystemClock.sleep(20)
        }
        automation.waitForIdle(300, 5_000)
    }

    private fun waitFor(description: String, predicate: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (predicate()) return
            SystemClock.sleep(100)
        }
        fail("Timed out waiting for $description")
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
