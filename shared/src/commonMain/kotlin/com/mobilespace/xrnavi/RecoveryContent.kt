package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun RecoveryContent(
    model: RecoveryViewModel,
    previewEnabled: Boolean,
    onBack: () -> Unit,
    onPreview: () -> Unit
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val stopEditing = {
        focus.clearFocus()
        keyboard?.hide()
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (model.stage != RecoveryStage.Request) AuthInfo(recoveryPreviewMessage)
        Text(
            if (model.stage == RecoveryStage.Request) model.stage.title else "${model.stage.title} · демо",
            fontFamily = rememberRobotoCondensedFontFamily(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        when (model.stage) {
            RecoveryStage.Request -> {
                RecoveryBody("Вкажіть електронну пошту для запиту відновлення. У цій версії надсилання листів ще не підключено.")
                AuthFormInput(
                    label = "Електронна пошта",
                    value = model.email,
                    onValueChange = model::updateEmail,
                    error = model.emailError,
                    onBlur = model::touchEmail,
                    leadingIcon = {
                        Icon(
                            painterResource(
                                if (MaterialTheme.colorScheme.background.red < 0.5f)
                                    Res.drawable.sign_in_mail_dark else Res.drawable.sign_in_mail_light
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        stopEditing()
                        model.requestReset()
                    })
                )
                RecoveryBody("Після підключення сервісу відповідь буде однаковою незалежно від того, чи існує акаунт із цією адресою.")
                RecoveryAction("Надіслати посилання", model.canRequest) {
                    stopEditing()
                    model.requestReset()
                }
            }

            RecoveryStage.CheckEmail -> {
                RecoveryBody("Демонстрація екрана очікування листа. Запит не надсилався, існування акаунта не перевірялося.")
                RecoveryStatusIcon(
                    if (MaterialTheme.colorScheme.background.red < 0.5f)
                        Res.drawable.sign_in_mail_dark else Res.drawable.sign_in_mail_light
                )
                AuthInfo("У підключеному сценарії: не бачите листа — перевірте папку «Спам».")
                RecoveryAction("Відкрити пошту") { model.openMailPreview() }
                TextButton(onClick = { stopEditing(); model.returnToRequest() }) { Text("Змінити адресу") }
                TextButton(onClick = model::resend) { Text("Запросити лист повторно") }
            }

            RecoveryStage.NewPassword -> {
                RecoveryBody("Демонстрація форми нового пароля. Перевіреного посилання немає; зміна пароля недоступна.")
                RecoveryPasswords(model)
                RecoveryBody("Мінімум 8 символів.")
                RecoveryAction("Зберегти новий пароль", model.canReset) {
                    stopEditing()
                    model.submitNewPassword()
                }
            }

            RecoveryStage.Changed -> {
                RecoveryBody("Це лише демонстрація стану S44. Насправді пароль не змінено, акаунт і сесію не створено.")
                RecoveryStatusIcon(Res.drawable.recovery_check)
                RecoveryAction("Перейти до входу", onClick = onBack)
            }

            RecoveryStage.InvalidLink -> {
                RecoveryBody("Демонстраційний стан: посилання недійсне, використане або прострочене. Жодного реального токена не отримували й не перевіряли.")
                RecoveryStatusIcon(Res.drawable.recovery_link_off, warning = true)
                RecoveryAction("Запросити нове посилання") {
                    stopEditing()
                    model.returnToRequest()
                }
            }
        }
        if (model.notice.isNotEmpty()) AuthInfo(model.notice)
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Повернутися до входу")
        }
        if (previewEnabled) {
            TextButton(onClick = onPreview, modifier = Modifier.fillMaxWidth()) {
                Text("Внутрішній preview S41–S45 · debug", fontSize = 12.sp)
            }
        }
    }
}
