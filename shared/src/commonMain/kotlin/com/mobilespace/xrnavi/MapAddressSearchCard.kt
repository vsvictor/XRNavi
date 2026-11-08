package com.mobilespace.xrnavi

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MapAddressSearchCard(
    state: RouteState,
    near: GeoPoint,
    geo: GeoState,
    dispatch: (RouteEvent) -> Unit,
    geoDispatch: (GeoEvent) -> Unit,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val title = if (state.editing == RouteField.Origin) "Звідки" else "Куди"
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { dispatch(RouteEvent.QueryChanged(it, near)) },
                    modifier = Modifier.weight(1f).focusRequester(focus).testTag("route-search-input"),
                    label = { Text(title) },
                    singleLine = true,
                )
                TextButton(
                    onClick = { dispatch(RouteEvent.CloseSearch) },
                    modifier = Modifier.testTag("route-search-close"),
                ) { Text("Закрити") }
            }
            if (state.editing == RouteField.Origin) {
                val fix = geo.reliableFix
                Column(
                    Modifier.fillMaxWidth()
                        .clickable {
                            if (fix != null) {
                                dispatch(RouteEvent.UseCurrentLocation(
                                    RoutePlace("Моє місцезнаходження", "Надійна позиція пристрою", fix.point),
                                ))
                            } else {
                                dispatch(RouteEvent.CloseSearch)
                                geoDispatch(GeoEvent.Locate)
                            }
                        }
                        .testTag("route-use-current"),
                ) {
                    Text("Моє місцезнаходження", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        if (fix != null) "Надійна позиція" else "Потрібне визначення позиції",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            when (state.searchPhase) {
                SearchPhase.Idle -> Text(
                    "Введіть адресу (від 2 символів). Запит ще не виконується.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("search-idle"),
                )
                SearchPhase.Loading -> CircularProgressIndicator(Modifier.size(20.dp).testTag("search-loading"))
                SearchPhase.Empty -> Text(
                    "Нічого не знайдено. Це не помилка мережі: уточніть адресу.",
                    fontSize = 13.sp, modifier = Modifier.testTag("search-empty"),
                )
                SearchPhase.Error -> Column(Modifier.testTag("search-error")) {
                    Text(state.searchError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    Text(
                        "Перевірте з’єднання. Введена адреса збережена.",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { dispatch(RouteEvent.RetrySearch) }) { Text("Повторити") }
                }
                SearchPhase.Results -> Unit
            }
            Column(Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState())) {
                state.suggestions.forEach { place ->
                    Column(
                        Modifier.fillMaxWidth()
                            .clickable { dispatch(RouteEvent.SelectPlace(place)) }
                            .padding(vertical = 8.dp)
                            .semantics { contentDescription = "${place.name}. ${place.address}" }
                            .testTag("route-suggestion"),
                    ) {
                        Text(place.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (place.address != place.name) {
                            Text(
                                place.address, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}
