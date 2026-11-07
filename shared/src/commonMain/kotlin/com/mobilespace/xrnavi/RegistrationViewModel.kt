package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/** Compose-facing host of [RegistrationBloc]: mirrors its state and forwards intents as events. */
internal class RegistrationViewModel : ViewModel() {
    private val bloc = RegistrationBloc()
    private var current by mutableStateOf(bloc.state.value)

    private fun dispatch(event: RegistrationEvent) {
        bloc.dispatch(event)
        current = bloc.state.value
    }

    val name get() = current.name
    val email get() = current.email
    val password get() = current.password
    val confirmation get() = current.confirmation
    val passwordVisible get() = current.passwordVisible
    val confirmationVisible get() = current.confirmationVisible
    val consent get() = current.consent
    val membership get() = current.membership
    val notice get() = current.notice
    val canSubmit get() = current.canSubmit

    fun validationError(field: RegistrationField): String? = current.validationError(field)
    fun visibleError(field: RegistrationField): String? = current.visibleError(field)

    fun update(field: RegistrationField, value: String) =
        dispatch(RegistrationEvent.FieldChanged(field, value))

    fun touch(field: RegistrationField) = dispatch(RegistrationEvent.FieldTouched(field))
    fun togglePasswordVisibility() = dispatch(RegistrationEvent.PasswordVisibilityToggled)
    fun toggleConfirmationVisibility() = dispatch(RegistrationEvent.ConfirmationVisibilityToggled)
    fun updateConsent(value: Boolean) = dispatch(RegistrationEvent.ConsentChanged(value))
    fun selectMembership(value: RegistrationMembership) =
        dispatch(RegistrationEvent.MembershipSelected(value))

    fun submit() = dispatch(RegistrationEvent.Submitted)
    fun cancel() = dispatch(RegistrationEvent.Cancelled)

    override fun onCleared() {
        dispatch(RegistrationEvent.Cleared)
        super.onCleared()
    }
}

internal val RegistrationViewModelFactory = viewModelFactory {
    initializer { RegistrationViewModel() }
}
