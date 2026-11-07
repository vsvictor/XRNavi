package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ColumnScope.DemoSettingsScreen(
    layout: AccountLayout,
    themeSettings: ThemeSettingsViewModel,
    onShowThemePicker: () -> Unit,
    onOpenAdditional: ((AdditionalScreen) -> Unit)?
) {
    val version = rememberAppVersion()
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
        if (onOpenAdditional != null) AccountAction(
            "Офлайн-мапи", "Відкрити · завантаження не підключено"
        ) { onOpenAdditional(AdditionalScreen.OfflineMaps) }
        else UnavailableSettingCard(
            UnavailableSetting(
                "Офлайн-мапи",
                "Завантажених мап немає. Map SDK і завантаження не підключено."
            )
        )
        AdditionalAccountActions(
            listOf(AdditionalScreen.Company, AdditionalScreen.Diagnostics), onOpenAdditional
        )
        AccountBody("XRNavi $version · Mobile space")
        AccountBody("Демонстраційний UI. Дані акаунта та справжня навігація не підключені.")
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
                onClick = onShowThemePicker
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

@Composable
internal fun ThemePreferenceOptions(
    model: ThemeSettingsViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
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
