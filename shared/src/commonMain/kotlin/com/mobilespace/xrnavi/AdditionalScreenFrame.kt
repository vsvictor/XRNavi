package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val screensWithoutBottomNavigation = setOf(
    AdditionalScreen.Invitation, AdditionalScreen.DeleteAccount,
    AdditionalScreen.LocationEducation, AdditionalScreen.RouteUnverified
)

/** Shared header, navigation and scroll container for the non-map additional screens. */
@Composable
internal fun AdditionalScreenFrame(
    screen: AdditionalScreen,
    actions: AdditionalActions,
    content: @Composable ColumnScope.(AccountLayout) -> Unit
) {
    val window = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val layout = AccountLayout.forWindow(
        with(density) { window.width.toDp().value },
        with(density) { window.height.toDp().value }
    )
    val scroll = rememberScrollState()
    LaunchedEffect(screen) { scroll.scrollTo(0) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().imePadding().exportTestTags()
                .testTag("design-${screen.designId}-${layout.name}")
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (layout.tablet) ProductWordmark(Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = actions.back) {
                    AccountIcon(AccountIcon.Back, contentDescription = "Назад")
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        screen.title, fontFamily = rememberRobotoCondensedFontFamily(),
                        fontSize = if (layout.tablet) 28.sp else 26.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        "Локальний приклад · серверні дані не отримано",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { actions.unavailable("Довідка") }) {
                    AccountIcon(AccountIcon.Help, contentDescription = "Довідка")
                }
            }
            Row(Modifier.weight(1f)) {
                if (layout.landscape) BottomNavigation(
                    additionalNavigationSection(screen), actions.section,
                    rail = true, compactRail = !layout.tablet, accountStyle = true
                )
                Column(
                    Modifier.weight(1f).verticalScroll(scroll)
                        .padding(horizontal = if (layout.tablet) 24.dp else 20.dp)
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content(layout)
                }
            }
            if (!layout.landscape && screen !in screensWithoutBottomNavigation) {
                BottomNavigation(additionalNavigationSection(screen), actions.section)
            }
        }
    }
}
