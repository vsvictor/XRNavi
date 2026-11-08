package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.*

@Composable
internal fun RegistrationForm(
    model: RegistrationViewModel,
    modifier: Modifier,
    onBack: () -> Unit
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val fieldFocus = remember { RegistrationField.entries.associateWith { FocusRequester() } }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RegistrationMembership.entries.forEach { membership ->
                val chosen = membership == model.membership
                TextButton(
                    onClick = { model.selectMembership(membership) },
                    modifier = Modifier.weight(1f).padding(4.dp)
                        .background(
                            if (chosen) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        ).semantics {
                            selected = chosen
                            role = Role.Tab
                        }
                ) { Text(membership.label, fontSize = 12.sp) }
            }
        }
        if (model.membership == RegistrationMembership.Corporate) {
            AuthInfo(invitationUnavailableMessage)
        }
        RegistrationInput(model, RegistrationField.Name, "Ім’я та прізвище", model.name, fieldFocus)
        RegistrationInput(
            model,
            RegistrationField.Email,
            "Електронна пошта",
            model.email,
            fieldFocus
        )
        RegistrationInput(model, RegistrationField.Password, "Пароль", model.password, fieldFocus)
        if (model.visibleError(RegistrationField.Password) == null) {
            Text(
                "Пароль · від 8 символів",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RegistrationInput(
            model,
            RegistrationField.Confirmation,
            "Підтвердіть пароль",
            model.confirmation,
            fieldFocus
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = model.consent,
                onCheckedChange = model::updateConsent,
                modifier = Modifier.semantics {
                    contentDescription =
                        "Згода з Умовами користування та Політикою конфіденційності"
                }
            )
            Text(
                "Погоджуюся з Умовами користування та Політикою конфіденційності.",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = {
                focus.clearFocus()
                keyboard?.hide()
                model.submit()
            },
            enabled = model.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(26.dp)
        ) { Text("Реєстрація") }
        if (model.notice.isNotEmpty()) {
            AuthInfo(model.notice)
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Уже маєте акаунт? Увійти")
        }
    }
}
