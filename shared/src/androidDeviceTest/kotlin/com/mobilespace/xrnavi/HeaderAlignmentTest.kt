package com.mobilespace.xrnavi

import android.content.Intent
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderAlignmentTest {
    @Test
    fun mapGarageAndProfileWordmarksShareTopPosition() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            fun measure(content: @Composable () -> Unit): Rect {
                scenario.onActivity { activity ->
                    activity.setContent { XrnaviTheme { content() } }
                }
                instrumentation.waitForIdleSync()
                val deadline = SystemClock.uptimeMillis() + 10_000
                var bounds: Rect? = null
                while (bounds == null && SystemClock.uptimeMillis() < deadline) {
                    bounds = findLogo(instrumentation.uiAutomation.rootInActiveWindow)
                        ?.let { Rect().also(it::getBoundsInScreen) }
                    if (bounds == null) SystemClock.sleep(100)
                }
                assertNotNull("Wordmark logo must be visible", bounds)
                return requireNotNull(bounds)
            }

            val map = measure {
                MapTopBar(false, false, "", {}, {}, {}, overlay = true)
            }
            val garage = measure {
                GarageScreen(GarageState(), {}, {}, {}, {})
            }
            val profile = measure { AccountPageHero("Мій профіль") }
            val profileWithBack = measure { AccountPageHero("Безпека", onBack = {}) }
            assertEquals("Map and garage logos must align", garage.top, map.top)
            assertEquals("Profile and garage logos must align", garage.top, profile.top)
            assertEquals("Back button must not add spacing above the logo", garage.top, profileWithBack.top)
        } finally {
            scenario.close()
        }
    }

    private fun findLogo(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.contentDescription?.toString() == "Знак Mobile Space 01 Orbit") return node
        for (index in 0 until node.childCount) {
            findLogo(node.getChild(index))?.let { return it }
        }
        return null
    }
}
