package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
        AccountBody("Демонстраційний UI. Дані аккаунта та справжня навігація не підключені.")
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
