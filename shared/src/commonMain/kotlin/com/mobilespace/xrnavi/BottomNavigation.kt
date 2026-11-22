package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

@Composable
internal fun BottomNavigation(
    selectedSection: DemoSection,
    onSectionChange: (DemoSection) -> Unit,
    rail: Boolean = false,
    compactRail: Boolean = false,
    accountStyle: Boolean = false
) {
    val styledRail = rail && accountStyle
    val tabs = listOf(
        Triple("Мапа", Res.drawable.nav_map_light, Res.drawable.nav_map_dark),
        Triple("Гараж", Res.drawable.nav_garage_light, Res.drawable.nav_garage_dark),
        Triple("Налаштування", Res.drawable.nav_settings_light, Res.drawable.nav_settings_dark),
        Triple("Профіль", Res.drawable.nav_profile_light, Res.drawable.nav_profile_dark)
    )
    val items: @Composable (Modifier) -> Unit = { itemModifier ->
        tabs.forEachIndexed { index, tab ->
            val section = when (index) {
                0 -> DemoSection.Map
            1 -> DemoSection.Garage
            2 -> DemoSection.Settings
            3 -> DemoSection.Profile
            else -> DemoSection.Map
        }
            val active = section == selectedSection
            val tabModifier = itemModifier
                    .semantics {
                        selected = active
                        if (rail && compactRail) contentDescription = tab.first
                    }
                    .clickable(role = Role.Tab) { onSectionChange(section) }
                    .then(
                        if (styledRail && !compactRail && !active)
                            Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)
                        else Modifier
                    )
                    .padding(horizontal = 2.dp, vertical = 4.dp)
            val icon: @Composable () -> Unit = {
                FigmaIcon(
                    light = tab.second,
                    dark = tab.third,
                    modifier = Modifier.size(
                        if (styledRail && !compactRail) 24.dp
                        else if (styledRail) 21.dp else 19.dp
                    ),
                    tint = if (active && styledRail && !compactRail) MaterialTheme.colorScheme.onSurface
                    else if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val label: @Composable () -> Unit = {
                if (!rail || !compactRail) Text(
                    tab.first,
                    modifier = Modifier.padding(
                        top = 4.dp
                    ),
                    fontSize = if (styledRail && index == 2) 7.sp
                    else if (styledRail) 12.sp else if (rail) 9.sp else 11.sp,
                    lineHeight = if (styledRail && index == 2) 11.sp
                    else if (styledRail) 16.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
                    letterSpacing = if (styledRail) 0.4.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
                    color = if (active && styledRail) MaterialTheme.colorScheme.onSurface
                    else if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (styledRail) 1 else 2
                )
            }
            Column(
                tabModifier, horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { icon(); label() }
        }
    }
    if (rail) {
        Column(
            Modifier.width(if (compactRail) 56.dp else 88.dp)
                .fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant)
                .then(if (accountStyle && !compactRail) Modifier.padding(horizontal = 8.dp, vertical = 24.dp) else Modifier)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(if (accountStyle && !compactRail) 22.dp else 12.dp)
        ) { items(Modifier.fillMaxWidth().heightIn(min = if (accountStyle && !compactRail) 72.dp else 70.dp)) }
    } else Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(Modifier.weight(1f).fillMaxHeight())
    }
}
