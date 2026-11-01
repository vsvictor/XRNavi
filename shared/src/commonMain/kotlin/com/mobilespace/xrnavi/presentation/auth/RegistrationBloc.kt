package com.mobilespace.xrnavi.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobilespace.xrnavi.domain.auth.AuthValidationError
import com.mobilespace.xrnavi.domain.auth.emailValidationError
import com.mobilespace.xrnavi.domain.auth.passwordConfirmationError
import com.mobilespace.xrnavi.domain.auth.passwordValidationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class RegistrationField { Name, Email, Password, Confirmation }

internal enum class RegistrationMembership {
    Personal,
    Corporate
}

internal enum class RegistrationNotice {
    SubmissionUnavailable
}

internal data class RegistrationState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordVisible: Boolean = false,
    val confirmationVisible: Boolean = false,
    val consent: Boolean = false,
    val membership: RegistrationMembership = RegistrationMembership.Personal,
    val notice: RegistrationNotice? = null,
    val touched: Set<RegistrationField> = emptySet()
) {
    fun validationError(field: RegistrationField): AuthValidationError? = when (field) {
        RegistrationField.Name ->
            if (name.trim().isEmpty()) AuthValidationError.NameRequired else null
        RegistrationField.Email -> emailValidationError(email)
        RegistrationField.Password -> passwordValidationError(password)
        RegistrationField.Confirmation -> passwordConfirmationError(password, confirmation)
    }

    fun visibleError(field: RegistrationField): AuthValidationError? =
        validationError(field).takeIf { field in touched }

    val canSubmit: Boolean
        get() = consent && RegistrationField.entries.all { validationError(it) == null }
}

internal sealed interface RegistrationEvent {
    data class FieldChanged(val field: RegistrationField, val value: String) : RegistrationEvent
    data class FieldTouched(val field: RegistrationField) : RegistrationEvent
    data object TogglePasswordVisibility : RegistrationEvent
    data object ToggleConfirmationVisibility : RegistrationEvent
    data class ConsentChanged(val value: Boolean) : RegistrationEvent
    data class MembershipSelected(val value: RegistrationMembership) : RegistrationEvent
    data object Submit : RegistrationEvent
    data object Cancel : RegistrationEvent
}

internal class RegistrationBloc : ViewModel() {
    private val mutableState = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = mutableState.asStateFlow()

    fun dispatch(event: RegistrationEvent) {
        val current = mutableState.value
        mutableState.value = when (event) {
            is RegistrationEvent.FieldChanged -> current
                .copy(notice = null, touched = current.touched + event.field)
                .withFieldValue(event.field, event.value)
            is RegistrationEvent.FieldTouched -> current.copy(
                touched = current.touched + event.field
            )
            RegistrationEvent.TogglePasswordVisibility -> current.copy(
                passwordVisible = !current.passwordVisible
            )
            RegistrationEvent.ToggleConfirmationVisibility -> current.copy(
                confirmationVisible = !current.confirmationVisible
            )
            is RegistrationEvent.ConsentChanged -> current.copy(consent = event.value)
            is RegistrationEvent.MembershipSelected -> current.copy(
                membership = event.value,
                notice = null
            )
            RegistrationEvent.Submit -> submit(current)
            RegistrationEvent.Cancel -> RegistrationState()
        }
    }

    private fun submit(current: RegistrationState): RegistrationState = if (!current.canSubmit) {
        current.copy(touched = RegistrationField.entries.toSet())
    } else {
        // TODO: Call the domain registration use case backed by data/auth; keep credentials transient and report provider results.
        current.clearSecrets().copy(notice = RegistrationNotice.SubmissionUnavailable)
    }

    override fun onCleared() {
        mutableState.value = mutableState.value.clearSecrets()
        super.onCleared()
    }
}

private fun RegistrationState.withFieldValue(
    field: RegistrationField,
    value: String
): RegistrationState = when (field) {
    RegistrationField.Name -> copy(name = value)
    RegistrationField.Email -> copy(email = value)
    RegistrationField.Password -> copy(password = value)
    RegistrationField.Confirmation -> copy(confirmation = value)
}

private fun RegistrationState.clearSecrets(): RegistrationState = copy(
    password = "",
    confirmation = "",
    passwordVisible = false,
    confirmationVisible = false,
    touched = touched - RegistrationField.Password - RegistrationField.Confirmation
)

internal val RegistrationBlocFactory = viewModelFactory {
    initializer { RegistrationBloc() }
}
