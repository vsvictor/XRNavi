package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

internal enum class GarageVehicleAction(val path: String) {
    Edit("M4 20L5 15L16 4L20 8L9 19ZM14 6L18 10"),
    Delete("M4 7H20M9 4H15M6 7L7 20H17L18 7M10 10V17M14 10V17"),
}

@Composable
internal fun GarageVehicleIconButton(
    action: GarageVehicleAction,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = remember(action) {
        ImageVector.Builder(action.name, 24.dp, 24.dp, 24f, 24f).addPath(
            pathData = PathParser().parsePathString(action.path).toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ).build()
    }
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color(0xFF335CFF),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF335CFF).copy(alpha = 0.55f),
            disabledContentColor = Color.White,
        ),
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(24.dp))
    }
}
