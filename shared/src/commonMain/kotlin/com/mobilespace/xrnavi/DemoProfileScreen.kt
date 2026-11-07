package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ColumnScope.DemoProfileScreen(
    profile: DemoProfileState,
    layout: AccountLayout,
    savedPlacesCount: Int,
    onOpenSavedPlaces: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenVehicleProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitDemo: () -> Unit,
    onRegister: () -> Unit,
    onRecovery: () -> Unit,
    onOpenAdditional: ((AdditionalScreen) -> Unit)?,
    unavailable: (String) -> Unit
) {
    val savedPlacesSubtitle = if (savedPlacesCount == 0) "Поки порожньо · локально"
    else "$savedPlacesCount місць · локально"
    val additionalActions = listOf(
        AdditionalScreen.DriverTasks, AdditionalScreen.TripHistory, AdditionalScreen.Invitation
    )
    val accountActions: @Composable ColumnScope.() -> Unit = {
        ProfileOrganization(profile, layout)
        AccountAction("Збережені місця", savedPlacesSubtitle, onOpenSavedPlaces)
        ProfileSecondaryActions(
            horizontal = layout == AccountLayout.PhonePortrait ||
                layout == AccountLayout.PhoneLandscape,
            onOpenPrivacy = onOpenAdditional?.let { open ->
                { open(AdditionalScreen.Privacy) }
            },
            onOpenSavedPlaces = onOpenSavedPlaces,
            unavailable = unavailable
        )
        AdditionalAccountActions(additionalActions, onOpenAdditional)
        TextButton(onClick = onOpenSettings) { Text("Відкрити налаштування") }
        ProfileAuthActions(onExitDemo, onRegister, onRecovery)
    }
    val identity: @Composable ColumnScope.() -> Unit = {
        ProfileIdentity(profile, layout, unavailable)
    }
    val vehicle: @Composable ColumnScope.() -> Unit = {
        ProfileVehicle(
            profile,
            layout,
            onOpenGarage = onOpenGarage,
            onOpenVehicleProfile = onOpenVehicleProfile
        )
        ProfileStatistics(layout)
    }
    when (layout) {
        AccountLayout.PhoneLandscape -> ResponsiveSections(
            columns = true,
            firstWeight = 0.46f,
            first = vehicle,
            second = { identity(); accountActions() }
        )

        AccountLayout.TabletLandscape -> ResponsiveSections(
            columns = true,
            firstWeight = 0.48f,
            gap = 24,
            first = { identity(); accountActions() },
            second = vehicle
        )

        else -> {
            identity()
            vehicle()
            if (layout == AccountLayout.TabletPortrait) {
                AccountAction("Збережені місця", savedPlacesSubtitle, onOpenSavedPlaces)
                ResponsiveSections(
                    columns = true,
                    first = { ProfileOrganization(profile, layout) },
                    second = {
                        ProfileSecondaryActions(
                            onOpenPrivacy = onOpenAdditional?.let { open ->
                                { open(AdditionalScreen.Privacy) }
                            },
                            onOpenSavedPlaces = onOpenSavedPlaces,
                            unavailable = unavailable
                        )
                        AdditionalAccountActions(additionalActions, onOpenAdditional)
                    }
                )
                TextButton(onClick = onOpenSettings) { Text("Відкрити налаштування") }
                ProfileAuthActions(onExitDemo, onRegister, onRecovery)
            } else {
                accountActions()
            }
        }
    }
    AccountCard {
        CardHeading("Про демопрофіль")
        AccountBody(profile.accountDetails)
        AccountBody("Єдиний наявний демопрофіль. Це не підтверджене володіння автомобілем.")
        AccountBody(profile.statisticsExplanation)
    }
}

