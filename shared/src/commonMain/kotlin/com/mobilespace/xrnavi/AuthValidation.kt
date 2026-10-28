package com.mobilespace.xrnavi

private val emailPattern = Regex("^[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+$")

internal fun emailValidationError(email: String): String? =
    if (emailPattern.matches(email.trim())) null else "Вкажіть коректну електронну пошту"

internal fun passwordValidationError(password: String): String? =
    if (password.length >= 8) null else "Щонайменше 8 символів"

internal fun passwordConfirmationError(password: String, confirmation: String): String? = when {
    confirmation.isEmpty() -> "Підтвердіть пароль"
    confirmation != password -> "Паролі не збігаються"
    else -> null
}
