package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal enum class RecoveryStage(val code: String, val title: String) {
    Request("S41", "Відновити пароль"),
    CheckEmail("S42", "Перевірте пошту"),
    NewPassword("S43", "Новий пароль"),
    Changed("S44", "Пароль змінено"),
    InvalidLink("S45", "Посилання недійсне")
}

internal const val recoveryUnavailableMessage =
    "Відновлення пароля ще не підключено. Дані не надіслано"
internal const val passwordResetUnavailableMessage =
    "Зміну пароля ще не підключено. Пароль не змінено. Дані не надіслано"
internal const val recoveryPreviewMessage =
    "Внутрішній preview · демо. Листи не надсилалися, токен не перевірявся, пароль не змінювався."

internal class RecoveryViewModel(private val previewEnabled: Boolean = false) : ViewModel() {
    var stage by mutableStateOf(RecoveryStage.Request)
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmation by mutableStateOf("")
        private set
    var passwordVisible by mutableStateOf(false)
        private set
    var confirmationVisible by mutableStateOf(false)
        private set
    var notice by mutableStateOf("")
        private set
    private var emailTouched by mutableStateOf(false)
    private var passwordTouched by mutableStateOf(false)
    private var confirmationTouched by mutableStateOf(false)

    val emailError: String?
        get() = emailValidationError(email).takeIf { emailTouched }
    val passwordError: String?
        get() = passwordValidationError(password).takeIf { passwordTouched }
    val confirmationError: String?
        get() = passwordConfirmationError(password, confirmation).takeIf { confirmationTouched }
    val canRequest: Boolean
        get() = stage == RecoveryStage.Request && emailValidationError(email) == null
    val canReset: Boolean
        get() = previewEnabled && stage == RecoveryStage.NewPassword &&
                passwordValidationError(password) == null &&
                passwordConfirmationError(password, confirmation) == null

    fun updateEmail(value: String) {
        email = value
        touchEmail()
        notice = ""
    }

    fun touchEmail() {
        emailTouched = true
    }

    fun touchPassword() {
        passwordTouched = true
    }

    fun touchConfirmation() {
        confirmationTouched = true
    }

    fun requestReset() {
        if (stage != RecoveryStage.Request) return
        touchEmail()
        if (!canRequest) return
        notice = recoveryUnavailableMessage
    }

    fun openPreview(next: RecoveryStage): Boolean {
        if (!previewEnabled) return false
        clearSecrets()
        notice = ""
        stage = next
        return true
    }

    fun returnToRequest() {
        clearSecrets()
        notice = ""
        stage = RecoveryStage.Request
    }

    fun resend() {
        if (!previewEnabled || stage != RecoveryStage.CheckEmail) return
        notice = recoveryUnavailableMessage
    }

    fun openMailPreview() {
        if (!previewEnabled || stage != RecoveryStage.CheckEmail) return
        notice = "Відкриття пошти не підключено. Жодного листа не надсилали; це лише preview."
    }

    fun updatePassword(value: String) {
        if (!previewEnabled || stage != RecoveryStage.NewPassword) return
        password = value
        touchPassword()
        notice = ""
    }

    fun updateConfirmation(value: String) {
        if (!previewEnabled || stage != RecoveryStage.NewPassword) return
        confirmation = value
        touchConfirmation()
        notice = ""
    }

    fun togglePasswordVisibility() {
        if (previewEnabled && stage == RecoveryStage.NewPassword) passwordVisible = !passwordVisible
    }

    fun toggleConfirmationVisibility() {
        if (previewEnabled && stage == RecoveryStage.NewPassword) confirmationVisible =
            !confirmationVisible
    }

    fun submitNewPassword() {
        if (!previewEnabled || stage != RecoveryStage.NewPassword) return
        touchPassword()
        touchConfirmation()
        if (!canReset) return
        clearSecrets()
        notice = passwordResetUnavailableMessage
    }

    fun cancel() {
        returnToRequest()
        email = ""
        emailTouched = false
    }

    private fun clearSecrets() {
        password = ""
        confirmation = ""
        passwordVisible = false
        confirmationVisible = false
        passwordTouched = false
        confirmationTouched = false
    }

    override fun onCleared() {
        clearSecrets()
        super.onCleared()
    }
}

internal fun recoveryViewModelFactory(previewEnabled: Boolean) = viewModelFactory {
    initializer { RecoveryViewModel(previewEnabled) }
}
