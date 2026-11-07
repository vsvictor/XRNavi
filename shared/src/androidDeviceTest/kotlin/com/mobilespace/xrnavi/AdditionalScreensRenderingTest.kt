package com.mobilespace.xrnavi

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

class DesignCaptureActivity : ComponentActivity()

@RunWith(AndroidJUnit4::class)
class AdditionalScreensRenderingTest {
    @Test
    fun rendersEveryAdditionalScreenInEightDesignVariants() {
        captureDesigns(
            AdditionalScreen.entries.map { screen ->
                DesignCase("${screen.designId}-${screen.name}") {
                    AdditionalScreens(AdditionalScreensState(screen = screen), {})
                }
            },
            "design-captures"
        )
    }
}

internal data class DesignCase(val name: String, val content: @Composable () -> Unit)

internal fun captureDesigns(cases: List<DesignCase>, directory: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val context = instrumentation.context
    val output = File(context.getExternalFilesDir(null), directory).apply {
        assertTrue("Cannot create screenshot directory", isDirectory || mkdirs())
    }
    val scenario = ActivityScenario.launch<DesignCaptureActivity>(
        Intent(context, DesignCaptureActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    try {
        val sizes = listOf(
            "PhonePortrait" to IntSize(402, 874),
            "PhoneLandscape" to IntSize(874, 402),
            "TabletPortrait" to IntSize(834, 1194),
            "TabletLandscape" to IntSize(1120, 768)
        )
        for (design in cases) {
            for ((layout, size) in sizes) {
                for (dark in listOf(false, true)) {
                    val name = "${design.name}-$layout-${if (dark) "dark" else "light"}"
                    val captured = CountDownLatch(1)
                    val result = AtomicReference<Bitmap>()
                    scenario.onActivity { activity ->
                        activity.setContent {
                            CaptureDesign(name, size, dark, design.content) {
                                result.set(it)
                                captured.countDown()
                            }
                        }
                    }
                    assertTrue(
                        "$name: frame capture timed out",
                        captured.await(15, TimeUnit.SECONDS)
                    )
                    val bitmap = result.get()
                    assertEquals("$name: width", size.width, bitmap.width)
                    assertEquals("$name: height", size.height, bitmap.height)
                    assertEquals(
                        "$name: background must be opaque",
                        255,
                        bitmap.getPixel(0, 0) ushr 24
                    )
                    File(output, "$name.png").outputStream().use {
                        assertTrue(
                            "$name: PNG encoding failed",
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        )
                    }
                    bitmap.recycle()
                }
            }
        }
        assertEquals(cases.size * 8, output.listFiles { f -> f.extension == "png" }?.size)
    } finally {
        scenario.close()
    }
}

@Composable
private fun CaptureDesign(
    name: String,
    size: IntSize,
    dark: Boolean,
    content: @Composable () -> Unit,
    captured: (Bitmap) -> Unit
) {
    val actualWindow = LocalWindowInfo.current
    val designWindow = object : WindowInfo by actualWindow {
        override val containerSize: IntSize = size
    }
    val layer = rememberGraphicsLayer()
    CompositionLocalProvider(
        LocalDensity provides Density(1f),
        LocalWindowInfo provides designWindow
    ) {
        XrnaviTheme(darkTheme = dark) {
            Box(
                Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
                    .requiredSize(size.width.dp, size.height.dp)
                    .drawWithContent {
                        layer.record { this@drawWithContent.drawContent() }
                        drawLayer(layer)
                    }
                    .background(MaterialTheme.colorScheme.background)
            ) {
                key(name) { content() }
            }
            LaunchedEffect(name, size, dark) {
                repeat(3) { withFrameNanos { } }
                val hardware = layer.toImageBitmap().asAndroidBitmap()
                try {
                    captured(requireNotNull(hardware.copy(Bitmap.Config.ARGB_8888, false)))
                } finally {
                    hardware.recycle()
                }
            }
        }
    }
}
