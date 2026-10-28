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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onExitDemo: () -> Unit,
    onRegister: () -> Unit,
    onRecovery: () -> Unit
) {
    AuthBackHandler(onBack)
    var unavailableAction by remember(section) { mutableStateOf<String?>(null) }
    var showThemePicker by rememberSaveable { mutableStateOf(false) }
    val version = rememberAppVersion()
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
                        SectionTitle(if (section == DemoSection.Profile) "Мій профіль" else "Налаштування")
                        Text(
                            "Деморежим · без авторизації",
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            letterSpacing = 0.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (layout.tablet || section == DemoSection.Settings && !layout.landscape) IconButton(
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
                        section, onSectionChange, rail = true,
                        compactRail = layout == AccountLayout.PhoneLandscape,
                        accountStyle = true
                    )
                    key(section) {
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
                                if (section == DemoSection.Profile) {
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
                                        ProfileVehicle(profile, layout) { unavailableAction = it }
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
                    section, onSectionChange, accountStyle = true,
                    horizontalLabels = layout.tablet
                )
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
    unavailable: (String) -> Unit
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
            onClick = { unavailable("Повний гараж і редагування автомобілів не підключено. Наявний лише легковий демопрофіль Ford Mustang GT.") },
            modifier = Modifier.semantics {
                contentDescription = "Керувати гаражем · не підключено"
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
                            .clickable(role = Role.Button) {
                            unavailable("Деталі авто й редагування не підключено. ${demoVehicle.name}: ${demoVehicle.specification}. Це демонстраційний профіль, не підтверджене володіння.")
                            }.semantics { contentDescription = "Деталі демоавто · не підключено" }
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
