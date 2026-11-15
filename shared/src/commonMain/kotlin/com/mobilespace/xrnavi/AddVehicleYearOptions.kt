package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AddVehicleYearOptions(
    years: List<Int>,
    selectedYear: Int?,
    loading: Boolean,
    error: String,
    onSelected: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    Text("Рік випуску", style = MaterialTheme.typography.titleSmall)
    when {
        loading -> CircularProgressIndicator(Modifier.testTag("add-vehicle-years-loading"))
        error.isNotEmpty() -> {
            Text(error, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text("Повторити") }
        }
        years.isEmpty() -> Text("Для цієї моделі років випуску не знайдено.")
        else -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            years.forEach { year ->
                FilterChip(
                    selected = selectedYear == year,
                    onClick = { onSelected(year) },
                    label = { Text("$year") },
                    modifier = Modifier.testTag("add-year-$year"),
                )
            }
        }
    }
}
