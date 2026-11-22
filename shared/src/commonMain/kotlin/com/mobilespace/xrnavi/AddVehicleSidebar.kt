package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AddVehicleSidebar(
    state: AddVehicleState,
    modifier: Modifier,
) {
    val title = if (state.editingVehicle == null) "Ваш транспорт" else state.editingVehicle.name
    val context = listOfNotNull(
        state.year?.toString() ?: state.editingVehicle?.name?.substringAfterLast(" ")
            ?.takeIf { it.toIntOrNull() != null },
        state.equipment.selectedTrim?.name ?: state.trim?.name,
    ).joinToString(" · ").ifEmpty { "Оберіть авто з каталогу" }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            fontFamily = rememberRobotoCondensedFontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            context,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            if (state.editingVehicle != null) "Зміни збережуть існуючий профіль авто."
            else "Дані синхронізуються із сервером",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
