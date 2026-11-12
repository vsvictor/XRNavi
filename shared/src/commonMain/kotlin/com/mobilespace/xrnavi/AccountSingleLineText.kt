package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer

@Composable
internal fun AccountSingleLineText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val measured = rememberTextMeasurer().measure(text, style, softWrap = false, maxLines = 1)
        val width = with(LocalDensity.current) { maxWidth.toPx() }
        val scale = (width / measured.size.width.coerceAtLeast(1)).coerceAtMost(1f)
        Text(
            text,
            style = style.copy(fontSize = style.fontSize * scale),
            maxLines = 1,
            softWrap = false,
        )
    }
}
