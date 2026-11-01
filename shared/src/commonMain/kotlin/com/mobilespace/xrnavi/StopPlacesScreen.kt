package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.stop_category_border
import xrnavi.shared.generated.resources.stop_category_charging
import xrnavi.shared.generated.resources.stop_category_fuel
import xrnavi.shared.generated.resources.stop_category_parking
import xrnavi.shared.generated.resources.stop_category_rest
import xrnavi.shared.generated.resources.stop_category_service
import xrnavi.shared.generated.resources.stop_category_weighing
import xrnavi.shared.generated.resources.stop_places_back
import xrnavi.shared.generated.resources.stop_places_empty_description
import xrnavi.shared.generated.resources.stop_places_empty_title
import xrnavi.shared.generated.resources.stop_places_search
import xrnavi.shared.generated.resources.stop_places_theme
import xrnavi.shared.generated.resources.stop_places_title

@Composable
internal fun StopPlacesScreen(
    state: StopPlacesState,
    onEvent: (StopPlacesEvent) -> Unit,
    onToggleTheme: () -> Unit
) {
    val back = { onEvent(StopPlacesEvent.Back) }
    AuthBackHandler(back)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(Modifier.fillMaxSize().imePadding().exportTestTags()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = back) {
                    Text(stringResource(Res.string.stop_places_back))
                }
                TextButton(onClick = onToggleTheme) {
                    Text(stringResource(Res.string.stop_places_theme))
                }
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().testTag("stop-places-content")
                    .verticalScroll(rememberScrollState()).padding(Constants.DP_16),
                verticalArrangement = Arrangement.spacedBy(Constants.DP_12)
            ) {
                ProductWordmark()
                Text(
                    stringResource(Res.string.stop_places_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onEvent(StopPlacesEvent.Query(it)) },
                    label = { Text(stringResource(Res.string.stop_places_search)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Constants.DP_8),
                    verticalArrangement = Arrangement.spacedBy(Constants.DP_4)
                ) {
                    StopPlaceCategory.entries.forEach { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { onEvent(StopPlacesEvent.Category(category)) },
                            label = { Text(stringResource(category.labelResource())) }
                        )
                    }
                }
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(Constants.DP_16),
                        verticalArrangement = Arrangement.spacedBy(Constants.DP_8)
                    ) {
                        Text(
                            stringResource(Res.string.stop_places_empty_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(Res.string.stop_places_empty_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StopPlaceCategory.labelResource() = when (this) {
    StopPlaceCategory.Parking -> Res.string.stop_category_parking
    StopPlaceCategory.Rest -> Res.string.stop_category_rest
    StopPlaceCategory.Fuel -> Res.string.stop_category_fuel
    StopPlaceCategory.Charging -> Res.string.stop_category_charging
    StopPlaceCategory.Service -> Res.string.stop_category_service
    StopPlaceCategory.Weighing -> Res.string.stop_category_weighing
    StopPlaceCategory.Border -> Res.string.stop_category_border
}
