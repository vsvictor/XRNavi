package com.mobilespace.xrnavi

import android.content.Intent
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GarageHeroRenderingTest {
    @Test
    fun garageTitleIsBelowWordmark() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        GarageScreen(
                            state = GarageState(),
                            dispatch = {},
                            onAddVehicle = {},
                            onOpenDetails = {},
                            onEditVehicle = {},
                        )
                    }
                }
            }
            val deadline = SystemClock.uptimeMillis() + 10_000
            var wordmark: Rect? = null
            var title: Rect? = null
            while (SystemClock.uptimeMillis() < deadline && (wordmark == null || title == null)) {
                val root = instrumentation.uiAutomation.rootInActiveWindow
                wordmark = findText(root, "XRNavi")?.let { Rect().also(it::getBoundsInScreen) }
                title = findText(root, "МІЙ ГАРАЖ")?.let { Rect().also(it::getBoundsInScreen) }
                if (wordmark == null || title == null) SystemClock.sleep(100)
            }
            assertTrue("Garage wordmark must be visible", wordmark != null)
            assertTrue("Garage title must be visible", title != null)
            assertTrue("Garage title must not overlap the wordmark", title!!.top >= wordmark!!.bottom)
        } finally {
            scenario.close()
        }
    }

    @Test
    fun titleWithoutWordmarkRemainsAtBottomOfHero() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        SignInHero(
                            modifier = Modifier.fillMaxWidth().height(196.dp),
                            dark = false,
                            horizontalLayout = false,
                            title = "Мій гараж",
                            showOverlayContent = false,
                        )
                    }
                }
            }
            val deadline = SystemClock.uptimeMillis() + 10_000
            var title: Rect? = null
            while (SystemClock.uptimeMillis() < deadline && title == null) {
                title = findText(instrumentation.uiAutomation.rootInActiveWindow, "МІЙ ГАРАЖ")
                    ?.let { Rect().also(it::getBoundsInScreen) }
                if (title == null) SystemClock.sleep(100)
            }
            val density = instrumentation.targetContext.resources.displayMetrics.density
            val contentTop = IntArray(2)
            scenario.onActivity { it.findViewById<android.view.View>(android.R.id.content).getLocationOnScreen(contentTop) }
            assertTrue("Title-only hero title must be in the lower half", title != null &&
                title!!.top >= contentTop[1] + (98 * density).toInt())
        } finally {
            scenario.close()
        }
    }

    private fun findText(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.text?.toString() == text) return node
        for (index in 0 until node.childCount) {
            findText(node.getChild(index), text)?.let { return it }
        }
        return null
    }
}
