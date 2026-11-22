package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.garage_mustang_gt_hero

@Composable
internal fun GarageLandscapeLayout(
    headerHeight: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            Modifier.weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SignInHero(
                modifier = Modifier.fillMaxWidth().height(116.dp)
                    .clip(RoundedCornerShape(12.dp)),
                dark = isDarkXrnaviTheme(),
                horizontalLayout = false,
                image = Res.drawable.garage_mustang_gt_hero,
                grayscale = true,
                showOverlayContent = false,
            )
            Text(
                "Ваш транспорт",
                fontFamily = rememberRobotoCondensedFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            Modifier.weight(1.6f).padding(top = headerHeight),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
