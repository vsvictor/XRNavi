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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import xrnavi.shared.generated.resources.*

@Composable
internal fun RecoveryPasswords(model: RecoveryViewModel) {
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
