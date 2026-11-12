package com.mobilespace.xrnavi

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

internal data class HorizontalSwipe(
    val startX: Float,
    val deltaX: Float,
    val width: Float,
)

internal fun Modifier.horizontalSwipeGesture(
    onSwipe: (HorizontalSwipe) -> Unit,
): Modifier = pointerInput(onSwipe) {
    val minimumDistance = 56.dp.toPx()
    var startX = 0f
    var distance = 0f
    detectHorizontalDragGestures(
        onDragStart = {
            startX = it.x
            distance = 0f
        },
        onHorizontalDrag = { change, amount ->
            distance += amount
            change.consume()
        },
        onDragEnd = {
            if (abs(distance) >= minimumDistance) {
                onSwipe(HorizontalSwipe(startX, distance, size.width.toFloat()))
            }
        },
        onDragCancel = { distance = 0f },
    )
}
