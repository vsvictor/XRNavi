package com.mobilespace.xrnavi

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun SplashOrbit(
    color: Color,
    orbitDegrees: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.semantics { contentDescription = "Mobile Space 01 Orbit" }) {
        // Draw in the 96-unit logo coordinate space, directly at the display's pixel density.
        scale(size.width / 96f, size.height / 96f, pivot = Offset.Zero) {
            drawArc(
                color = color,
                startAngle = 12f,
                sweepAngle = 306.65f,
                useCenter = false,
                topLeft = Offset(8f, 8f),
                size = Size(80f, 80f),
                style = Stroke(width = 5f, cap = StrokeCap.Round),
            )
            val monogram = Path().apply {
                moveTo(27f, 64f)
                lineTo(27f, 34f)
                lineTo(48f, 53f)
                lineTo(69f, 34f)
                lineTo(69f, 64f)
            }
            drawPath(
                monogram,
                color,
                style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            val angle = (orbitDegrees - 41.35f) * PI / 180.0
            drawCircle(
                color,
                radius = 6f,
                center = Offset(48f + 40f * cos(angle).toFloat(), 48f + 40f * sin(angle).toFloat()),
            )
        }
    }
}
