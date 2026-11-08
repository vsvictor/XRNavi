package com.mobilespace.xrnavi

internal enum class RegistrationField { Name, Email, Password, Confirmation }

internal enum class RegistrationMembership(val label: String) {
    Personal("Особистий"),
    Corporate("Корпоративний")
}

internal const val registrationUnavailableMessage =
    "Реєстрацію ще не підключено. Дані не надіслано"
internal const val invitationUnavailableMessage =
    "Для корпоративного акаунта потрібне запрошення від адміністратора організації. Сценарій запрошення ще не підключено."

internal data class RegistrationState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordVisible: Boolean = false,
    val confirmationVisible: Boolean = false,
    val consent: Boolean = false,
    val membership: RegistrationMembership = RegistrationMembership.Personal,
    val notice: String = "",
    val touched: Set<RegistrationField> = emptySet()
) {
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
}

internal sealed interface RegistrationEvent {
    data class FieldChanged(val field: RegistrationField, val value: String) : RegistrationEvent
    data class FieldTouched(val field: RegistrationField) : RegistrationEvent
    data object PasswordVisibilityToggled : RegistrationEvent
    data object ConfirmationVisibilityToggled : RegistrationEvent
    data class ConsentChanged(val value: Boolean) : RegistrationEvent
    data class MembershipSelected(val value: RegistrationMembership) : RegistrationEvent
    data object Submitted : RegistrationEvent
    data object Cancelled : RegistrationEvent
    data object Cleared : RegistrationEvent
}
