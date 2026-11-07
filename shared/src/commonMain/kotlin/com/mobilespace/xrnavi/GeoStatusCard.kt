package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class GeoCardContent(
    val title: String,
    val body: String,
    val primary: String? = null,
    val primaryEvent: GeoEvent? = null,
)

internal fun geoCardContent(state: GeoState): GeoCardContent? = when (state.phase) {
    GeoPhase.Reliable -> null
    GeoPhase.Unknown -> if (!state.active) null else GeoCardContent(
        "Очікуємо дозвіл",
        "Підтвердьте запит системи на доступ до геолокації або вкажіть початок вручну.",
    )
    GeoPhase.Acquiring -> GeoCardContent(
        "Очікуємо позицію",
        "Визначаємо місцезнаходження… Зачекайте на надійну позицію або вкажіть початок вручну.",
    )
    GeoPhase.TimedOut -> GeoCardContent(
        "Позицію не визначено",
        "Надійної позиції поки немає. Спробуйте ще раз або вкажіть початок вручну.",
        "Повторити", GeoEvent.Locate,
    )
    GeoPhase.Denied -> GeoCardContent(
        "Немає доступу до геолокації",
        "Початок «моє місцезнаходження» потребує дозволу. Без нього вкажіть початок вручну.",
        "Дозволити", GeoEvent.Locate,
    )
    GeoPhase.PermanentlyDenied -> GeoCardContent(
        "Геолокацію заборонено",
        "Система більше не показує запит. Відкрийте налаштування застосунку або вкажіть початок вручну.",
        "Відкрити налаштування", GeoEvent.OpenSettings,
    )
    GeoPhase.ServiceOff -> GeoCardContent(
        "Служба геолокації вимкнена",
        "Дозвіл застосунку не означає, що служба пристрою ввімкнена. Позиція не вважається актуальною.",
        "Відкрити налаштування", GeoEvent.OpenSettings,
    )
    GeoPhase.Stale -> GeoCardContent(
        "Остання відома позиція",
        "Позиція застаріла (${ageMinutes(state)} хв тому) і не використовується як поточний початок.",
        "Оновити", GeoEvent.Locate,
    )
}

private fun ageMinutes(state: GeoState): Long =
    ((state.now - (state.fix?.timestampMillis ?: state.now)) / 60_000).coerceAtLeast(0)

@Composable
internal fun GeoStatusCard(
    state: GeoState,
    dispatch: (GeoEvent) -> Unit,
    onManualOrigin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = geoCardContent(state) ?: return
    Surface(
        modifier.fillMaxWidth().testTag("geo-card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(content.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(content.body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (content.primary != null && content.primaryEvent != null) {
                    TextButton(onClick = { dispatch(content.primaryEvent) }) { Text(content.primary) }
                }
                TextButton(onClick = onManualOrigin, modifier = Modifier.testTag("geo-manual")) {
                    Text("Вказати початок вручну")
                }
                TextButton(onClick = { dispatch(GeoEvent.Stop) }) { Text("Закрити") }
            }
        }
    }
}
