package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthenticatedProfileVehicle(
    state: GarageState,
    onOpenGarage: () -> Unit,
    onOpenVehicleDetails: (String) -> Unit,
) {
    val activeVehicle = state.active
    Column(
        Modifier.fillMaxWidth().testTag("authenticated-profile-garage"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Мій гараж",
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = onOpenGarage, modifier = Modifier.testTag("profile-open-garage")) {
                Text("Керувати гаражем")
            }
        }
        when {
            state.loading -> Row(
                Modifier.fillMaxWidth().heightIn(min = 72.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(Modifier.testTag("profile-garage-loading"))
                Text("Завантаження гаража…")
            }
            state.loadFailed -> Surface(
                color = accountSurfaceColor(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)
                    .testTag("profile-garage-empty"),
            ) {
                Text("У гаражі немає авто.", Modifier.padding(16.dp))
            }
            state.error.isNotEmpty() -> Text(
                state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("profile-garage-error"),
            )
            !state.loaded && state.error.isEmpty() ->
                Text("Завантаження гаража…", Modifier.testTag("profile-garage-loading"))
            activeVehicle == null && state.vehicles.isEmpty() -> Surface(
                color = accountSurfaceColor(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile-garage-empty"),
            ) {
                Text("У гаражі немає авто.", Modifier.padding(16.dp))
            }
            activeVehicle == null -> Surface(
                color = accountSurfaceColor(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile-garage-no-active"),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Основне авто не вибрано.")
                    Text(
                        "У гаражі авто: ${state.vehicles.size}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> Surface(
                color = accountSurfaceColor(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile-vehicle-card"),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(activeVehicle.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${activeVehicle.category} · ${activeVehicle.verification.label()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Дані отримано з гаража акаунта.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = { onOpenVehicleDetails(activeVehicle.id) },
                        modifier = Modifier.testTag("profile-open-vehicle-details"),
                    ) {
                        Text("Деталі авто ›")
                    }
                }
            }
        }
    }
}
