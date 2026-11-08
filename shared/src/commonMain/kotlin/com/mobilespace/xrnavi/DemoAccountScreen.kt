package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Host of the demo account area: header, navigation, dialogs and the active section screen. */
@Composable
internal fun DemoAccountScreen(
    section: DemoSection,
    profile: DemoProfileState,
    configurationSelections: Map<String, String>,
    themeSettings: ThemeSettingsViewModel,
    onSectionChange: (DemoSection) -> Unit,
    savedPlacesCount: Int,
    onOpenSavedPlaces: () -> Unit,
    onBack: () -> Unit,
    onSelectVehicle: (DemoVehicleProfile) -> Unit,
    onApplyVehicleConfiguration: (DemoVehicleConfiguration) -> Unit,
    onExitDemo: () -> Unit,
    onRegister: () -> Unit,
    onRecovery: () -> Unit,
    onOpenAdditional: ((AdditionalScreen) -> Unit)? = null
) {
    val account = viewModel { DemoAccountViewModel() }.bloc
    val garage = viewModel { DemoGarageViewModel() }.bloc
    val configurations = viewModel { DemoVehicleConfigurationsViewModel() }.bloc
    val accountState by account.state.collectAsStateWithLifecycle()
    val garageState by garage.state.collectAsStateWithLifecycle()
    val configurationsState by configurations.state.collectAsStateWithLifecycle()

    val viewedVehicleId = accountState.viewedVehicleId ?: profile.vehicleId
    val viewedVehicle = demoVehicleById(viewedVehicleId) ?: profile.selectedVehicle ?: demoVehicle
    val appliedConfigurationId = configurationSelections[viewedVehicle.id]
        ?: defaultVehicleConfigurationId(viewedVehicle.id).orEmpty()
    LaunchedEffect(viewedVehicle.id, appliedConfigurationId) {
        configurations.dispatch(
            DemoVehicleConfigurationsEvent.Sync(viewedVehicle.id, appliedConfigurationId)
        )
    }
    val draft = configurationsState.draft
    val draftConfigurationId = if (
        draft.vehicleId == viewedVehicle.id && draft.appliedConfigurationId == appliedConfigurationId
    ) draft.selectedConfigurationId.orEmpty() else appliedConfigurationId
    LaunchedEffect(section) { account.dispatch(DemoAccountEvent.DismissDialog) }
    DisposableEffect(Unit) {
        onDispose { account.dispatch(DemoAccountEvent.Left) }
    }

    val handleBack = {
        if (section == DemoSection.VehicleConfigurations) {
            configurations.dispatch(DemoVehicleConfigurationsEvent.Cancel)
        }
        onBack()
    }
    AuthBackHandler(handleBack)
    val sectionStateHolder = rememberSaveableStateHolder()
    val navigationSection = if (
        section == DemoSection.VehicleDetails || section == DemoSection.VehicleConfigurations
    ) {
        DemoSection.Garage
    } else {
        section
    }
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (accountState.notice != null || accountState.themePickerVisible) {
                val dismiss = { account.dispatch(DemoAccountEvent.DismissDialog) }
                AlertDialog(
                    onDismissRequest = dismiss,
                    // Keep the layer's measurement in host coordinates, including after rotation.
                    modifier = Modifier.requiredSize(maxWidth, maxHeight)
                        .pointerInput(Unit) { detectTapGestures { dismiss() } }
                        .wrapContentSize()
                        .pointerInput(Unit) { detectTapGestures {} },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    title = {
                        Text(
                            if (accountState.themePickerVisible) "Тема застосунку"
                            else "Можливість не підключено"
                        )
                    },
                    text = {
                        if (accountState.themePickerVisible) ThemePreferenceOptions(
                            themeSettings,
                            Modifier.verticalScroll(rememberScrollState())
                        )
                        else Text(
                            accountState.notice.orEmpty(),
                            Modifier.verticalScroll(rememberScrollState())
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = dismiss) { Text("Зрозуміло") }
                    }
                )
            }
            val layout = AccountLayout.forWindow(maxWidth.value, maxHeight.value)
            val unavailable = { text: String -> account.dispatch(DemoAccountEvent.Unavailable(text)) }
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
                        onClick = handleBack,
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
                                DemoSection.VehicleConfigurations -> "Конфігурації"
                                else -> "Налаштування"
                            }
                        )
                        Text(
                            when (section) {
                                DemoSection.Garage, DemoSection.VehicleDetails,
                                DemoSection.VehicleConfigurations ->
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
                            if (onOpenAdditional != null) onOpenAdditional(AdditionalScreen.VehicleCatalog)
                            else unavailable(garageAddUnavailableExplanation)
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Додати транспорт · не підключено"
                        }
                    ) {
                        Text("+", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    else if (layout.tablet || section == DemoSection.Settings && !layout.landscape) IconButton(
                        onClick = {
                            unavailable(
                                if (layout.tablet) "Це локальний деморежим. Довідковий сервіс не підключено."
                                else "Пошук у налаштуваннях не підключено."
                            )
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
                                when (section) {
                                    DemoSection.Garage -> DemoGarageScreen(
                                        layout = layout,
                                        currentVehicleId = profile.vehicleId,
                                        filter = garageState.filter,
                                        onFilterChange = { garage.dispatch(DemoGarageEvent.Filter(it)) },
                                        onSelectVehicle = onSelectVehicle,
                                        onOpenVehicleProfile = { vehicle ->
                                            account.dispatch(DemoAccountEvent.ViewVehicle(vehicle.id))
                                            onSectionChange(DemoSection.VehicleDetails)
                                        },
                                        unavailable = {
                                            if (it == garageAddUnavailableExplanation && onOpenAdditional != null)
                                                onOpenAdditional(AdditionalScreen.VehicleCatalog)
                                            else unavailable(it)
                                        }
                                    )

                                    DemoSection.VehicleDetails -> DemoVehicleDetailsScreen(
                                        viewedVehicle,
                                        layout,
                                        onOpenConfigurations = {
                                            onSectionChange(DemoSection.VehicleConfigurations)
                                        }
                                    )

                                    DemoSection.VehicleConfigurations -> DemoVehicleConfigurationsScreen(
                                        vehicle = viewedVehicle,
                                        configurations = vehicleConfigurationsFor(viewedVehicle.id),
                                        layout = layout,
                                        appliedConfigurationId = appliedConfigurationId,
                                        draftConfigurationId = draftConfigurationId,
                                        canApply = viewedVehicle.id == profile.vehicleId,
                                        onDraftChange = {
                                            configurations.dispatch(DemoVehicleConfigurationsEvent.Select(it))
                                        },
                                        onApply = onApplyVehicleConfiguration,
                                        onReturnToGarage = { onSectionChange(DemoSection.Garage) },
                                        onCancel = handleBack
                                    )

                                    DemoSection.Profile -> DemoProfileScreen(
                                        profile = profile,
                                        layout = layout,
                                        savedPlacesCount = savedPlacesCount,
                                        onOpenSavedPlaces = onOpenSavedPlaces,
                                        onOpenGarage = { onSectionChange(DemoSection.Garage) },
                                        onOpenVehicleProfile = {
                                            account.dispatch(DemoAccountEvent.ViewVehicle(profile.vehicleId))
                                            onSectionChange(DemoSection.VehicleDetails)
                                        },
                                        onOpenSettings = { onSectionChange(DemoSection.Settings) },
                                        onExitDemo = onExitDemo,
                                        onRegister = onRegister,
                                        onRecovery = onRecovery,
                                        onOpenAdditional = onOpenAdditional,
                                        unavailable = unavailable
                                    )

                                    else -> DemoSettingsScreen(
                                        layout = layout,
                                        themeSettings = themeSettings,
                                        onShowThemePicker = {
                                            account.dispatch(DemoAccountEvent.ShowThemePicker)
                                        },
                                        onOpenAdditional = onOpenAdditional
                                    )
                                }
                            }
                        }
                    }
                }
                if (!layout.landscape) BottomNavigation(
                    navigationSection, onSectionChange
                )
            }
        }
    }
}
