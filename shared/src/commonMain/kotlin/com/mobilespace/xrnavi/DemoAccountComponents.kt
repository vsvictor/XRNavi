package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Building blocks shared by the demo account screens (garage, profile, vehicle, settings). */
@Composable
internal fun AdditionalAccountActions(
    destinations: List<AdditionalScreen>,
    onOpenAdditional: ((AdditionalScreen) -> Unit)?
) {
    if (onOpenAdditional == null) return
    destinations.forEach { destination ->
        AccountAction(destination.title, "Відкрити · локальний приклад") {
            onOpenAdditional(destination)
        }
    }
}

@Composable
internal fun ResponsiveSections(
    columns: Boolean,
    firstWeight: Float = 0.5f,
    gap: Int = 16,
    first: @Composable ColumnScope.() -> Unit,
    second: @Composable ColumnScope.() -> Unit
) {
    if (columns) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap.dp)) {
            Column(
                Modifier.weight(firstWeight).testTag("account-sections-first"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = first
            )
            Column(
                Modifier.weight(1f - firstWeight).testTag("account-sections-second"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = second
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            first()
            second()
        }
    }
}

@Composable
internal fun AccountAction(title: String, status: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(10.dp), color = accountTonalColor()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp)
                Text(
                    status, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AccountIcon(AccountIcon.ChevronRight, Modifier.size(18.dp))
        }
    }
}

@Composable
internal fun AccountCard(
    tonal: Boolean = false,
    minHeight: Int = 0,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (tonal) accountTonalColor() else accountSurfaceColor()
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = minHeight.dp).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
internal fun SectionTitle(text: String, fontSize: Int = 24, lineHeight: Int = 30) {
    Text(
        text, fontFamily = rememberRobotoCondensedFontFamily(), fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp, lineHeight = lineHeight.sp,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
internal fun CardHeading(text: String) {
    Text(
        text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
        lineHeight = 20.sp, letterSpacing = 0.1.sp,
        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() }
    )
}

@Composable
internal fun AccountBody(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
internal fun accountSurfaceColor(): Color =
    if (MaterialTheme.colorScheme.background.red < 0.5f) Color(0xFF070D17)
    else MaterialTheme.colorScheme.surface

@Composable
internal fun accountTonalColor(): Color =
    if (MaterialTheme.colorScheme.background.red < 0.5f) accountSurfaceColor()
    else MaterialTheme.colorScheme.surfaceVariant
