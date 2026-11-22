package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import xrnavi.shared.generated.resources.*

@Composable
internal fun AddressSearchCard(
    origin: String,
    destination: String,
    target: SearchTarget,
    query: String,
    onTargetChange: (SearchTarget) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val originFocusRequester = remember { FocusRequester() }
    val destinationFocusRequester = remember { FocusRequester() }
    var requestedFocusTarget by remember { mutableStateOf<SearchTarget?>(null) }
    LaunchedEffect(target, requestedFocusTarget) {
        if (requestedFocusTarget == target) {
            when (target) {
                SearchTarget.Origin -> originFocusRequester.requestFocus()
                SearchTarget.Destination -> destinationFocusRequester.requestFocus()
            }
            requestedFocusTarget = null
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            AddressQueryRow(
                target = SearchTarget.Origin,
                active = target == SearchTarget.Origin,
                value = if (target == SearchTarget.Origin) query else origin,
                placeholder = "Моє місцезнаходження",
                onActivate = {
                    requestedFocusTarget = SearchTarget.Origin
                    onTargetChange(SearchTarget.Origin)
                },
                onValueChange = onQueryChange,
                onSearch = onSearch,
                showSearch = target == SearchTarget.Origin,
                focusRequester = originFocusRequester
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            AddressQueryRow(
                target = SearchTarget.Destination,
                active = target == SearchTarget.Destination,
                value = if (target == SearchTarget.Destination) query else destination,
                placeholder = "Введіть адресу",
                onActivate = {
                    requestedFocusTarget = SearchTarget.Destination
                    onTargetChange(SearchTarget.Destination)
                },
                onValueChange = onQueryChange,
                onSearch = onSearch,
                showSearch = target == SearchTarget.Destination,
                focusRequester = destinationFocusRequester
            )
        }
    }
}
