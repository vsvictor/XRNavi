package com.mobilespace.xrnavi

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
internal fun ProfileVehicleIllustration(modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier.clipToBounds()) {
        val unit = size.height / 100f
        val cx = size.width * 0.72f
        val cy = size.height * 0.53f
        val line = 1.5f * unit
        drawCircle(accent.copy(alpha = 0.05f), 64 * unit, Offset(cx, cy))
        drawCircle(accent.copy(alpha = 0.10f), 46 * unit, Offset(cx, cy), style = Stroke(unit))
        drawLine(
            accent.copy(alpha = 0.14f),
            Offset(0f, 85 * unit),
            Offset(size.width, 85 * unit),
            unit
        )
        val road = Path().apply {
            moveTo(0f, 68 * unit)
            cubicTo(
                size.width * 0.3f,
                68 * unit,
                size.width * 0.28f,
                25 * unit,
                size.width * 0.46f,
                25 * unit
            )
            lineTo(size.width * 0.52f, 25 * unit)
        }
        drawPath(road, accent.copy(alpha = 0.20f), style = Stroke(2 * unit, cap = StrokeCap.Round))
        drawCircle(accent, 3 * unit, Offset(size.width * 0.15f, 64 * unit))
        val car = Path().apply {
            moveTo(cx - 29 * unit, cy + 10 * unit)
            lineTo(cx - 22 * unit, cy - 14 * unit)
            quadraticTo(cx - 21 * unit, cy - 18 * unit, cx - 16 * unit, cy - 18 * unit)
            lineTo(cx + 16 * unit, cy - 18 * unit)
            quadraticTo(cx + 21 * unit, cy - 18 * unit, cx + 22 * unit, cy - 14 * unit)
            lineTo(cx + 29 * unit, cy + 10 * unit)
            close()
        }
        drawPath(car, accent.copy(alpha = 0.08f))
        drawPath(car, accent, style = Stroke(line))
        drawRoundRect(
            accent, Offset(cx - 32 * unit, cy + 3 * unit), Size(64 * unit, 27 * unit),
            CornerRadius(7 * unit), style = Stroke(line),
        )
        for (side in listOf(-1, 1)) {
            drawLine(
                accent, Offset(cx + side * 22 * unit, cy + 11 * unit),
                Offset(cx + side * 13 * unit, cy + 11 * unit), 3 * unit, StrokeCap.Round,
            )
            drawLine(
                accent, Offset(cx + side * 23 * unit, cy + 30 * unit),
                Offset(cx + side * 23 * unit, cy + 37 * unit), 5 * unit, StrokeCap.Round,
            )
        }
        drawLine(
            accent.copy(alpha = 0.5f),
            Offset(cx - 7 * unit, cy + 21 * unit),
            Offset(cx + 7 * unit, cy + 21 * unit),
            line
        )
    }
}
