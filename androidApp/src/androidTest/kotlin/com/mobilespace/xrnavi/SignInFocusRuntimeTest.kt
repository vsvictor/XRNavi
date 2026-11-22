package com.mobilespace.xrnavi

import android.os.SystemClock
import android.view.WindowInsets
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInFocusRuntimeTest {
    @Test
    fun passwordRetainsFocusAfterKeyboardOpens() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            fun passwordField(): AccessibilityNodeInfo? {
                fun find(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
                    if (node.className == "android.widget.EditText") {
                        for (index in 0 until node.childCount) {
                            if (node.getChild(index)?.contentDescription?.toString() == "Пароль") {
                                return node
                            }
                        }
                    }
                    for (index in 0 until node.childCount) {
                        node.getChild(index)?.let { find(it)?.let { field -> return field } }
                    }
                    return null
                }
                return automation.rootInActiveWindow?.let(::find)
            }

            fun awaitCondition(description: String, condition: () -> Boolean) {
                val deadline = SystemClock.uptimeMillis() + 10_000
                while (SystemClock.uptimeMillis() < deadline) {
                    if (condition()) return
                    SystemClock.sleep(100)
                }
                assertTrue(description, condition())
            }

            val fieldDeadline = SystemClock.uptimeMillis() + 10_000
            while (SystemClock.uptimeMillis() < fieldDeadline && passwordField() == null) {
                SystemClock.sleep(100)
            }
            assumeTrue(
                "Password focus test requires a signed-out app session",
                passwordField() != null
            )
            assertTrue(passwordField()!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            awaitCondition("Keyboard must open") {
                var visible = false
                scenario.onActivity {
                    visible = it.window.decorView.rootWindowInsets
                        ?.isVisible(WindowInsets.Type.ime()) == true
                }
                visible
            }
            SystemClock.sleep(1500)
            repeat(5) {
                assertTrue("Password must retain input focus", passwordField()?.isFocused == true)
                SystemClock.sleep(200)
            }
        } finally {
            scenario.onActivity {
                it.window.insetsController?.hide(WindowInsets.Type.ime())
            }
            scenario.close()
        }
    }
}
