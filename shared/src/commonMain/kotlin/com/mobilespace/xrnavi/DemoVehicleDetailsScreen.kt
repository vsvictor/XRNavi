package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DemoVehicleDetailsScreen(
    vehicle: DemoVehicleProfile,
    layout: AccountLayout,
    onOpenConfigurations: () -> Unit
) {
    val overview: @Composable ColumnScope.() -> Unit = {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accountSurfaceColor(),
            modifier = Modifier.fillMaxWidth().testTag("vehicle-profile-details")
        ) {
            Column {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(
                        when (layout) {
                            AccountLayout.PhonePortrait -> 1.8f
                            AccountLayout.PhoneLandscape -> 1.9f
                            AccountLayout.TabletPortrait -> 2.45f
                            AccountLayout.TabletLandscape -> 1.85f
                        }
                    ).background(
                        if (vehicle.artwork == DemoVehicleArtwork.Mustang) Color.Transparent
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    DemoVehicleImage(
                        vehicle = vehicle,
                        modifier = if (vehicle.artwork == DemoVehicleArtwork.Mustang) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.size(112.dp)
                        },
                        contentDescription = "Демонстраційне зображення ${vehicle.name}"
                    )
                }
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionTitle(vehicle.name, fontSize = if (layout.tablet) 30 else 24)
                    AccountBody("Локальний демопрофіль · дані не підтверджено зовнішнім сервісом")
                    AccountBody("Це демонстраційний запис, а не підтвердження права власності.")
                }
            }
        }
    }
    val data: @Composable ColumnScope.() -> Unit = {
        AccountCard {
            CardHeading("Дані з локальної моделі")
            VehicleProfileFact("Категорія", vehicle.category)
            VehicleProfileFact("Характеристики", vehicle.specification)
            AccountBody("Параметри демонстраційні й не перевірені зовнішнім сервісом.")
            if (vehicle.id == demoVehicle.id) AccountBody(
                "Модель не уточнює, яке саме габаритне вимірювання позначене як 1,4 м, " +
                    "і не визначає 1,8 т як максимальну допустиму масу."
            )
            TextButton(
                onClick = onOpenConfigurations,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .semantics {
                        contentDescription = "Переглянути конфігурації ${vehicle.name}"
                    }
            ) {
                Text("Переглянути конфігурації")
            }
        }
        AccountCard(tonal = true) {
            CardHeading("Перевірка даних")
            AccountBody(
                "Профіль доступний лише локально для демонстрації. " +
                    "Каталог, VIN-перевірка та підтвердження власності не підключені."
            )
        }
    }

    if (layout == AccountLayout.TabletLandscape) {
        ResponsiveSections(
            columns = true,
            firstWeight = 0.56f,
            gap = 24,
            first = overview,
            second = data
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            overview()
            data()
        }
    }
}

@Composable
private fun VehicleProfileFact(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { contentDescription = "$label: $value" }
        )
    }
}
