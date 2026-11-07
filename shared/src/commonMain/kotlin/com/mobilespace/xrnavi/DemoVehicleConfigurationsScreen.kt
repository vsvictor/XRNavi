package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DemoVehicleConfigurationsScreen(
    vehicle: DemoVehicleProfile,
    configurations: List<DemoVehicleConfiguration>,
    layout: AccountLayout,
    appliedConfigurationId: String,
    draftConfigurationId: String,
    canApply: Boolean,
    onDraftChange: (DemoVehicleConfiguration) -> Unit,
    onApply: (DemoVehicleConfiguration) -> Unit,
    onReturnToGarage: () -> Unit,
    onCancel: () -> Unit
) {
    val appliedConfiguration = configurations.firstOrNull {
        it.id == appliedConfigurationId
    }
    val draftConfiguration = configurations.firstOrNull {
        it.id == draftConfigurationId
    }
    val overview: @Composable ColumnScope.() -> Unit = {
        AccountCard {
            SectionTitle(vehicle.name, fontSize = 20, lineHeight = 26)
            ConfigurationStatusPill(
                text = if (canApply) "Активне авто" else "Лише перегляд",
                applied = canApply
            )
            AccountBody("${vehicle.category} · локальний демопрофіль")
            AccountBody("Локальні дані · не підтверджені зовнішнім сервісом")
        }
    }
    val configurationList: @Composable ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(
                "Конфігурації · ${configurations.size}",
                fontSize = if (layout.tablet) 22 else 20
            )
        }
        if (configurations.isEmpty()) {
            AccountCard(tonal = true) {
                CardHeading("Конфігурацій немає")
                AccountBody("Для цього профілю не задано локальних конфігурацій.")
            }
        } else {
            configurations.forEach { configuration ->
                val selected = configuration.id == draftConfigurationId
                val applied = configuration.id == appliedConfigurationId
                val incomplete = configuration.availability ==
                    VehicleConfigurationAvailability.Incomplete
                val status = when {
                    incomplete && selected && !applied -> "Обрано · неповні дані"
                    incomplete -> "Неповні дані"
                    applied -> "Застосовано"
                    selected -> "Обрано · ще не застосовано"
                    else -> "Доступно"
                }
                Surface(
                    modifier = Modifier.fillMaxWidth()
                        .testTag("vehicle-configuration-${configuration.id}")
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            role = Role.RadioButton,
                            onClick = { onDraftChange(configuration) }
                        )
                        .semantics {
                            this.selected = selected
                            stateDescription = when {
                                incomplete && selected && !applied ->
                                    "Обрано, ще не застосовано; неповні дані"
                                incomplete -> "Неповні дані, застосування недоступне"
                                applied -> "Застосовано"
                                selected -> "Обрано, ще не застосовано"
                                else -> "Не вибрано"
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                    else accountSurfaceColor(),
                    border = BorderStroke(
                        if (selected) 1.5.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (layout == AccountLayout.TabletLandscape) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ConfigurationName(configuration.name, Modifier.weight(1f))
                                ConfigurationStatusPill(
                                    text = status,
                                    selected = selected && !applied,
                                    applied = applied,
                                    unavailable = incomplete
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                ConfigurationName(configuration.name, Modifier.fillMaxWidth())
                                ConfigurationStatusPill(
                                    text = status,
                                    selected = selected && !applied,
                                    applied = applied,
                                    unavailable = incomplete
                                )
                            }
                        }
                        AccountBody(configuration.summary)
                        if (configuration.synthetic) {
                            AccountBody("Синтетичні демодані; не параметри виробника.")
                        }
                        if (incomplete) {
                            AccountBody("Причина: дані конфігурації неповні.")
                        }
                    }
                }
            }
        }
    }
    val selectionDetails: @Composable ColumnScope.() -> Unit = {
        AccountCard(tonal = true) {
            CardHeading(
                if (draftConfigurationId == appliedConfigurationId) {
                    "Поточна застосована конфігурація"
                } else {
                    "Обрано, ще не застосовано"
                }
            )
            if (draftConfiguration == null) {
                AccountBody("Не задано")
            } else {
                CardHeading(draftConfiguration.name)
                AccountBody(draftConfiguration.summary)
                if (draftConfiguration.synthetic) {
                    AccountBody("Синтетичні демодані; це не підтверджені характеристики авто.")
                }
            }
            if (draftConfigurationId != appliedConfigurationId && appliedConfiguration != null) {
                AccountBody("Застосовано зараз: ${appliedConfiguration.name}")
            }
            AccountBody("Перегляд не змінює транспорт або маршрут.")
        }
    }
    val actions: @Composable ColumnScope.() -> Unit = {
        val unavailableReason = when {
            configurations.isEmpty() -> "Для цього автомобіля немає конфігурацій для застосування."
            !canApply -> "Цей автомобіль не активний. Спочатку явно оберіть його в гаражі."
            draftConfiguration?.availability == VehicleConfigurationAvailability.Incomplete ->
                "Застосування недоступне: у вибраної конфігурації неповні дані."
            draftConfiguration == null -> "Виберіть доступну конфігурацію цього автомобіля."
            else -> null
        }
        if (unavailableReason != null) {
            AccountCard(tonal = true) {
                CardHeading("Застосування недоступне")
                AccountBody(unavailableReason)
            }
        }
        if (!canApply) {
            OutlinedButton(
                onClick = onReturnToGarage,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("Повернутися до гаража")
            }
        }
        Button(
            onClick = { draftConfiguration?.let(onApply) },
            enabled = canApply &&
                draftConfiguration?.availability == VehicleConfigurationAvailability.Available,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .testTag("apply-vehicle-configuration"),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Застосувати конфігурацію")
        }
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text("Скасувати")
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().testTag("vehicle-configurations"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (layout == AccountLayout.PhonePortrait) {
            overview()
            selectionDetails()
            configurationList()
            actions()
        } else {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (layout.tablet) 16.dp else 12.dp)
            ) {
                Column(
                    Modifier.weight(0.47f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    overview()
                    configurationList()
                }
                Column(
                    Modifier.weight(0.53f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    selectionDetails()
                    actions()
                }
            }
        }
    }
}

@Composable
private fun ConfigurationName(text: String, modifier: Modifier) {
    Text(
        text,
        modifier = modifier,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ConfigurationStatusPill(
    text: String,
    selected: Boolean = false,
    applied: Boolean = false,
    unavailable: Boolean = false
) {
    val background = when {
        unavailable -> MaterialTheme.colorScheme.surfaceVariant
        applied -> MaterialTheme.colorScheme.secondaryContainer
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when {
        unavailable -> MaterialTheme.colorScheme.onSurfaceVariant
        applied -> MaterialTheme.colorScheme.onSecondaryContainer
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = background, shape = RoundedCornerShape(16.dp)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = content,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
