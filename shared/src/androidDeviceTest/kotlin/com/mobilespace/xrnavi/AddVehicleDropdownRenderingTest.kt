package com.mobilespace.xrnavi

import android.content.Intent
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.SystemClock
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class AddVehicleDropdownRenderingTest {
    @Test
    fun expandedMenuSurvivesWindowShrinkingBelowItsContentHeight() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val originalFlags = automation.serviceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val selected = AtomicBoolean(false)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        AddVehicleDropdown(
                            value = "",
                            placeholder = "Make",
                            options = (1..100).map { it.toString() to "Make $it" },
                            expanded = true,
                            enabled = true,
                            tag = "dropdown-regression",
                            onValueChange = {},
                            onExpandedChange = {},
                            onSelected = { selected.set(true) },
                        )
                    }
                }
            }
            awaitOption()
            for (height in listOf(600, 240, 160, 600)) {
                scenario.onActivity { activity ->
                    activity.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
                }
                instrumentation.waitForIdleSync()
                SystemClock.sleep(250)
            }
            scenario.onActivity { activity ->
                activity.window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            var option = awaitOption()
            while (!option.isClickable) {
                option = requireNotNull(option.parent)
            }
            assertTrue(option.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            instrumentation.waitForIdleSync()
            assertTrue(selected.get())
        } finally {
            scenario.close()
            automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
        }
    }

    private fun awaitOption(): AccessibilityNodeInfo {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < deadline) {
            for (window in automation.windows) {
                findOption(window.root)?.let { return it }
            }
            SystemClock.sleep(50)
        }
        error("Dropdown option did not become visible")
    }

    private fun findOption(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.text?.toString() == "Make 1") return node
        for (index in 0 until node.childCount) {
            findOption(node.getChild(index))?.let { return it }
        }
        return null
    }
}
