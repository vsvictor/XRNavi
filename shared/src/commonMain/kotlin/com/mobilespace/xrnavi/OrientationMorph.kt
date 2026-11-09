package com.mobilespace.xrnavi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

private class MorphState {
    var recording = 0
    var lastSize = IntSize.Zero
    var frozen by mutableStateOf<GraphicsLayer?>(null)
    var frozenSize by mutableStateOf(IntSize.Zero)
    var fresh by mutableStateOf(false)
    var tick by mutableIntStateOf(0)
}

/**
 * Як на iOS: при зміні розміру вікна (поворот) нова розкладка будується один раз,
 * а знімок попереднього кадру плавно розчиняється поверх неї. Жодного перемальовування
 * розкладки на кожному кадрі анімації.
 */
@Composable
internal fun OrientationMorph(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val layers = arrayOf(rememberGraphicsLayer(), rememberGraphicsLayer())
    val state = remember { MorphState() }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(state.tick) {
        if (state.tick == 0) return@LaunchedEffect
        progress.snapTo(0f)
        state.fresh = false
        progress.animateTo(1f, tween(525, easing = FastOutSlowInEasing))
    }
    Box(modifier.fillMaxSize()) {
        Layout(
            content = content,
            modifier = Modifier.fillMaxSize().drawWithContent {
                val layer = layers[state.recording]
                layer.record(size = IntSize(size.width.toInt(), size.height.toInt())) {
                    this@drawWithContent.drawContent()
                }
                drawLayer(layer)
            }
        ) { measurables, c ->
            val size = IntSize(c.maxWidth, c.maxHeight)
            if (state.lastSize != IntSize.Zero && size != state.lastSize) {
                state.frozen = layers[state.recording]
                state.frozenSize = state.lastSize
                state.recording = 1 - state.recording
                state.fresh = true
                state.tick++
            }
            state.lastSize = size
            val placeables = measurables.map { it.measure(Constraints.fixed(size.width, size.height)) }
            layout(size.width, size.height) { placeables.forEach { it.place(0, 0) } }
        }
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (state.fresh) 1f else 1f - progress.value }
                .drawBehind {
                    val old = state.frozen
                    val oldSize = state.frozenSize
                    if (old != null && oldSize.width > 0 && oldSize.height > 0) {
                        val scale = min(size.width / oldSize.width, size.height / oldSize.height)
                        withTransform({
                            translate(
                                (size.width - oldSize.width * scale) / 2f,
                                (size.height - oldSize.height * scale) / 2f
                            )
                            scale(scale, scale, Offset.Zero)
                        }) { drawLayer(old) }
                    }
                }
        )
    }
}
