package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

/** Navigation and "unavailable" callbacks handed to every additional screen. */
internal class AdditionalActions(
    val open: (AdditionalScreen) -> Unit,
    val back: () -> Unit,
    val section: (DemoSection) -> Unit,
    val unavailable: (String) -> Unit
)

@Composable
internal fun AdditionalNoticeDialog(notice: String?, onDismiss: () -> Unit) {
    notice?.let {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Демонстраційний режим") },
            text = { Text(it, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Зрозуміло") } }
        )
    }
}

@Composable
internal fun AdditionalColumns(
    layout: AccountLayout,
    primary: @Composable ColumnScope.() -> Unit,
    secondary: @Composable ColumnScope.() -> Unit
) {
    if (layout.landscape || layout.tablet) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = primary
            )
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = secondary
            )
        }
    } else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        primary()
        secondary()
    }
}

@Composable
internal fun AdditionalCard(
    selected: Boolean = false,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = when {
            destructive -> colors.error.copy(alpha = 0.14f)
            selected -> colors.surfaceVariant
            colors.background.red < 0.5f -> Color(0xFF080F19)
            else -> colors.surface
        },
        border = if (selected) BorderStroke(1.dp, colors.primary) else null
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
internal fun AdditionalTitle(text: String, large: Boolean = false) {
    Text(
        text,
        fontFamily = if (large) rememberRobotoCondensedFontFamily() else rememberInterFontFamily(),
        fontSize = if (large) 24.sp else 14.sp,
        lineHeight = if (large) 28.sp else 20.sp,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.semantics { heading() })
}

@Composable
internal fun AdditionalHeading(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() })
}

@Composable
internal fun AdditionalBody(text: String) {
    Text(
        text, fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
internal fun AdditionalBadge(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
        Text(
            text, Modifier.padding(horizontal = 8.dp, vertical = 5.dp), fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun AdditionalNotice(title: String, body: String, warning: Boolean = false) {
    Surface(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
        color = if (warning) accountPolicyColor().copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccountIcon(
                if (warning) AccountIcon.Lock else AccountIcon.Help,
                Modifier.size(20.dp), tint = if (warning) accountPolicyColor() else null
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (warning) accountPolicyColor() else MaterialTheme.colorScheme.onSurface
                )
                AdditionalBody(body)
            }
        }
    }
}

@Composable
internal fun AdditionalPolicy(title: String, detail: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            AdditionalBody(detail)
        }
        AccountIcon(AccountIcon.Lock, Modifier.size(18.dp), contentDescription = "Лише для читання")
    }
}

@Composable
internal fun AdditionalAction(
    title: String, detail: String, destructive: Boolean = false, onClick: () -> Unit
) {
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                AdditionalBody(detail)
            }
            AccountIcon(AccountIcon.ChevronRight, Modifier.size(18.dp))
        }
    }
}

@Composable
internal fun AdditionalButton(
    title: String, secondary: Boolean = false, destructive: Boolean = false,
    enabled: Boolean = true, onClick: () -> Unit
) {
    Button(
        onClick, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                destructive -> MaterialTheme.colorScheme.error
                secondary -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primary
            },
            contentColor = when {
                destructive -> MaterialTheme.colorScheme.onError
                secondary -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onPrimary
            }
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) { Text(title, fontSize = 14.sp) }
}

@Composable
internal fun AdditionalChoices(values: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { value ->
            FilterChip(
                selected == value, onClick = { onSelect(value) }, label = { Text(value) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
internal fun AdditionalSwitch(
    title: String,
    detail: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp)
            AdditionalBody(detail)
        }
        Switch(checked, onChange, modifier = Modifier.semantics { contentDescription = title })
    }
}

@Composable
internal fun AdditionalConsent(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().toggleable(checked, role = Role.Checkbox, onValueChange = onChange)
            .semantics { contentDescription = title },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked, onCheckedChange = null)
        Text(title, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun AdditionalMetrics(layout: AccountLayout, values: List<Pair<String, String>>) {
    if (layout == AccountLayout.PhonePortrait && values.size > 2) {
        values.chunked(2).forEach { AdditionalMetrics(layout, it) }
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { (label, value) ->
            Surface(
                Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdditionalBody(label)
                    Text(
                        value, fontFamily = rememberRobotoCondensedFontFamily(),
                        fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun AdditionalVehicleImage() {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    AdditionalCard {
        Image(
            painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
            contentDescription = "Ілюстративне зображення Ford Mustang",
            modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop
        )
        AdditionalBody("Ілюстративне зображення · не підтверджує комплектацію")
    }
}

@Composable
internal fun AdditionalRouteIllustration(
    caption: String = "Київ → Львів · схема з макета, не навігаційна мапа"
) {
    Surface(
        Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            MapCanvas(Modifier.fillMaxWidth().height(180.dp), preview = true, compact = true)
            Box(Modifier.padding(12.dp)) { AdditionalBody(caption) }
        }
    }
}
