package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal enum class RegistrationField { Name, Email, Password, Confirmation }

internal enum class RegistrationMembership(val label: String) {
    Personal("Особистий"),
    Corporate("Корпоративний")
}

internal const val registrationUnavailableMessage =
    "Реєстрацію ще не підключено. Дані не надіслано"
internal const val invitationUnavailableMessage =
    "Для корпоративного акаунта потрібне запрошення від адміністратора організації. Сценарій запрошення ще не підключено."
internal const val registrationDocumentsMessage =
    "Умови користування та Політика конфіденційності ще недоступні. Юридичну частину реєстрації не завершено."

internal class RegistrationViewModel : ViewModel() {
    var name by mutableStateOf("")
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
    var consent by mutableStateOf(false)
        private set
    var membership by mutableStateOf(RegistrationMembership.Personal)
        private set
    var notice by mutableStateOf("")
        private set
    private var touched by mutableStateOf(emptySet<RegistrationField>())

    fun validationError(field: RegistrationField): String? = when (field) {
        RegistrationField.Name -> if (name.trim().isEmpty()) "Вкажіть ім’я та прізвище" else null
        RegistrationField.Email -> emailValidationError(email)
        RegistrationField.Password -> passwordValidationError(password)
        RegistrationField.Confirmation -> passwordConfirmationError(password, confirmation)
    }

    fun visibleError(field: RegistrationField): String? =
        validationError(field).takeIf { field in touched }

    val canSubmit: Boolean
        get() = consent && RegistrationField.entries.all { validationError(it) == null }

    fun update(field: RegistrationField, value: String) {
        when (field) {
            RegistrationField.Name -> name = value
            RegistrationField.Email -> email = value
            RegistrationField.Password -> password = value
            RegistrationField.Confirmation -> confirmation = value
        }
        touch(field)
        notice = ""
    }

    fun touch(field: RegistrationField) {
        touched = touched + field
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun toggleConfirmationVisibility() {
        confirmationVisible = !confirmationVisible
    }

    fun updateConsent(value: Boolean) {
        consent = value
    }

    fun selectMembership(value: RegistrationMembership) {
        membership = value
        notice = ""
    }

    fun submit() {
        if (!canSubmit) {
            touched = RegistrationField.entries.toSet()
            return
        }
        clearSecrets()
        notice = registrationUnavailableMessage
    }

    fun cancel() {
        clearSecrets()
        name = ""
        email = ""
        consent = false
        membership = RegistrationMembership.Personal
        touched = emptySet()
        notice = ""
    }

    private fun clearSecrets() {
        password = ""
        confirmation = ""
        passwordVisible = false
        confirmationVisible = false
        touched = touched - RegistrationField.Password - RegistrationField.Confirmation
    }

    override fun onCleared() {
        clearSecrets()
        super.onCleared()
    }

}

internal val RegistrationViewModelFactory = viewModelFactory {
    initializer { RegistrationViewModel() }
}
