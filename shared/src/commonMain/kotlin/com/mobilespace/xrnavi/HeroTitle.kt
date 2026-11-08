package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        Text(
            text = title?.uppercase() ?: heroSlogan,
            fontFamily = rememberRobotoCondensedFontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = titleSize,
            lineHeight = titleSize * 1.05f,
            softWrap = false,
            maxLines = 1,
            letterSpacing = (-0.45).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = if (title != null) Modifier.semantics { heading() } else Modifier
        )
    }
}
