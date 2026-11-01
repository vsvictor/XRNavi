package com.mobilespace.xrnavi

import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthGateRuntimeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation

    @Test
    fun signInCannotCreateSessionOrOpenMapWithoutAuthProvider() {
        val context = instrumentation.targetContext
        val scenario = ActivityScenario.launch<MainActivity>(
            Intent(context, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        try {
            instrumentation.waitForIdleSync()
            assertEquals(
                "Unlock the device and bring XRNavi to the foreground before this test",
                context.packageName,
                automation.rootInActiveWindow?.packageName?.toString()
            )
            assertTrue("Sign-in action must be visible", hasExactText("Увійти"))
            assertFalse("Guest access must not bypass authentication", hasExactText("Продовжити без входу"))
            assertFalse("The map must remain inaccessible", hasExactText("Мапа недоступна"))

            assertTrue("Sign-in action must be clickable", clickExactText("Увійти"))
            instrumentation.waitForIdleSync()

            assertTrue("Unavailable authentication must be disclosed", hasExactText("Авторизація недоступна"))
            assertFalse("Submitting cannot open the map without a configured session", hasExactText("Мапа недоступна"))
        } finally {
            scenario.close()
        }
    }

    private fun hasExactText(value: String): Boolean = textNodes(value).any {
        it.text?.toString()?.trim() == value
    }

    private fun clickExactText(value: String): Boolean {
        val textNode = textNodes(value).firstOrNull {
            it.text?.toString()?.trim() == value
        } ?: return false
        var target: AccessibilityNodeInfo? = textNode
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        return target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
    }

    private fun textNodes(value: String): List<AccessibilityNodeInfo> =
        automation.rootInActiveWindow
            ?.findAccessibilityNodeInfosByText(value)
            .orEmpty()
}
