package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun StopPlacesScreen(
    state: StopPlacesState,
    onEvent: (StopPlacesEvent) -> Unit,
    onToggleTheme: () -> Unit
) {
    val back = { onEvent(StopPlacesEvent.Back) }
    val contentScroll = rememberScrollState()
    LaunchedEffect(state.selectedId) { contentScroll.scrollTo(0) }
    AuthBackHandler(back)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(Modifier.fillMaxSize().imePadding().exportTestTags()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = back) { Text("Назад") }
                TextButton(onClick = onToggleTheme) { Text("Тема") }
            }
            val place = state.selected
            Column(
                Modifier.weight(1f).fillMaxWidth().testTag("stop-places-content")
                    .verticalScroll(contentScroll).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductWordmark()
                Text(
                    if (place == null) "Місця зупинки" else place.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("Локальний демокаталог · синтетичні об’єкти")
                if (state.notice.isNotBlank()) Text(state.notice)
                if (place == null) {
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
                    if (state.results.isEmpty()) {
                        Text(
                            if (stopPlaces.none { it.category == state.category }) {
                                "У демокаталозі немає об’єктів цього типу"
                            } else {
                                "За цим запитом у демокаталозі нічого не знайдено"
                            }
                        )
                    }
                    state.results.forEach { result ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = { onEvent(StopPlacesEvent.Details(result.id)) }) {
                                    Text(result.name, style = MaterialTheme.typography.titleMedium)
                                }
                                Text(result.category.label)
                                Text("${result.address.title} · ${result.address.detail}")
                                Text("Демонстраційний об’єкт · детальніше")
                            }
                        }
                    }
                } else {
                    Text(place.category.label)
                    Text("${place.address.title} · ${place.address.detail}")
                    Text(place.description)
                    Text("Години роботи: Не задано")
                    Text("Місця та тарифи: Не задано")
                    Text("Вільні місця: Не перевірено")
                    Text("Габарити, маса та придатність для автомобіля: Не перевірено")
                    Text("Маршрутизацію не підключено. Додавання не підтверджує безпечний маршрут або заїзд.")
                }
            }
            if (place != null) {
                Button(
                    onClick = { onEvent(StopPlacesEvent.AddSelected) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("Додати зупинку") }
            }
        }
    }
}
