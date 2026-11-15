package com.mobilespace.xrnavi

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.JsonPrimitive

internal fun VehicleConfigurationOption.swatchColor(): Color? {
    val rgb = (rawData["rgb"] as? JsonPrimitive)?.content?.trim() ?: return null
    val hex = rgb.removePrefix("#")
    if (hex.length == 6 && hex.all { it in "0123456789abcdefABCDEF" }) {
        return Color(0xFF000000 or hex.toLong(16))
    }
    val parts = rgb.split(",").map { it.trim().toIntOrNull()?.takeIf { it in 0..255 } ?: return null }
    if (parts.size != 3) return null
    return Color(red = parts[0], green = parts[1], blue = parts[2])
}