@Composable
private fun ProfileIdentity(
    profile: DemoProfileState,
    layout: AccountLayout,
    unavailable: (String) -> Unit
) {
    Column(
        Modifier.testTag("profile-identity"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val edit: @Composable () -> Unit = {
            Button(
                onClick = { unavailable("Редагування акаунта не підключено. Цей демо-профіль не містить підтверджених особистих даних.") },
                shape = RoundedCornerShape(24.dp),
                modifier = (if (layout == AccountLayout.TabletPortrait) Modifier else Modifier.fillMaxWidth())
                    .heightIn(min = 44.dp)
            ) {
                Text(
                    "Редагувати профіль · не підключено",
                    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(
                    when (layout) {
                        AccountLayout.PhoneLandscape -> 44.dp
                        AccountLayout.TabletLandscape -> 80.dp
                        else -> 64.dp
                    }
                ),
                shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "MS", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                        lineHeight = 24.sp, letterSpacing = 0.sp
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                SectionTitle(
                    profile.title,
                    fontSize = if (layout == AccountLayout.TabletLandscape) 32 else 24,
                    lineHeight = if (layout == AccountLayout.TabletLandscape) 40 else 30
                )
                AccountBody(profile.identity)
            }
            if (layout == AccountLayout.TabletPortrait) edit()
        }
        if (layout != AccountLayout.TabletPortrait) edit()
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Особистий", "Корпоративний").forEach { mode ->
                Surface(
                    Modifier.weight(1f).semantics {
                        selected = mode == profile.vehicleMode
                        contentDescription = "$mode · режим вибору транспорту на мапі"
                    },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline),
                    color = if (mode == profile.vehicleMode && MaterialTheme.colorScheme.background.red < 0.5f)
                        Color(0xFF3E465F)
                    else if (mode == profile.vehicleMode) MaterialTheme.colorScheme.surfaceVariant
                    else accountSurfaceColor()
                ) {
                    Text(
                        mode, Modifier.padding(vertical = 8.dp), fontSize = 14.sp,
                        lineHeight = 20.sp, letterSpacing = 0.1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        AccountBody("Режим мапи: ${profile.vehicleMode} · без членства в компанії.")
    }
}

@Composable
private fun ProfileVehicle(
    profile: DemoProfileState,
    layout: AccountLayout,
    onOpenGarage: () -> Unit,
    onOpenVehicleProfile: () -> Unit
) {
    val vehicle = profile.selectedVehicle ?: demoVehicle
    Row(
        Modifier.fillMaxWidth().testTag("profile-garage-heading"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Мій гараж",
            Modifier.weight(1f),
            fontSize = if (layout.tablet) 20.sp else 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = if (layout.tablet) 28.sp else 22.sp
        )
        TextButton(
            onClick = onOpenGarage,
            modifier = Modifier.semantics {
                contentDescription = "Керувати гаражем"
            },
            contentPadding = PaddingValues(start = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Керувати гаражем", fontSize = 12.sp)
                AccountIcon(
                    AccountIcon.ChevronRight,
                    Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    Surface(
        modifier = Modifier.testTag("profile-vehicle-card"),
        shape = RoundedCornerShape(10.dp), color = accountSurfaceColor()
    ) {
        Column {
            Box {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(
                        when (layout) {
                            AccountLayout.TabletPortrait -> 3.93f
                            AccountLayout.TabletLandscape -> 1.95f
                            AccountLayout.PhoneLandscape -> 3f
                            AccountLayout.PhonePortrait -> 3.48f
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
                Row(
                    Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = accountSurfaceColor()) {
                        Text(
                            if (profile.selectedVehicle != null) "Демоавто" else "Несумісний профіль",
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = if (layout.tablet) 20.sp else 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp)
                            .clickable(role = Role.Button, onClick = onOpenVehicleProfile)
                            .semantics { contentDescription = "Переглянути профіль ${vehicle.name}" }
                    ) {
                        Surface(shape = RoundedCornerShape(4.dp), color = accountSurfaceColor()) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "Деталі авто",
                                    fontSize = if (layout.tablet) 13.sp else 11.sp,
                                    lineHeight = if (layout.tablet) 20.sp else 16.sp
                                )
                                AccountIcon(
                                    AccountIcon.ChevronRight,
                                    Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Column(
                Modifier.padding(horizontal = 12.dp, vertical = if (layout.tablet) 8.dp else 3.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    vehicle.name,
                    fontSize = if (layout.tablet) 32.sp else 22.sp,
                    lineHeight = if (layout.tablet) 40.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp,
                    fontFamily = rememberRobotoCondensedFontFamily()
                )
                AccountBody("${vehicle.category} · демонстраційні характеристики")
            }
        }
    }
    if (profile.selectedVehicle != null) {
        AccountBody("Демо · ${profile.vehicleMode} · ${profile.vehicleClass}")
    } else {
        AuthInfo("Активна категорія на мапі: ${profile.vehicleClass}. Сумісного профілю немає; Mustang не вибрано для цієї категорії.")
    }
}

@Composable
private fun ProfileOrganization(profile: DemoProfileState, layout: AccountLayout) {
    AccountCard(
        tonal = true,
        minHeight = if (layout == AccountLayout.TabletLandscape) 164 else 0
    ) {
        CardHeading("Організація")
        AccountBody(profile.organization)
        AccountBody("Синхронізація та корпоративні права недоступні.")
    }
}

@Composable
private fun ProfileSecondaryActions(
    horizontal: Boolean = false,
    onOpenPrivacy: (() -> Unit)? = null,
    onOpenSavedPlaces: (() -> Unit)? = null,
    unavailable: (String) -> Unit
) {
    val security: @Composable () -> Unit = {
        AccountAction("Безпека та особисті дані",
            if (onOpenPrivacy != null) "Відкрити центр приватності" else "Не підключено") {
            if (onOpenPrivacy != null) onOpenPrivacy()
            else unavailable("Керування даними акаунта й видалення профілю не підключено. Демо-профіль не є акаунтом.")
        }
    }
    val favorites: @Composable () -> Unit = {
        AccountAction("Обране", if (onOpenSavedPlaces != null) "Локальні місця" else "Немає даних") {
            if (onOpenSavedPlaces != null) onOpenSavedPlaces()
            else unavailable("Сервіс обраного не підключено. Збережених даних немає.")
        }
    }
    if (horizontal) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(2f)) { security() }
        Box(Modifier.weight(1f)) { favorites() }
    } else {
        security()
        favorites()
    }
}

@Composable
private fun ProfileStatistics(layout: AccountLayout) {
    Row(
        Modifier.testTag("profile-statistics"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("поїздки", "км", "год у дорозі").forEach { label ->
            Surface(
                Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    Modifier.heightIn(
                        min = when (layout) {
                            AccountLayout.TabletLandscape -> 88.dp
                            AccountLayout.TabletPortrait, AccountLayout.PhoneLandscape -> 72.dp
                            AccountLayout.PhonePortrait -> 64.dp
                        }
                    ).padding(horizontal = 12.dp, vertical = if (layout.tablet) 12.dp else 6.dp)
                ) {
                    Text(
                        "—",
                        fontSize = if (layout.tablet) 32.sp else 26.sp,
                        lineHeight = if (layout.tablet) 38.sp else 31.sp,
                        fontFamily = rememberRobotoCondensedFontFamily(),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(
                        Modifier.height(
                            when (layout) {
                                AccountLayout.PhonePortrait -> 2.dp
                                AccountLayout.TabletLandscape -> 4.dp
                                else -> 0.dp
                            }
                        )
                    )
                    Text(
                        label, fontSize = 12.sp,
                        lineHeight = 16.sp, letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    AccountBody("Статистика відсутня · деморежим")
}

@Composable
private fun ProfileAuthActions(onExit: () -> Unit, onRegister: () -> Unit, onRecovery: () -> Unit) {
    AccountCard {
        CardHeading("Вхід і реєстрація")
        AccountBody("Ці дії закривають демонстраційні дані та поточну сесію входу.")
        TextButton(onClick = onExit) { Text("Вийти з деморежиму") }
        TextButton(onClick = onRegister) { Text("Відкрити реєстрацію") }
        TextButton(onClick = onRecovery) { Text("Відновлення пароля") }
    }
}
