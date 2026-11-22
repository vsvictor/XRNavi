package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

@Composable
internal fun ProviderButton(
    modifier: Modifier,
    label: String,
    icon: DrawableResource,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    iconTint: Color? = null
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaIcon(light = icon, dark = icon, modifier = Modifier.size(18.dp), tint = iconTint)
        Text(
            text = "  $label",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}
