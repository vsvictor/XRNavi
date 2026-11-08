package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

@Composable
internal fun ProductWordmark(modifier: Modifier = Modifier) {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FigmaIcon(
            light = Res.drawable.orbit_glyph_light,
            dark = Res.drawable.orbit_glyph_dark,
            modifier = Modifier.size(30.dp),
            contentDescription = "Знак Mobile Space 01 Orbit"
        )
        Text(
            text = "XRNavi",
            fontFamily = rememberSpaceGroteskFontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = (-0.35).sp,
            color = if (dark) Color(0xFFF2F5FA) else Color(0xFF172033)
        )
    }
}
