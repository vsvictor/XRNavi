package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import org.jetbrains.compose.resources.DrawableResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun RecoveryScreen(
    model: RecoveryViewModel,
    previewEnabled: Boolean,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var previewMenu by remember { mutableStateOf(false) }
    val back = {
        focus.clearFocus()
        keyboard?.hide()
        onBack()
    }
    AuthBackHandler(onBack = back)
    if (previewEnabled && previewMenu) {
        AlertDialog(
            onDismissRequest = { previewMenu = false },
            title = { Text("Внутрішній preview S41–S45") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(recoveryPreviewMessage, fontSize = 12.sp)
                    RecoveryStage.entries.forEach { stage ->
                        TextButton(onClick = {
                            focus.clearFocus()
                            keyboard?.hide()
                            model.openPreview(stage)
                            previewMenu = false
                        }) { Text("${stage.code} · ${stage.title}") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { previewMenu = false }) { Text("Закрити") }
            }
        )
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val split = maxWidth >= 600.dp && maxWidth > maxHeight
        val tablet = minOf(maxWidth, maxHeight) >= 600.dp
        val density = LocalDensity.current
        val imeHeight = with(density) { WindowInsets.ime.getBottom(this).toDp() }
        val short = maxHeight - imeHeight < 240.dp
        Column(Modifier.fillMaxSize().imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = back,
                    modifier = Modifier.semantics { contentDescription = "Повернутися до входу" }
                ) { Text("‹", fontSize = 28.sp) }
                Column(Modifier.weight(1f).padding(vertical = if (short) 0.dp else 8.dp)) {
                    ProductWordmark()
                    if (!short) {
                        Text(
                            "Mobile space",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onToggleTheme) { Text("Тема") }
            }
            key(model.stage) {
                val scroll = rememberScrollState()
                if (split) {
                    Row(
                        Modifier.weight(1f).padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!short) {
                            RecoveryPhoto(
                                Modifier.weight(1f).fillMaxHeight().padding(vertical = 24.dp)
                            )
                        }
                        Column(
                            Modifier.weight(1f).fillMaxHeight().verticalScroll(scroll)
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            RecoveryContent(model, previewEnabled, back) { previewMenu = true }
                        }
                    }
                } else {
                    Column(
                        Modifier.weight(1f).verticalScroll(scroll)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!short) {
                            RecoveryPhoto(
                                Modifier.fillMaxWidth().height(if (tablet) 270.dp else 176.dp)
                            )
                            Spacer(Modifier.height(28.dp))
                        }
                        Column(
                            Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(bottom = 24.dp)
                        ) {
                            RecoveryContent(model, previewEnabled, back) { previewMenu = true }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveryPhoto(modifier: Modifier) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    Image(
        painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(16.dp))
    )
}

@Composable
private fun RecoveryContent(
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

@Composable
private fun RecoveryStatusIcon(resource: DrawableResource, warning: Boolean = false) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (warning) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(resource),
                contentDescription = null,
                tint = if (warning) androidx.compose.ui.graphics.Color(
                    if (dark) 0xFFFFC95C else 0xFF8A5C00
                ) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun RecoveryBody(text: String) {
    Text(
        text,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun RecoveryAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) { Text(text) }
}

@Composable
private fun RecoveryPasswords(model: RecoveryViewModel) {
    val confirmationFocus = remember { FocusRequester() }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    AuthFormInput(
        label = "Новий пароль",
        value = model.password,
        onValueChange = model::updatePassword,
        error = model.passwordError,
        onBlur = model::touchPassword,
        secret = true,
        visible = model.passwordVisible,
        onToggleVisibility = model::togglePasswordVisibility,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { confirmationFocus.requestFocus() })
    )
    AuthFormInput(
        label = "Підтвердіть пароль",
        value = model.confirmation,
        onValueChange = model::updateConfirmation,
        error = model.confirmationError,
        onBlur = model::touchConfirmation,
        modifier = Modifier.focusRequester(confirmationFocus),
        secret = true,
        visible = model.confirmationVisible,
        onToggleVisibility = model::toggleConfirmationVisibility,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); keyboard?.hide() })
    )
}
