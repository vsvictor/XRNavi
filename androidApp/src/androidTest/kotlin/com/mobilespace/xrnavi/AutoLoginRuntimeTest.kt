package com.mobilespace.xrnavi

import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoLoginRuntimeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation

    @Test
    fun storedSessionSignsInAutomaticallyAfterActivityRecreation() {
        val arguments = InstrumentationRegistry.getArguments()
        val email = arguments.getString("xrnaviEmail")
        val password = arguments.getString("xrnaviPassword")
        assumeTrue("Pass xrnaviEmail and xrnaviPassword instrumentation arguments", email != null && password != null)

        val first = ActivityScenario.launch(MainActivity::class.java)
        try {
            await("Email input or map must appear", 15_000) { field("Електронна пошта") != null || hasMap() }
            field("Електронна пошта")?.let {
                setText(it, email!!)
                setText(field("Пароль")!!, password!!)
                await("Sign-in button") { clickable("Увійти") != null }
                assertTrue(clickable("Увійти")!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            }
            await("Login must reach the map", 30_000) { hasMap() }
        } finally {
            first.close()
        }

        val second = ActivityScenario.launch(MainActivity::class.java)
        try {
            await("Stored session must open the map without the sign-in form", 20_000) { hasMap() }
            assertTrue(field("Електронна пошта") == null)
        } finally {
            second.close()
        }
    }

    private fun hasMap() = find {
        (it.contentDescription?.toString() ?: it.text?.toString()).orEmpty().contains("Центр мапи")
    } != null

    private fun field(label: String): AccessibilityNodeInfo? = find { node ->
        node.className == "android.widget.EditText" &&
            (node.contentDescription?.toString() == label ||
                (0 until node.childCount).any {
                    node.getChild(it)?.contentDescription?.toString() == label
                })
    }

    private fun clickable(description: String): AccessibilityNodeInfo? =
        find { it.contentDescription?.toString() == description && it.isClickable }

    private fun setText(field: AccessibilityNodeInfo, text: String) {
        assertTrue(field.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            },
        ))
    }

    private fun find(matches: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        fun visit(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            if (matches(node)) return node
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let { visit(it)?.let { found -> return found } }
            }
            return null
        }
        return automation.rootInActiveWindow?.let(::visit)
    }

    private fun await(description: String, timeoutMillis: Long = 10_000, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMillis
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition()) return
            SystemClock.sleep(100)
        }
        assertTrue(description, condition())
    }
}
