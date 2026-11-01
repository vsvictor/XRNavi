package com.mobilespace.xrnavi.domain.auth

private val emailPattern = Regex("^[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+$")

internal enum class AuthValidationError {
    NameRequired,
    InvalidEmail,
    PasswordTooShort,
    ConfirmationRequired,
    PasswordMismatch
}

internal fun emailValidationError(email: String): AuthValidationError? =
    if (emailPattern.matches(email.trim())) null else AuthValidationError.InvalidEmail

internal fun passwordValidationError(password: String): AuthValidationError? =
    if (password.length >= 8) null else AuthValidationError.PasswordTooShort

internal fun passwordConfirmationError(
    password: String,
    confirmation: String
): AuthValidationError? = when {
    confirmation.isEmpty() -> AuthValidationError.ConfirmationRequired
    confirmation != password -> AuthValidationError.PasswordMismatch
    else -> null
}
