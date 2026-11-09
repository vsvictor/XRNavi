package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Однорядковий заголовок екрана авторизації; `title == null` — слоган. */
@Composable
internal fun HeroTitle(
    title: String?,
    horizontalLayout: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val titleSize = rememberHeroTitleSize(maxWidth, horizontalLayout)
        var shrink by remember(titleSize, maxWidth, title) { mutableStateOf(1f) }
        val fontSize = titleSize * shrink
        Text(
            text = title?.uppercase() ?: heroSlogan,
            fontFamily = rememberRobotoCondensedFontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            lineHeight = fontSize * 1.05f,
            softWrap = false,
            maxLines = 1,
            onTextLayout = {
                val needed = it.multiParagraph.maxIntrinsicWidth
                if (it.hasVisualOverflow && needed > 0f && shrink > 0.3f) {
                    shrink *= (it.size.width / needed * 0.97f).coerceIn(0.5f, 0.97f)
                }
            },
            letterSpacing = (-0.45).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = if (title != null) Modifier.semantics { heading() } else Modifier
        )
    }
}
