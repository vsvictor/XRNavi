package com.mobilespace.xrnavi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var showDocuments by remember { mutableStateOf(false) }
    val back = {
        focus.clearFocus()
        keyboard?.hide()
        onBack()
    }
    AuthBackHandler(onBack = back)
    if (showDocuments) {
        AlertDialog(
            onDismissRequest = { showDocuments = false },
            title = { Text("Документи недоступні") },
            text = { Text(registrationDocumentsMessage) },
            confirmButton = {
                TextButton(onClick = { showDocuments = false }) { Text("Зрозуміло") }
            }
        )
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val split = maxWidth >= 600.dp && maxWidth > maxHeight
        val tablet = minOf(maxWidth, maxHeight) >= 600.dp
        val density = LocalDensity.current
        val keyboardHeight = with(density) { WindowInsets.ime.getBottom(this).toDp() }
        val compactHeader = maxHeight - keyboardHeight < 240.dp
        val scroll = rememberScrollState()
        Column(Modifier.fillMaxSize().imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = back, modifier = Modifier.semantics {
                    contentDescription = "Повернутися до входу"
                }) { Text("‹", fontSize = 28.sp) }
                Column(Modifier.weight(1f).padding(vertical = if (compactHeader) 0.dp else 8.dp)) {
                    Text(
                        "Створити акаунт",
                        fontFamily = rememberRobotoCondensedFontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compactHeader) 22.sp else 26.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!compactHeader) {
                        Text(
                            "Ваш шлях починається тут",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onToggleTheme) { Text("Тема") }
            }
            if (split) {
                Row(
                    Modifier.weight(1f).padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    if (!compactHeader) {
                        RegistrationHero(
                            Modifier.weight(1f).fillMaxHeight().padding(bottom = 16.dp),
                            compact = false
                        )
                    }
                    RegistrationForm(
                        viewModel,
                        Modifier.weight(1f).fillMaxHeight().verticalScroll(scroll)
                            .padding(bottom = 24.dp),
                        onDocuments = { showDocuments = true },
                        onBack = back
                    )
                }
            } else {
                Column(
                    Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RegistrationHero(
                        Modifier.fillMaxWidth().height(if (tablet) 260.dp else 110.dp),
                        compact = !tablet
                    )
                    Spacer(Modifier.height(16.dp))
                    RegistrationForm(
                        viewModel,
                        Modifier.widthIn(max = 540.dp).fillMaxWidth().padding(bottom = 24.dp),
                        onDocuments = { showDocuments = true },
                        onBack = back
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrationHero(modifier: Modifier, compact: Boolean) {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    Box(modifier.clip(RoundedCornerShape(16.dp))) {
        Image(
            painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                if (compact) {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                }
            )
        )
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!compact) ProductWordmark()
            Column {
                Text(
                    "БІЛЬШЕ, НІЖ ДОРОГА.",
                    fontFamily = rememberRobotoCondensedFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 22.sp else 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Один профіль. Усі ваші автомобілі.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Mobile space · XRNavi",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RegistrationForm(
    model: RegistrationViewModel,
    modifier: Modifier,
    onDocuments: () -> Unit,
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
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
        TextButton(onClick = onDocuments) {
            Text(
                "Умови та конфіденційність · недоступні",
                fontSize = 12.sp
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
        ) { Text("Створити акаунт") }
        if (model.notice.isNotEmpty()) {
            AuthInfo(model.notice)
        }
        if (model.membership == RegistrationMembership.Personal) {
            AuthInfo(invitationUnavailableMessage)
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Уже маєте акаунт? Увійти")
        }
    }
}

@Composable
private fun RegistrationInput(
    model: RegistrationViewModel,
    field: RegistrationField,
    label: String,
    value: String,
    fieldFocus: Map<RegistrationField, FocusRequester>
) {
    val secret = field == RegistrationField.Password || field == RegistrationField.Confirmation
    val visible =
        if (field == RegistrationField.Password) model.passwordVisible else model.confirmationVisible
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    AuthFormInput(
        label = label,
        value = value,
        onValueChange = { model.update(field, it) },
        error = model.visibleError(field),
        onBlur = { model.touch(field) },
        modifier = Modifier.focusRequester(fieldFocus.getValue(field)),
        secret = secret,
        visible = visible,
        onToggleVisibility = {
            if (field == RegistrationField.Password) model.togglePasswordVisibility()
            else model.toggleConfirmationVisibility()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                secret -> KeyboardType.Password
                field == RegistrationField.Email -> KeyboardType.Email
                else -> KeyboardType.Text
            },
            capitalization = if (field == RegistrationField.Name) KeyboardCapitalization.Words else KeyboardCapitalization.None,
            autoCorrectEnabled = !secret && field != RegistrationField.Email,
            imeAction = if (field == RegistrationField.Confirmation) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                RegistrationField.entries.getOrNull(field.ordinal + 1)?.let {
                    fieldFocus.getValue(it).requestFocus()
                }
            },
            onDone = { focus.clearFocus(); keyboard?.hide() }
        )
    )
}
