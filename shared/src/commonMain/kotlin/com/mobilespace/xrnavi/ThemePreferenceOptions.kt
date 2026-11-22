package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

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
