package com.mobilespace.xrnavi

import android.content.Intent
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import android.view.Choreographer
import android.graphics.Rect
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.lifecycle.viewmodel.compose.viewModel
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdditionalScreensInteractionTest {
    @Test
    fun catalogPrivacyAndInvitationControlsDispatchBlocEvents() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val originalAccessibilityFlags = instrumentation.uiAutomation.serviceInfo.flags
        instrumentation.uiAutomation.serviceInfo =
            instrumentation.uiAutomation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
        val context = instrumentation.context
        val bloc = AdditionalNavigationBloc()
        lateinit var deletion: DeleteAccountBloc
        lateinit var invitation: InvitationBloc
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(
                context,
                DesignCaptureActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    val state by bloc.state.collectAsState()
                    deletion = viewModel { DeleteAccountViewModel() }.bloc
                    invitation = viewModel { InvitationViewModel() }.bloc
                    XrnaviTheme(darkTheme = false) { AdditionalScreens(state, bloc::dispatch) }
                }
            }
            fun open(screen: AdditionalScreen) {
                scenario.onActivity { bloc.dispatch(AdditionalNavigationEvent.Open(screen)) }
                settleFrames()
            }
            open(AdditionalScreen.VehicleCatalog)
            click("Ford")
            assertEquals(AdditionalScreen.VehicleModel, bloc.state.value.screen)
            click("Далі · перевірити параметри")
            assertEquals(AdditionalScreen.VehicleReview, bloc.state.value.screen)
            click("До каталогу")
            assertEquals(AdditionalScreen.VehicleCatalog, bloc.state.value.screen)
            assertTrue(bloc.state.value.backStack.isEmpty())

            open(AdditionalScreen.Privacy)
            click("Видалення аккаунта")
            assertEquals(AdditionalScreen.DeleteAccount, bloc.state.value.screen)
            val deletionButton =
                findNode { it.text?.toString() == "Підтвердити запит на видалення" }
            assertTrue(generateSequence(deletionButton) { it.parent }.any { !it.isEnabled })
            click("Розумію наслідки та окреме зберігання робочих даних.")
            assertTrue(deletion.state.value.consent)
            click("Підтвердити запит на видалення")
            assertTrue(deletion.state.value.notice.orEmpty().contains("не видалено"))
            click("Зрозуміло")
            click("Скасувати · залишити аккаунт")
            assertEquals(AdditionalScreen.Privacy, bloc.state.value.screen)
            assertFalse(deletion.state.value.consent)

            open(AdditionalScreen.Invitation)
            val code = findNode { it.isEditable }
            assertTrue(
                code.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        "EXAMPLE"
                    )
                }
            ))
            settleFrames()
            assertEquals("EXAMPLE", invitation.state.value.code)
            click("Ознайомився з правилами організації та умовами передачі даних")
            assertTrue(invitation.state.value.consent)
            click("Прийняти запрошення")
            assertTrue(
                invitation.state.value.notice.orEmpty().contains("Членство в організації не змінено")
            )
        } finally {
            scenario.close()
            instrumentation.uiAutomation.serviceInfo =
                instrumentation.uiAutomation.serviceInfo.apply {
                    flags = originalAccessibilityFlags
                }
        }
    }

    private fun click(label: String) {
        var node =
            findNode { it.text?.toString() == label || it.contentDescription?.toString() == label }
        while (!node.isClickable && node.parent != null) node = node.parent
        assertTrue(
            "Cannot click $label",
            node.isEnabled && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        )
        settleFrames()
    }

    private fun settleFrames() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val ready = CountDownLatch(1)
        fun nextFrame(remaining: Int) {
            Choreographer.getInstance().postFrameCallback {
                if (remaining == 1) ready.countDown() else nextFrame(remaining - 1)
            }
        }
        instrumentation.runOnMainSync { nextFrame(3) }
        assertTrue("UI frames did not settle", ready.await(5, TimeUnit.SECONDS))
        instrumentation.waitForIdleSync()
    }

    private fun findNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        repeat(30) {
            instrumentation.uiAutomation.waitForIdle(100, 5000)
            if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
            val root = instrumentation.uiAutomation.rootInActiveWindow
            if (root == null) {
                Thread.sleep(100)
                return@repeat
            }
            assertEquals(instrumentation.context.packageName, root.packageName.toString())
            search(root, predicate)?.let { return it }
            val scroll = search(root) { it.isScrollable }
            if (scroll == null) {
                throw AssertionError("Control not found in the active test window:\n${describe(root)}")
            }
            val bounds = Rect().also(scroll::getBoundsInScreen)
            val downTime = SystemClock.uptimeMillis()
            val x = bounds.exactCenterX()
            val from = bounds.top + bounds.height() * 0.8f
            val to = bounds.top + bounds.height() * 0.2f
            fun pointer(action: Int, y: Float) {
                val event =
                    MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
                event.source = InputDevice.SOURCE_TOUCHSCREEN
                assertTrue(
                    "Swipe event was rejected",
                    instrumentation.uiAutomation.injectInputEvent(event, true)
                )
                event.recycle()
            }
            pointer(MotionEvent.ACTION_DOWN, from)
            repeat(10) { step ->
                SystemClock.sleep(30)
                pointer(MotionEvent.ACTION_MOVE, from + (to - from) * (step + 1) / 10f)
            }
            pointer(MotionEvent.ACTION_UP, to)
            settleFrames()
        }
        throw AssertionError(
            "Control not found after scrolling:\n${
                instrumentation.uiAutomation.rootInActiveWindow?.let(
                    ::describe
                )
            }"
        )
    }

    private fun search(
        node: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (predicate(node)) return node
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child -> search(child, predicate)?.let { return it } }
        }
        return null
    }

    private fun describe(node: AccessibilityNodeInfo): String = buildString {
        appendLine("${node.className}: ${node.text} / ${node.contentDescription}; scroll=${node.isScrollable}; actions=${node.actionList}")
        for (index in 0 until node.childCount) node.getChild(index)?.let { append(describe(it)) }
    }
}
