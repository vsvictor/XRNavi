package com.mobilespace.xrnavi

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.*
import com.mobilespace.xrnavi.presentation.auth.RegistrationField
import com.mobilespace.xrnavi.presentation.auth.RegistrationBloc
import com.mobilespace.xrnavi.presentation.auth.RegistrationEvent
import com.mobilespace.xrnavi.presentation.auth.RegistrationMembership
import com.mobilespace.xrnavi.presentation.auth.RegistrationNotice
import com.mobilespace.xrnavi.presentation.auth.RegistrationState
import com.mobilespace.xrnavi.presentation.auth.localizedMessage

@Composable
internal fun RegistrationScreen(
    bloc: RegistrationBloc,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val state by bloc.state.collectAsState()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var showDocuments by remember { mutableStateOf(false) }
    val backDescription = stringResource(Res.string.registration_back_accessibility)
    val back = {
        focus.clearFocus()
        keyboard?.hide()
        onBack()
    }
    AuthBackHandler(onBack = back)
    if (showDocuments) {
        AlertDialog(
            onDismissRequest = { showDocuments = false },
            title = { Text(stringResource(Res.string.registration_documents_title)) },
            text = { Text(stringResource(Res.string.registration_documents_message)) },
            confirmButton = {
                TextButton(onClick = { showDocuments = false }) {
                    Text(stringResource(Res.string.action_understood))
                }
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
                Modifier.fillMaxWidth().padding(horizontal = Constants.DP_12, vertical = Constants.DP_4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = back, modifier = Modifier.semantics {
                    contentDescription = backDescription
                }) { Text(stringResource(Res.string.registration_back_glyph), fontSize = 28.sp) }
                Column(Modifier.weight(1f).padding(vertical = if (compactHeader) 0.dp else Constants.DP_8)) {
                    Text(
                        stringResource(Res.string.registration_title),
                        fontFamily = rememberRobotoCondensedFontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compactHeader) 22.sp else 26.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!compactHeader) {
                        Text(
                            stringResource(Res.string.registration_subtitle),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onToggleTheme) {
                    Text(stringResource(Res.string.search_theme))
                }
            }
            if (split) {
                Row(
                    Modifier.weight(1f).padding(horizontal = Constants.DP_24),
                    horizontalArrangement = Arrangement.spacedBy(Constants.DP_32)
                ) {
                    if (!compactHeader) {
                        RegistrationHero(
                            Modifier.weight(1f).fillMaxHeight().padding(bottom = Constants.DP_16),
                            compact = false
                        )
                    }
                    RegistrationForm(
                        bloc,
                        state,
                        Modifier.weight(1f).fillMaxHeight().verticalScroll(scroll)
                            .padding(bottom = Constants.DP_24),
                        onDocuments = { showDocuments = true },
                        onBack = back
                    )
                }
            } else {
                Column(
                    Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = Constants.DP_24),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RegistrationHero(
                        Modifier.fillMaxWidth().height(if (tablet) 260.dp else 110.dp),
                        compact = !tablet
                    )
                    Spacer(Modifier.height(Constants.DP_16))
                    RegistrationForm(
                        bloc,
                        state,
                        Modifier.widthIn(max = 540.dp).fillMaxWidth().padding(bottom = Constants.DP_24),
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
    Box(modifier.clip(RoundedCornerShape(Constants.DP_16)).background(MaterialTheme.colorScheme.surface)) {
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
            Modifier.fillMaxSize().padding(Constants.DP_16),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!compact) ProductWordmark()
            Column {
                Text(
                    stringResource(Res.string.registration_hero_title),
                    fontFamily = rememberRobotoCondensedFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 22.sp else 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    stringResource(Res.string.registration_hero_subtitle),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(Res.string.registration_brand),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RegistrationForm(
    bloc: RegistrationBloc,
    state: RegistrationState,
    modifier: Modifier,
    onDocuments: () -> Unit,
    onBack: () -> Unit
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val consentDescription = stringResource(Res.string.registration_consent_accessibility)
    val fieldFocus = remember { RegistrationField.entries.associateWith { FocusRequester() } }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(Constants.DP_8))
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RegistrationMembership.entries.forEach { membership ->
                val chosen = membership == state.membership
                TextButton(
                    onClick = { bloc.dispatch(RegistrationEvent.MembershipSelected(membership)) },
                    modifier = Modifier.weight(1f).padding(Constants.DP_4)
                        .background(
                            if (chosen) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                            RoundedCornerShape(Constants.DP_6)
                        ).semantics {
                            selected = chosen
                            role = Role.Tab
                        }
                ) {
                    Text(
                        stringResource(membership.labelResource()),
                        fontSize = 12.sp
                    )
                }
            }
        }
        if (state.membership == RegistrationMembership.Corporate) {
            AuthInfo(stringResource(Res.string.registration_invitation_unavailable))
        }
        RegistrationInput(
            bloc,
            state,
            RegistrationField.Name,
            stringResource(Res.string.registration_name),
            state.name,
            fieldFocus
        )
        RegistrationInput(
            bloc,
            state,
            RegistrationField.Email,
            stringResource(Res.string.recovery_email),
            state.email,
            fieldFocus
        )
        RegistrationInput(
            bloc,
            state,
            RegistrationField.Password,
            stringResource(Res.string.registration_password),
            state.password,
            fieldFocus
        )
        if (state.visibleError(RegistrationField.Password) == null) {
            Text(
                stringResource(Res.string.registration_password_hint),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RegistrationInput(
            bloc,
            state,
            RegistrationField.Confirmation,
            stringResource(Res.string.registration_password_confirmation),
            state.confirmation,
            fieldFocus
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.consent,
                onCheckedChange = { bloc.dispatch(RegistrationEvent.ConsentChanged(it)) },
                modifier = Modifier.semantics {
                    contentDescription = consentDescription
                }
            )
            Text(
                stringResource(Res.string.registration_consent),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
        TextButton(onClick = onDocuments) {
            Text(
                stringResource(Res.string.registration_documents_action),
                fontSize = 12.sp
            )
        }
        Button(
            onClick = {
                focus.clearFocus()
                keyboard?.hide()
                bloc.dispatch(RegistrationEvent.Submit)
            },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(26.dp)
        ) { Text(stringResource(Res.string.registration_title)) }
        state.notice?.let { notice ->
            AuthInfo(
                stringResource(
                    when (notice) {
                        RegistrationNotice.SubmissionUnavailable ->
                            Res.string.registration_unavailable
                    }
                )
            )
        }
        if (state.membership == RegistrationMembership.Personal) {
            AuthInfo(stringResource(Res.string.registration_invitation_unavailable))
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.registration_existing_account))
        }
    }
}

@Composable
private fun RegistrationInput(
    bloc: RegistrationBloc,
    state: RegistrationState,
    field: RegistrationField,
    label: String,
    value: String,
    fieldFocus: Map<RegistrationField, FocusRequester>
) {
    val secret = field == RegistrationField.Password || field == RegistrationField.Confirmation
    val visible =
        if (field == RegistrationField.Password) state.passwordVisible else state.confirmationVisible
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    AuthFormInput(
        label = label,
        value = value,
        onValueChange = { bloc.dispatch(RegistrationEvent.FieldChanged(field, it)) },
        error = state.visibleError(field)?.localizedMessage(),
        onBlur = { bloc.dispatch(RegistrationEvent.FieldTouched(field)) },
        modifier = Modifier.focusRequester(fieldFocus.getValue(field)),
        secret = secret,
        visible = visible,
        onToggleVisibility = {
            if (field == RegistrationField.Password) {
                bloc.dispatch(RegistrationEvent.TogglePasswordVisibility)
            } else {
                bloc.dispatch(RegistrationEvent.ToggleConfirmationVisibility)
            }
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

@Composable
private fun RegistrationMembership.labelResource() = when (this) {
    RegistrationMembership.Personal -> Res.string.registration_membership_personal
    RegistrationMembership.Corporate -> Res.string.registration_membership_corporate
}
