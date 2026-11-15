package com.mobilespace.xrnavi

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.garage_mustang_gt_hero

@RunWith(AndroidJUnit4::class)
class GarageLoadingRenderingTest {
    @Test
    fun heroIsGrayscaleAndLoaderIsCenteredInScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val originalFlags = automation.serviceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        val frame = Rect()
        var density = 1f
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        density = LocalDensity.current.density
                        Box(Modifier.fillMaxSize().onGloballyPositioned {
                            val bounds = it.boundsInWindow()
                            frame.set(
                                bounds.left.toInt(),
                                bounds.top.toInt(),
                                bounds.right.toInt(),
                                bounds.bottom.toInt()
                            )
                        }) {
                            SignInHero(
                                modifier = Modifier.fillMaxWidth().height(196.dp),
                                dark = isDarkXrnaviTheme(), horizontalLayout = false,
                                image = Res.drawable.garage_mustang_gt_hero, grayscale = true,
                                showOverlayContent = false,
                            )
                            GarageLoadingOverlay()
                        }
                    }
                }
            }
            val deadline = SystemClock.uptimeMillis() + 5_000
            var progress: AccessibilityNodeInfo? = null
            while (progress == null && SystemClock.uptimeMillis() < deadline) {
                if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
                progress = automation.windows.firstNotNullOfOrNull { findProgress(it.root) }
                SystemClock.sleep(100)
            }
            val bounds = Rect().also { requireNotNull(progress).getBoundsInScreen(it) }
            assertTrue(
                "Loader horizontal center",
                kotlin.math.abs(bounds.centerX() - frame.centerX()) <= 2
            )
            assertTrue(
                "Loader vertical center",
                kotlin.math.abs(bounds.centerY() - frame.centerY()) <= 2
            )
            val image = requireNotNull(automation.takeScreenshot())
            try {
                var samples = 0
                for (x in 2..8) for (y in 60..120 step 10) {
                    val pixel = image.getPixel(
                        frame.left + frame.width() * x / 10,
                        frame.top + (y * density).toInt()
                    )
                    val red = android.graphics.Color.red(pixel)
                    val green = android.graphics.Color.green(pixel)
                    val blue = android.graphics.Color.blue(pixel)
                    assertTrue(
                        "Hero must be grayscale",
                        kotlin.math.abs(red - green) <= 2 && kotlin.math.abs(green - blue) <= 2
                    )
                    samples++
                }
                assertTrue(samples >= 40)
            } finally {
                image.recycle()
            }
        } finally {
            scenario.close()
            automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
        }
    }

    private fun findProgress(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.className?.toString() == "android.widget.ProgressBar") return node
        for (i in 0 until node.childCount) findProgress(node.getChild(i))?.let { return it }
        return null
    }
}
