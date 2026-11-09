package com.mobilespace.xrnavi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp

/**
 * Як на iOS: при зміні розміру вікна (поворот) вміст не стрибає, а плавно
 * «перетікає» зі старого розміру в новий, перебудовуючи розкладку на кожному кадрі.
 */
@Composable
internal fun OrientationMorph(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val target = IntSize(constraints.maxWidth, constraints.maxHeight)
        var from by remember { mutableStateOf(target) }
        var to by remember { mutableStateOf(target) }
        val progress = remember { Animatable(1f) }
        LaunchedEffect(target) {
            if (target == to) return@LaunchedEffect
            val p = progress.value
            from = IntSize(
                lerp(from.width, to.width, p),
                lerp(from.height, to.height, p)
            )
            to = target
            progress.snapTo(0f)
            progress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        Layout(content = content, modifier = Modifier.fillMaxSize().clipToBounds()) { measurables, c ->
            val p = progress.value
            val w = lerp(from.width, to.width, p).coerceAtLeast(1)
            val h = lerp(from.height, to.height, p).coerceAtLeast(1)
            val placeables = measurables.map { it.measure(Constraints.fixed(w, h)) }
            layout(c.maxWidth, c.maxHeight) {
                placeables.forEach { it.place((c.maxWidth - w) / 2, (c.maxHeight - h) / 2) }
            }
        }
    }
}
