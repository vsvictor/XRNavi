package com.mobilespace.xrnavi

import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VehiclePhotoHeroRenderingTest {
    @Test
    fun heroRetainsBackTitleAttributionAndSelectedPhoto() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val photo = VehiclePhoto(
            url = "https://example.invalid/car.jpg",
            sourceUrl = "https://example.invalid/source",
            author = "Author",
            license = "CC BY 4.0",
            licenseUrl = "https://example.invalid/license",
            title = "Vehicle",
        )
        val state = VehiclePhotosState(
            photos = listOf(photo),
            page = 1,
            failedImages = setOf(photo.url),
        )
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        try {
            for (title in listOf("Додати авто", "Профіль транспорту")) {
                val back = AtomicBoolean(false)
                val page = AtomicInteger(-1)
                scenario.onActivity { activity ->
                    activity.setContent {
                        XrnaviTheme {
                            Column {
                                VehiclePhotoHero(title, { back.set(true) }, state) {
                                    if (it is VehiclePhotosEvent.PageChanged) page.set(it.page)
                                }
                            }
                        }
                    }
                }
                val deadline = SystemClock.uptimeMillis() + 5_000
                var root: AccessibilityNodeInfo? = null
                do {
                    instrumentation.waitForIdleSync()
                    root = instrumentation.uiAutomation.rootInActiveWindow
                    if (root != null && find(root, title.uppercase()) != null &&
                        find(root, "CC BY 4.0") != null && page.get() == 1
                    ) break
                    SystemClock.sleep(50)
                } while (SystemClock.uptimeMillis() < deadline)
                val window = requireNotNull(root)
                assertNotNull(find(window, title.uppercase()))
                assertNotNull(find(window, "Vehicle · Author · Wikimedia Commons"))
                assertNotNull(find(window, "CC BY 4.0"))
                assertTrue(page.get() == 1)
                var button = requireNotNull(find(window, "Назад"))
                while (!button.isClickable) button = requireNotNull(button.parent)
                assertTrue(button.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                instrumentation.waitForIdleSync()
                assertTrue(back.get())
            }
        } finally {
            scenario.close()
        }
    }

    private fun find(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString() == text || node.contentDescription?.toString() == text) return node
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { find(it, text) }?.let { return it }
        }
        return null
    }
}
