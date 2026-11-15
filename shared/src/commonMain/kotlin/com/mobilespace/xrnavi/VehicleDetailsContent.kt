package com.mobilespace.xrnavi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
internal fun VehicleDetailsContent(vehicle: GarageVehicle) {
    val values = listOf(
        "Висота" to vehicle.heightMeters?.let { "${it.displayMeasure()} м" },
        "Ширина" to vehicle.widthMeters?.let { "${it.displayMeasure()} м" },
        "Довжина" to vehicle.lengthMeters?.let { "${it.displayMeasure()} м" },
        "Маса" to vehicle.weightKg?.let { "$it кг" },
    )
    Text(
        "${vehicle.name} · збережені дані",
        fontFamily = rememberRobotoCondensedFontFamily(),
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 23.sp,
        color = MaterialTheme.colorScheme.onSurface,
    )
    if (vehicle.needsVerification) {
        Text(
            "Профіль потребує підтвердження для побудови маршруту з урахуванням параметрів цього транспорту.",
            Modifier.testTag("vehicle-unverified"),
            color = MaterialTheme.colorScheme.error,
        )
    }
    AddVehicleEquipmentSection(
        "Габарити та маса",
        listOf(values.map { (label, value) -> label to (value ?: "Не вказано") }),
    )
    vehicle.vehicleData?.let { VehicleSavedEquipmentDetails(it) }
}
