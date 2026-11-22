package com.mobilespace.xrnavi

internal enum class RegistrationField { FirstName, LastName, MiddleName, Email, Password, Confirmation }

internal enum class RegistrationMembership(val label: String, val accountType: String) {
    Personal("Особистий", "private"),
    Corporate("Корпоративний", "corporate")
}

internal const val registrationUnavailableMessage =
    "Реєстрацію ще не підключено. Дані не надіслано"
internal const val invitationUnavailableMessage =
    "Для корпоративного аккаунта потрібне запрошення від адміністратора організації. Сценарій запрошення ще не підключено."

internal data class RegistrationState(
    val name: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordVisible: Boolean = false,
    val confirmationVisible: Boolean = false,
    val consent: Boolean = false,
    val membership: RegistrationMembership = RegistrationMembership.Personal,
    val notice: String = "",
    val touched: Set<RegistrationField> = emptySet(),
    val serviceAvailable: Boolean = false,
    val busy: Boolean = false,
    val registered: Boolean = false
) {
    fun validationError(field: RegistrationField): String? = when (field) {
        RegistrationField.FirstName -> if (name.trim().isEmpty()) "Вкажіть ім’я" else null
        RegistrationField.LastName -> if (lastName.trim().isEmpty()) "Вкажіть прізвище" else null
        RegistrationField.MiddleName -> null
        RegistrationField.Email -> emailValidationError(email)
        RegistrationField.Password -> passwordValidationError(password)
        RegistrationField.Confirmation -> passwordConfirmationError(password, confirmation)
    }

    fun visibleError(field: RegistrationField): String? =
        validationError(field).takeIf { field in touched }

    val canSubmit: Boolean
        get() = !busy && consent && RegistrationField.entries.all { validationError(it) == null }

    /** Parameters of the `auth.register` request; `middle_name` is sent only when filled. */
    fun registerParams(): Map<String, Any> = mapOf(
        "email" to email.trim(),
        "password" to password,
        "profile" to buildMap {
            put("first_name", name.trim())
            put("last_name", lastName.trim())
            if (middleName.isNotBlank()) put("middle_name", middleName.trim())
            put("account_type", membership.accountType)
        }
    )
}

internal sealed interface RegistrationEvent {
    data class FieldChanged(val field: RegistrationField, val value: String) : RegistrationEvent
    data class FieldTouched(val field: RegistrationField) : RegistrationEvent
    data object PasswordVisibilityToggled : RegistrationEvent
    data object ConfirmationVisibilityToggled : RegistrationEvent
    data class ConsentChanged(val value: Boolean) : RegistrationEvent
    data class MembershipSelected(val value: RegistrationMembership) : RegistrationEvent
    data object Submitted : RegistrationEvent
    data object Registered : RegistrationEvent
    data class Failed(val message: String) : RegistrationEvent
    data object RegisteredHandled : RegistrationEvent
    data object Cancelled : RegistrationEvent
    data object Cleared : RegistrationEvent
}
