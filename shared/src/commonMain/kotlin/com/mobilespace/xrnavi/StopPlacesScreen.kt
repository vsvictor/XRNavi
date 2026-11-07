package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun StopPlacesScreen(
    state: StopPlacesState,
    onEvent: (StopPlacesEvent) -> Unit,
    onToggleTheme: () -> Unit,
    onSectionChange: ((DemoSection) -> Unit)? = null
) {
    val back = { onEvent(StopPlacesEvent.Back) }
    val contentScroll = rememberScrollState()
    LaunchedEffect(state.selectedId) { contentScroll.scrollTo(0) }
    AuthBackHandler(back)
    val window = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val layout = AccountLayout.forWindow(
        with(density) { window.width.toDp().value },
        with(density) { window.height.toDp().value }
    )
    val place = state.selected
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(Modifier.fillMaxSize().imePadding().exportTestTags()) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (layout.tablet) ProductWordmark(Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = back) {
                    AccountIcon(
                        AccountIcon.Back,
                        contentDescription = "Назад"
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        if (place == null) "Зупинки на маршруті" else place.name,
                        fontFamily = rememberRobotoCondensedFontFamily(), fontSize = 26.sp
                    )
                    AdditionalBody("Локальний демокаталог · синтетичні об’єкти")
                }
                TextButton(onClick = onToggleTheme) { Text("Тема") }
            }
            Row(Modifier.weight(1f)) {
                if (layout.landscape && onSectionChange != null) BottomNavigation(
                    DemoSection.Map, onSectionChange, rail = true, compactRail = !layout.tablet,
                    accountStyle = true
                )
                Column(
                    Modifier.weight(1f).testTag("stop-places-content")
                        .verticalScroll(contentScroll).padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (place == null) {
                        AdditionalColumns(layout, primary = {
                            OutlinedTextField(
                                value = state.query,
                                onValueChange = { onEvent(StopPlacesEvent.Query(it)) },
                                label = { Text("Пошук у демокаталозі") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            FlowRow(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                StopPlaceCategory.entries.forEach { category ->
                                    FilterChip(
                                        selected = state.category == category,
                                        onClick = { onEvent(StopPlacesEvent.Category(category)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.12f
                                            ),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        ),
                                        label = { Text(category.label) }
                                    )
                                }
                            }
                            AdditionalRouteIllustration("Схема з макета · об’єкти каталогу на ній не позначено")
                        }, secondary = {
                            if (state.results.isEmpty()) {
                                AdditionalNotice(
                                    "Об’єктів не знайдено",
                                    if (stopPlaces.none { it.category == state.category }) {
                                        "У демокаталозі немає об’єктів цього типу"
                                    } else {
                                        "За цим запитом у демокаталозі нічого не знайдено"
                                    }
                                )
                            }
                            state.results.forEach { result ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable(role = Role.Button) {
                                            onEvent(StopPlacesEvent.Details(result.id))
                                        },
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary
                                    ),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(
                                        Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AdditionalTitle(result.name)
                                        AdditionalBody("${result.category.label} · ${result.address.title}")
                                        AdditionalBody("Вільні місця не підтверджені · детальніше ›")
                                    }
                                }
                            }
                            AdditionalNotice(
                                "Придатність не перевірена",
                                "Габарити, масу, доступність в’їзду та послуги потрібно уточнити. Синтетичні об’єкти не є рекомендаціями."
                            )
                        })
                    } else {
                        AdditionalColumns(layout, primary = {
                            AdditionalRouteIllustration("Приклад схеми · не підтверджує місце чи під’їзд до об’єкта")
                            AdditionalCard {
                                AdditionalTitle(place.category.label, large = true)
                                AdditionalBody("${place.address.title} · ${place.address.detail}")
                                AdditionalBody(place.description)
                            }
                        }, secondary = {
                            AdditionalHeading("Характеристики та послуги")
                            AdditionalCard {
                                AdditionalBody("Години роботи: Не задано")
                                AdditionalBody("Довжина місця та ширина в’їзду: Не перевірено")
                                AdditionalBody("Місця, тарифи та допустима маса: Не задано")
                                AdditionalBody("Охорона, душ, туалет, кафе: Не перевірено")
                            }
                            OutlinedButton(
                                onClick = { onEvent(StopPlacesEvent.Unavailable("Контакт персоналу")) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Уточнити в’їзд у персоналу") }
                            AdditionalNotice(
                                "Немає даних про вільні місця",
                                "Придатність, вільні місця та безпечний під’їзд не підтверджені. Маршрутизацію не підключено.",
                                warning = true
                            )
                            Button(
                                onClick = { onEvent(StopPlacesEvent.AddSelected) },
                                modifier = Modifier.fillMaxWidth().testTag("add-stop-place")
                            ) { Text("Додати зупинку · локально") }
                        })
                    }
                    if (state.notice.isNotBlank()) AdditionalNotice(
                        "Демокаталог",
                        state.notice,
                        warning = true
                    )
                }
            }
            if (!layout.landscape && onSectionChange != null) BottomNavigation(
                DemoSection.Map, onSectionChange
            )
        }
    }
}
