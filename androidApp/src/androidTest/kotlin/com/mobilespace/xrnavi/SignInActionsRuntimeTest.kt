package com.mobilespace.xrnavi

import android.os.Bundle
import android.os.SystemClock
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
class SignInActionsRuntimeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation

    @Test
    fun signInSubmitsInvalidTestCredentialsAndShowsFailureInsteadOfPlaceholder() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            await("Email input must be available") { field("Електронна пошта") != null }
            setText(field("Електронна пошта")!!, "xrnavi-login-check@example.invalid")
            setText(field("Пароль")!!, "Not-a-real-account-password")
            await("Sign-in button must be available") { node("Увійти") != null }
            assertTrue(node("Увійти")!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            find { it.className == "android.widget.ScrollView" }
                ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            await("RPC failure or timeout must be displayed, not the old placeholder", 25_000) {
                find {
                    val text = it.text?.toString().orEmpty()
                    text.startsWith("Не вдалося увійти.") ||
                        text.startsWith("Сервер не відповів вчасно.") ||
                        text.startsWith("Сервер повернув некоректну відповідь")
                } != null
            }
            assertTrue(find {
                it.text?.contains("Авторизацію не підключено") == true
            } == null)
        } finally {
            scenario.onActivity { it.window.insetsController?.hide(android.view.WindowInsets.Type.ime()) }
            scenario.close()
        }
    }

    @Test
    fun eyeRevealsAndHidesSamePasswordAndEnvelopeOpensSystemChooser() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        var chooserOpened = false
        try {
            await("Password input must be available") { field("Пароль") != null }
            val password = "Xrnavi-visibility-check"
            setText(field("Пароль")!!, password)
            assertTrue(field("Пароль")!!.isPassword)
            await("Eye button must be accessible") { node("Показати пароль") != null }
            assertTrue(node("Показати пароль")!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            await("Eye must switch to the hide-password action") { node("Приховати пароль") != null }
            await("Password text must become visible") {
                field("Пароль")?.text?.toString() == password
            }
            assertEquals(password, field("Пароль")!!.text.toString())
            assertTrue(node("Приховати пароль")!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            await("Eye must switch back to the show-password action") { node("Показати пароль") != null }
            await("Password must become masked again") {
                field("Пароль")?.text?.toString() != password
            }
            assertFalse(field("Пароль")!!.text.toString() == password)
            assertTrue(node("Обрати поштовий аккаунт")!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            chooserOpened = true
            await("Envelope must open the native account chooser") {
                val packageName = automation.rootInActiveWindow?.packageName?.toString()
                packageName == "android" || packageName == "com.google.android.gms"
            }
        } finally {
            if (chooserOpened) {
                automation.executeShellCommand("input keyevent KEYCODE_BACK").close()
                await("Must return to XRNavi after dismissing chooser") {
                    automation.rootInActiveWindow?.packageName?.toString() == "com.mobilespace.xrnavi"
                }
            }
            scenario.onActivity { it.window.insetsController?.hide(android.view.WindowInsets.Type.ime()) }
            scenario.close()
        }
    }

    private fun field(label: String): AccessibilityNodeInfo? = find { node ->
        node.className == "android.widget.EditText" &&
            (node.contentDescription?.toString() == label ||
                (0 until node.childCount).any {
                    node.getChild(it)?.contentDescription?.toString() == label
                })
    }

    private fun node(description: String): AccessibilityNodeInfo? =
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
