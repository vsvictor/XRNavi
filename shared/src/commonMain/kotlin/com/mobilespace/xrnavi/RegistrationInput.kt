package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.*
import xrnavi.shared.generated.resources.*

private val nameFields = setOf(
    RegistrationField.FirstName,
    RegistrationField.LastName,
    RegistrationField.MiddleName
)

@Composable
internal fun RegistrationInput(
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
            capitalization = if (field in nameFields) KeyboardCapitalization.Words else KeyboardCapitalization.None,
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
