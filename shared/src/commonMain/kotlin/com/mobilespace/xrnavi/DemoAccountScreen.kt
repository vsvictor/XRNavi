package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun DemoAccountScreen(
    section: DemoSection,
    profile: DemoProfileState,
    themeSettings: ThemeSettingsViewModel,
    onSectionChange: (DemoSection) -> Unit,
    onBack: () -> Unit,
    onSelectVehicle: (DemoVehicleProfile) -> Unit,
    onExitDemo: () -> Unit,
    onRegister: () -> Unit,
    onRecovery: () -> Unit
) {
    AuthBackHandler(onBack)
    var unavailableAction by remember(section) { mutableStateOf<String?>(null) }
    var showThemePicker by rememberSaveable { mutableStateOf(false) }
    var garageFilterName by rememberSaveable { mutableStateOf(GarageFilter.All.name) }
    val sectionStateHolder = rememberSaveableStateHolder()
    val version = rememberAppVersion()
    val garageFilter = GarageFilter.valueOf(garageFilterName)
    val navigationSection = if (section == DemoSection.VehicleDetails) {
        DemoSection.Garage
    } else {
        section
    }
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (unavailableAction != null || showThemePicker) {
                val dismiss = {
                    unavailableAction = null
                    showThemePicker = false
                }
                AlertDialog(
                    onDismissRequest = dismiss,
                    // Keep the layer's measurement in host coordinates, including after rotation.
                    modifier = Modifier.requiredSize(maxWidth, maxHeight)
                        .pointerInput(Unit) { detectTapGestures { dismiss() } }
                        .wrapContentSize()
                        .pointerInput(Unit) { detectTapGestures {} },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    title = { Text(if (showThemePicker) "Тема застосунку" else "Можливість не підключено") },
                    text = {
                        if (showThemePicker) ThemePreferenceOptions(themeSettings)
                        else Text(
                            unavailableAction.orEmpty(),
                            Modifier.verticalScroll(rememberScrollState())
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = dismiss) { Text("Зрозуміло") }
                    }
                )
            }
            val layout = AccountLayout.forWindow(maxWidth.value, maxHeight.value)
            Column(Modifier.fillMaxSize().testTag("account-layout-${layout.name}")) {
                Row(
                    Modifier.fillMaxWidth().testTag("account-header")
                        .heightIn(
                            min = when (layout) {
                                AccountLayout.PhoneLandscape -> 58.dp
                                AccountLayout.TabletLandscape -> 72.dp
                                else -> 70.dp
                            }
                        )
                        .padding(horizontal = if (layout.tablet) 20.dp else 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (layout.tablet) ProductWordmark(Modifier.padding(start = 8.dp, end = 32.dp))
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Назад до попереднього розділу"
                        }
                    ) {
                        AccountIcon(AccountIcon.Back, Modifier.size(22.dp))
                    }
                    Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                        SectionTitle(
                            when (section) {
                                DemoSection.Profile -> "Мій профіль"
                                DemoSection.Garage -> "Мій гараж"
                                DemoSection.VehicleDetails -> "Профіль автомобіля"
                                else -> "Налаштування"
                            }
                        )
                        Text(
                            when (section) {
                                DemoSection.Garage, DemoSection.VehicleDetails ->
                                    "Локальні демонстраційні дані · не підтверджені"
                                else -> "Деморежим · без авторизації"
                            },
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            letterSpacing = 0.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (section == DemoSection.Garage) IconButton(
                        onClick = {
                            unavailableAction = garageAddUnavailableExplanation
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Додати транспорт · не підключено"
                        }
                    ) {
                        Text("+", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    else if (layout.tablet || section == DemoSection.Settings && !layout.landscape) IconButton(
                        onClick = {
                            unavailableAction = if (layout.tablet)
                                "Це локальний деморежим. Довідковий сервіс не підключено."
                            else "Пошук у налаштуваннях не підключено."
                        },
                        modifier = Modifier.semantics {
                            contentDescription = if (layout.tablet) "Довідка про деморежим"
                            else "Пошук у налаштуваннях · не підключено"
                        }
                    ) {
                        AccountIcon(
                            if (layout.tablet) AccountIcon.Help else AccountIcon.Search,
                            Modifier.size(22.dp)
                        )
                    }
                    if (layout == AccountLayout.PhoneLandscape) ProductWordmark(Modifier.padding(end = 16.dp))
                }
                if (layout.tablet) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.weight(1f)) {
                    if (layout.landscape) BottomNavigation(
                        navigationSection, onSectionChange, rail = true,
                        compactRail = layout == AccountLayout.PhoneLandscape,
                        accountStyle = true
                    )
                    sectionStateHolder.SaveableStateProvider(section.name) {
                        Column(
                            Modifier.weight(1f).testTag("demo-account-content")
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    horizontal = when (layout) {
                                        AccountLayout.PhoneLandscape -> 12.dp
                                        AccountLayout.TabletLandscape -> 24.dp
                                        else -> 20.dp
                                    }
                                )
                                .padding(
                                    top = when (layout) {
                                        AccountLayout.TabletLandscape -> 24.dp
                                        AccountLayout.TabletPortrait -> 20.dp
                                        else -> 10.dp
                                    },
                                    bottom = 16.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                Modifier.widthIn(max = 1280.dp).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(if (layout.tablet) 16.dp else 8.dp)
                            ) {
                                    if (section == DemoSection.Garage) {
                                        GarageContent(
                                            layout = layout,
                                            currentVehicleClass = profile.vehicleClass,
                                            filter = garageFilter,
                                            onFilterChange = { garageFilterName = it.name },
                                            onSelectVehicle = onSelectVehicle,
                                            onOpenVehicleProfile = {
                                                onSectionChange(DemoSection.VehicleDetails)
                                            },
                                            unavailable = { unavailableAction = it }
                                        )
                                    } else if (section == DemoSection.VehicleDetails) {
                                        VehicleProfileDetails(demoVehicle, layout)
                                    } else if (section == DemoSection.Profile) {
                                    val accountActions: @Composable ColumnScope.() -> Unit = {
                                        ProfileOrganization(profile, layout)
                                        ProfileSecondaryActions(
                                            horizontal = layout == AccountLayout.PhonePortrait ||
                                                    layout == AccountLayout.PhoneLandscape
                                        ) { unavailableAction = it }
                                        TextButton(onClick = { onSectionChange(DemoSection.Settings) }) {
                                            Text("Відкрити налаштування")
                                        }
                                        ProfileAuthActions(onExitDemo, onRegister, onRecovery)
                                    }
                                    val identity: @Composable ColumnScope.() -> Unit = {
                                        ProfileIdentity(profile, layout) { unavailableAction = it }
                                    }
                                    val vehicle: @Composable ColumnScope.() -> Unit = {
                                        ProfileVehicle(
                                            profile,
                                            layout,
                                            onOpenGarage = { onSectionChange(DemoSection.Garage) },
                                            onOpenVehicleProfile = {
                                                onSectionChange(DemoSection.VehicleDetails)
                                            }
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
                                                ResponsiveSections(
                                                    columns = true,
                                                    first = { ProfileOrganization(profile, layout) },
                                                    second = {
                                                        ProfileSecondaryActions {
                                                            unavailableAction = it
                                                        }
                                                    }
                                                )
                                                TextButton(onClick = { onSectionChange(DemoSection.Settings) }) {
                                                    Text("Відкрити налаштування")
                                                }
                                                ProfileAuthActions(
                                                    onExitDemo,
                                                    onRegister,
                                                    onRecovery
                                                )
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
                                } else {
                                    val summary: @Composable ColumnScope.() -> Unit = {
                                        AccountCard(minHeight = 72) {
                                            Text(
                                                "Особисті налаштування",
                                                modifier = Modifier.testTag("settings-summary")
                                                    .semantics { heading() },
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                lineHeight = 19.sp,
                                                letterSpacing = 0.sp
                                            )
                                            AccountBody("Без організації, робочої ролі та синхронізації.")
                                        }
                                    }
                                    val offlineAndVersion: @Composable ColumnScope.() -> Unit = {
                                        UnavailableSettingCard(
                                            UnavailableSetting(
                                                "Офлайн-мапи",
                                                "Завантажених мап немає. Map SDK і завантаження не підключено."
                                            )
                                        )
                                        AccountBody("XRNavi $version · Mobile space")
                                        AccountBody("Демонстраційний UI. Справжні авторизація та навігація не підключені.")
                                    }
                                    val personal: @Composable ColumnScope.() -> Unit = {
                                        SettingsHeading("Особисті · локально", layout)
                                        SettingsGroup(grouped = layout == AccountLayout.PhonePortrait) {
                                            SettingsRow(
                                                "Мова інтерфейсу",
                                                "Інші локалізації та голоси не підключено.",
                                                value = "Українська",
                                                icon = if (layout == AccountLayout.PhonePortrait) AccountIcon.Language else null,
                                                grouped = layout == AccountLayout.PhonePortrait
                                            )
                                            if (layout == AccountLayout.PhonePortrait) SettingsDivider()
                                            SettingsRow(
                                                "Тема застосунку",
                                                "Застосовується до всіх екранів. Зберігається локально.",
                                                value = themeSettings.preference.label,
                                                icon = if (layout == AccountLayout.PhonePortrait) AccountIcon.Moon else null,
                                                grouped = layout == AccountLayout.PhonePortrait,
                                                onClick = { showThemePicker = true }
                                            )
                                            unavailablePersonalSettings.forEach { setting ->
                                                if (layout == AccountLayout.PhonePortrait) SettingsDivider()
                                                SettingsRow(
                                                    "${setting.title} · ${setting.status}",
                                                    setting.explanation,
                                                    icon = if (layout == AccountLayout.PhonePortrait) when (setting.title) {
                                                        "Голосові підказки" -> AccountIcon.Voice
                                                        "XR-навігація" -> AccountIcon.Xr
                                                        else -> null
                                                    } else null,
                                                    grouped = layout == AccountLayout.PhonePortrait
                                                )
                                            }
                                        }
                                    }
                                    val organization: @Composable ColumnScope.() -> Unit = {
                                        SettingsHeading("Від організації · недоступно", layout)
                                        SettingsGroup(grouped = layout == AccountLayout.PhonePortrait) {
                                            unavailableOrganizationSettings.forEachIndexed { index, setting ->
                                                if (index > 0 && layout == AccountLayout.PhonePortrait) SettingsDivider()
                                                SettingsRow(
                                                    "${setting.title} · ${setting.status}",
                                                    setting.explanation,
                                                    icon = if (layout == AccountLayout.PhonePortrait)
                                                        if (index == 0) AccountIcon.Route else AccountIcon.Speed
                                                    else null,
                                                    grouped = layout == AccountLayout.PhonePortrait,
                                                    locked = true
                                                )
                                            }
                                        }
                                    }
                                    if (layout.landscape) {
                                        ResponsiveSections(
                                            columns = true,
                                            firstWeight = if (layout.tablet) 0.315f else 0.52f,
                                            gap = if (layout.tablet) 24 else 16,
                                            first = {
                                                summary()
                                                AccountCard {
                                                    CardHeading("Особисті")
                                                    AccountBody("Можна змінювати тему")
                                                }
                                                AccountCard {
                                                    CardHeading("Від організації")
                                                    AccountBody("Параметри недоступні")
                                                }
                                                offlineAndVersion()
                                            },
                                            second = { personal(); organization() }
                                        )
                                    } else {
                                        summary()
                                        if (layout == AccountLayout.TabletPortrait) {
                                            SettingsSectionIndex()
                                            ResponsiveSections(
                                                columns = true,
                                                first = personal,
                                                second = { organization(); offlineAndVersion() }
                                            )
                                        } else {
                                            personal()
                                            organization()
                                            offlineAndVersion()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!layout.landscape) BottomNavigation(
                    navigationSection, onSectionChange, accountStyle = true,
                    horizontalLabels = layout.tablet
                )
            }
        }
    }
}

@Composable
private fun GarageContent(
    layout: AccountLayout,
    currentVehicleClass: String,
    filter: GarageFilter,
    onFilterChange: (GarageFilter) -> Unit,
    onSelectVehicle: (DemoVehicleProfile) -> Unit,
    onOpenVehicleProfile: () -> Unit,
    unavailable: (String) -> Unit
) {
    val vehicles = garageVehicles(filter)
    val list: @Composable ColumnScope.() -> Unit = {
        GarageFilterBar(filter, onFilterChange)
        GarageSectionHeading(
            text = if (filter == GarageFilter.All) "Доступний демопрофіль"
            else "${filter.label} транспорт"
        )
        if (vehicles.isEmpty()) {
            GarageEmptyState(filter)
        } else {
            AccountCard(tonal = true) {
                CardHeading(demoVehicle.name)
                AccountBody(
                    if (currentVehicleClass == demoVehicle.category)
                        "Поточний вибір для демомаршруту"
                    else "Доступний легковий демопрофіль"
                )
                AccountBody("Це демонстраційний транспорт, не підтвердження власності.")
            }
        }
    }
    val preview: @Composable ColumnScope.() -> Unit = {
        if (vehicles.isNotEmpty()) {
            GarageVehicleCard(
                vehicle = demoVehicle,
                layout = layout,
                selected = currentVehicleClass == demoVehicle.category,
                onSelect = { onSelectVehicle(demoVehicle) },
                onDetails = onOpenVehicleProfile
            )
        }
        Button(
            onClick = {
                unavailable(garageAddUnavailableExplanation)
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Додати транспорт · не підключено", fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        AccountBody(
            garageCorporateUnavailableExplanation
        )
    }

    if (layout.landscape) {
        ResponsiveSections(
            columns = true,
            firstWeight = if (layout.tablet) 0.39f else 0.43f,
            gap = if (layout.tablet) 24 else 12,
            first = list,
            second = preview
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GarageFilterBar(filter, onFilterChange)
            GarageSectionHeading(
                text = if (filter == GarageFilter.All) "Доступний демопрофіль"
                else "${filter.label} транспорт"
            )
            if (vehicles.isEmpty()) {
                GarageEmptyState(filter)
            } else {
                GarageVehicleCard(
                    vehicle = demoVehicle,
                    layout = layout,
                    selected = currentVehicleClass == demoVehicle.category,
                    onSelect = { onSelectVehicle(demoVehicle) },
                    onDetails = onOpenVehicleProfile
                )
            }
            Button(
                onClick = {
                    unavailable(garageAddUnavailableExplanation)
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Додати транспорт · не підключено", fontSize = 14.sp, textAlign = TextAlign.Center)
            }
            AccountBody(
                garageCorporateUnavailableExplanation
            )
        }
    }
}

@Composable
private fun GarageFilterBar(
    selected: GarageFilter,
    onSelected: (GarageFilter) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accountSurfaceColor())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GarageFilter.entries.forEach { filter ->
            val active = filter == selected
            Box(
                Modifier.weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .clickable(role = Role.Tab) { onSelected(filter) }
                    .semantics {
                        this.selected = active
                        contentDescription = filter.label
                    }
                    .testTag("garage-filter-${filter.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    filter.label,
                    modifier = Modifier.padding(horizontal = 2.dp),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (active) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun GarageSectionHeading(text: String) {
    Text(
        text,
        fontFamily = rememberRobotoCondensedFontFamily(),
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun GarageEmptyState(filter: GarageFilter) {
    AccountCard(tonal = true) {
        CardHeading("Немає сумісних демопрофілів")
        AccountBody(
            "Для категорії «${filter.label}» локальних демопрофілів немає. " +
                "Ford Mustang GT є легковим і не відображається як сумісний транспорт."
        )
    }
}

@Composable
private fun GarageVehicleCard(
    vehicle: DemoVehicleProfile,
    layout: AccountLayout,
    selected: Boolean,
    onSelect: () -> Unit,
    onDetails: () -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("garage-profile-card"),
        shape = RoundedCornerShape(12.dp),
        color = accountSurfaceColor(),
        border = BorderStroke(
            width = if (selected) 1.dp else 0.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column {
            Box {
                Image(
                    painterResource(
                        if (dark) Res.drawable.profile_mustang_hero_dark
                        else Res.drawable.profile_mustang_hero_light
                    ),
                    contentDescription = "Фото демопрофілю ${vehicle.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(
                        when (layout) {
                            AccountLayout.PhonePortrait -> 1.8f
                            AccountLayout.PhoneLandscape -> 1.9f
                            AccountLayout.TabletPortrait -> 2.45f
                            AccountLayout.TabletLandscape -> 1.85f
                        }
                    )
                )
                Box(
                    Modifier.fillMaxWidth().heightIn(min = 82.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            vehicle.name,
                            color = Color.White,
                            fontFamily = rememberRobotoCondensedFontFamily(),
                            fontSize = if (layout.tablet) 26.sp else 22.sp,
                            lineHeight = if (layout.tablet) 32.sp else 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Демо-профіль · характеристики не перевірені",
                            color = Color.White.copy(alpha = 0.84f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Surface(
                    Modifier.align(Alignment.TopStart).padding(10.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                ) {
                    Text(
                        if (selected) "ОБРАНО ДЛЯ ДЕМОМАРШРУТУ" else "ДОСТУПНИЙ ДЕМОПРОФІЛЬ",
                        Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountBody("Категорія: ${vehicle.category}")
                AccountBody("Демо-характеристики, не перевірені: ${vehicle.specification}")
                TextButton(
                    onClick = onDetails,
                    modifier = Modifier.align(Alignment.End).heightIn(min = 44.dp)
                        .semantics { contentDescription = "Переглянути профіль ${vehicle.name}" }
                ) {
                    Text("Переглянути профіль")
                }
                Button(
                    onClick = onSelect,
                    enabled = !selected,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("garage-select-profile"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        if (selected) "Обрано для демомаршруту"
                        else "Обрати ${vehicle.name}",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ResponsiveSections(
    columns: Boolean,
    firstWeight: Float = 0.5f,
    gap: Int = 16,
    first: @Composable ColumnScope.() -> Unit,
    second: @Composable ColumnScope.() -> Unit
) {
    if (columns) {
        Row(horizontalArrangement = Arrangement.spacedBy(gap.dp)) {
            Column(
                Modifier.weight(firstWeight).testTag("account-sections-first"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = first
            )
            Column(
                Modifier.weight(1f - firstWeight).testTag("account-sections-second"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = second
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            first()
            second()
        }
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
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
                Image(
                    painterResource(
                        if (dark) Res.drawable.profile_mustang_hero_dark
                        else Res.drawable.profile_mustang_hero_light
                    ),
                    contentDescription = "Демонстраційний Ford Mustang GT",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(
                        when (layout) {
                            AccountLayout.TabletPortrait -> 3.93f
                            AccountLayout.TabletLandscape -> 1.95f
                            AccountLayout.PhoneLandscape -> 3f
                            AccountLayout.PhonePortrait -> 3.48f
                        }
                    )
                )
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
                            .semantics { contentDescription = "Переглянути профіль ${demoVehicle.name}" }
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
                    demoVehicle.name,
                    fontSize = if (layout.tablet) 32.sp else 22.sp,
                    lineHeight = if (layout.tablet) 40.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp,
                    fontFamily = rememberRobotoCondensedFontFamily()
                )
                AccountBody(demoVehicle.specification)
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
private fun VehicleProfileDetails(vehicle: DemoVehicleProfile, layout: AccountLayout) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    val overview: @Composable ColumnScope.() -> Unit = {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accountSurfaceColor(),
            modifier = Modifier.fillMaxWidth().testTag("vehicle-profile-details")
        ) {
            Column {
                Image(
                    painterResource(
                        if (dark) Res.drawable.profile_mustang_hero_dark
                        else Res.drawable.profile_mustang_hero_light
                    ),
                    contentDescription = "Демонстраційне фото ${vehicle.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(
                        when (layout) {
                            AccountLayout.PhonePortrait -> 1.8f
                            AccountLayout.PhoneLandscape -> 1.9f
                            AccountLayout.TabletPortrait -> 2.45f
                            AccountLayout.TabletLandscape -> 1.85f
                        }
                    )
                )
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionTitle(vehicle.name, fontSize = if (layout.tablet) 30 else 24)
                    AccountBody("Особистий демопрофіль · не підтверджено зовнішнім сервісом")
                    AccountBody("Це демонстраційний транспорт, а не підтвердження права власності.")
                }
            }
        }
    }
    val data: @Composable ColumnScope.() -> Unit = {
        AccountCard {
            CardHeading("Дані з локальної моделі")
            VehicleProfileFact("Категорія", vehicle.category)
            VehicleProfileFact("Характеристики", vehicle.specification)
            AccountBody(
                "Модель не уточнює, яке саме габаритне вимірювання позначене як 1,4 м, " +
                    "і не визначає 1,8 т як максимальну допустиму масу."
            )
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
private fun ProfileSecondaryActions(horizontal: Boolean = false, unavailable: (String) -> Unit) {
    val security: @Composable () -> Unit = {
        AccountAction("Безпека та особисті дані", "Не підключено") {
            unavailable("Керування даними акаунта й видалення профілю не підключено. Демо-профіль не є акаунтом.")
        }
    }
    val favorites: @Composable () -> Unit = {
        AccountAction("Обране", "Немає даних") {
            unavailable("Сервіс обраного не підключено. Збережених даних немає.")
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
        AccountBody("Ці сценарії завершують перегляд деморежиму. Серверна авторизація не підключена.")
        TextButton(onClick = onExit) { Text("Вийти з деморежиму") }
        TextButton(onClick = onRegister) { Text("Відкрити реєстрацію") }
        TextButton(onClick = onRecovery) { Text("Відновлення пароля") }
    }
}

@Composable
private fun ThemePreferenceOptions(model: ThemeSettingsViewModel) {
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AccountBody("Застосовується до всіх екранів і зберігається лише на цьому пристрої. Системна тема не змінюється.")
        Column(Modifier.selectableGroup()) {
            ThemePreference.entries.forEach { theme ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .selectable(
                            selected = model.preference == theme,
                            role = Role.RadioButton,
                            onClick = { model.select(theme) }
                        ).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = model.preference == theme, onClick = null)
                    Text(theme.label, Modifier.padding(start = 12.dp))
                }
            }
        }
        if (model.notice.isNotEmpty()) AuthInfo(model.notice)
    }
}

@Composable
private fun SettingsGroup(grouped: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (grouped) accountSurfaceColor() else Color.Transparent
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (grouped) 0.dp else 12.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
}

@Composable
private fun SettingsRow(
    title: String,
    description: String,
    value: String? = null,
    grouped: Boolean = true,
    locked: Boolean = false,
    icon: AccountIcon? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().testTag("settings-row-$title")
            .heightIn(min = if (grouped) 65.dp else 48.dp)
            .clip(RoundedCornerShape(if (grouped) 0.dp else 12.dp))
            .background(accountSurfaceColor())
            .then(
                if (onClick != null) Modifier.clickable(
                    role = Role.Button,
                    onClick = onClick
                ) else Modifier
            )
            .padding(horizontal = if (grouped) 14.dp else 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) AccountIcon(
            icon, Modifier.size(20.dp),
            tint = if (locked) accountPolicyColor() else null
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title, fontSize = 13.sp, lineHeight = 16.sp,
                letterSpacing = 0.sp, fontWeight = FontWeight.Medium
            )
            Text(
                description,
                fontSize = 11.sp,
                lineHeight = if (grouped) 14.sp else 16.sp,
                letterSpacing = 0.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (value != null) Text(
            value,
            fontSize = if (grouped) 11.sp else 12.sp,
            lineHeight = if (grouped) 13.sp else 16.sp,
            letterSpacing = 0.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (onClick != null) AccountIcon(
            AccountIcon.ChevronRight,
            Modifier.size(16.dp)
        )
        if (locked) AccountIcon(
            AccountIcon.Lock,
            Modifier.size(15.dp),
            contentDescription = "Корпоративний параметр недоступний"
        )
    }
}

@Composable
private fun UnavailableSettingCard(setting: UnavailableSetting) {
    SettingsRow(
        "${setting.title} · ${setting.status}", setting.explanation,
        grouped = false, icon = AccountIcon.Download
    )
}

@Composable
private fun AccountAction(title: String, status: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(10.dp), color = accountTonalColor()) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp)
                Text(
                    status, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AccountIcon(AccountIcon.ChevronRight, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AccountCard(
    tonal: Boolean = false,
    minHeight: Int = 0,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (tonal) accountTonalColor() else accountSurfaceColor()
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = minHeight.dp).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun SectionTitle(text: String, fontSize: Int = 24, lineHeight: Int = 30) {
    Text(
        text, fontFamily = rememberRobotoCondensedFontFamily(), fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp, lineHeight = lineHeight.sp,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun SettingsHeading(text: String, layout: AccountLayout) {
    if (layout == AccountLayout.PhonePortrait) {
        Text(
            text, fontWeight = FontWeight.Medium, fontSize = 11.sp,
            lineHeight = 13.sp, letterSpacing = 0.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() }
        )
    } else {
        CardHeading(text)
    }
}

@Composable
private fun CardHeading(text: String) {
    Text(
        text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
        lineHeight = 20.sp, letterSpacing = 0.1.sp,
        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun AccountBody(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun accountSurfaceColor(): Color =
    if (MaterialTheme.colorScheme.background.red < 0.5f) Color(0xFF070D17)
    else MaterialTheme.colorScheme.surface

@Composable
private fun accountTonalColor(): Color =
    if (MaterialTheme.colorScheme.background.red < 0.5f) accountSurfaceColor()
    else MaterialTheme.colorScheme.surfaceVariant

@Composable
private fun SettingsSectionIndex() {
    Surface(shape = RoundedCornerShape(8.dp), color = accountSurfaceColor()) {
        Row(Modifier.fillMaxWidth().padding(4.dp)) {
            listOf("Особисті", "Від організації", "Офлайн-мапи").forEach { text ->
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(4.dp))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() }
                    )
                }
            }
        }
    }
}
