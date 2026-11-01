package com.mobilespace.xrnavi.presentation.auth

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.auth_error_confirmation_required
import xrnavi.shared.generated.resources.auth_error_email_invalid
import xrnavi.shared.generated.resources.auth_error_name_required
import xrnavi.shared.generated.resources.auth_error_password_mismatch
import xrnavi.shared.generated.resources.auth_error_password_too_short
import com.mobilespace.xrnavi.domain.auth.AuthValidationError

@Composable
internal fun AuthValidationError.localizedMessage(): String = stringResource(
    when (this) {
        AuthValidationError.NameRequired -> Res.string.auth_error_name_required
        AuthValidationError.InvalidEmail -> Res.string.auth_error_email_invalid
        AuthValidationError.PasswordTooShort -> Res.string.auth_error_password_too_short
        AuthValidationError.ConfirmationRequired -> Res.string.auth_error_confirmation_required
        AuthValidationError.PasswordMismatch -> Res.string.auth_error_password_mismatch
    }
)
