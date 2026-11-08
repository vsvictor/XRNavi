package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RegistrationBloc {
    private val mutableState = MutableStateFlow(RegistrationState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: RegistrationEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: RegistrationState, event: RegistrationEvent): RegistrationState =
            when (event) {
                is RegistrationEvent.FieldChanged -> when (event.field) {
                    RegistrationField.Name -> state.copy(name = event.value)
                    RegistrationField.Email -> state.copy(email = event.value)
                    RegistrationField.Password -> state.copy(password = event.value)
                    RegistrationField.Confirmation -> state.copy(confirmation = event.value)
                }.copy(touched = state.touched + event.field, notice = "")

                is RegistrationEvent.FieldTouched ->
                    state.copy(touched = state.touched + event.field)

                RegistrationEvent.PasswordVisibilityToggled ->
                    state.copy(passwordVisible = !state.passwordVisible)

                RegistrationEvent.ConfirmationVisibilityToggled ->
                    state.copy(confirmationVisible = !state.confirmationVisible)

                is RegistrationEvent.ConsentChanged -> state.copy(consent = event.value)
                is RegistrationEvent.MembershipSelected ->
                    state.copy(membership = event.value, notice = "")

                RegistrationEvent.Submitted ->
                    if (!state.canSubmit) state.copy(touched = RegistrationField.entries.toSet())
                    else clearSecrets(state).copy(notice = registrationUnavailableMessage)

                RegistrationEvent.Cancelled -> RegistrationState()
                RegistrationEvent.Cleared -> clearSecrets(state)
            }

        private fun clearSecrets(state: RegistrationState) = state.copy(
            password = "",
            confirmation = "",
            passwordVisible = false,
            confirmationVisible = false,
            touched = state.touched - RegistrationField.Password - RegistrationField.Confirmation
        )
    }
}
