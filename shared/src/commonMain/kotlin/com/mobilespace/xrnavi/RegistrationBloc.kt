package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class RegistrationBloc(
    private val service: RegistrationService? = null,
    private val scope: CoroutineScope? = null,
) {
    private val mutableState = MutableStateFlow(
        RegistrationState(serviceAvailable = service != null && scope != null)
    )
    val state = mutableState.asStateFlow()

    fun dispatch(event: RegistrationEvent) {
        val before = mutableState.value
        val after = transition(before, event)
        mutableState.value = after
        if (after.busy && !before.busy) launchRegister(after)
    }

    private fun launchRegister(snapshot: RegistrationState) {
        val service = service ?: return
        scope?.launch {
            val result = try {
                service.register(snapshot)
                RegistrationEvent.Registered
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                RegistrationEvent.Failed(error.registrationFailureMessage())
            }
            dispatch(result)
        }
    }

    companion object {
        fun transition(state: RegistrationState, event: RegistrationEvent): RegistrationState =
            when (event) {
                is RegistrationEvent.FieldChanged -> when (event.field) {
                    RegistrationField.FirstName -> state.copy(name = event.value)
                    RegistrationField.LastName -> state.copy(lastName = event.value)
                    RegistrationField.MiddleName -> state.copy(middleName = event.value)
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
                    if (state.busy) state
                    else if (!state.canSubmit) state.copy(touched = RegistrationField.entries.toSet())
                    else if (state.serviceAvailable) state.copy(busy = true, notice = "")
                    else clearSecrets(state).copy(notice = registrationUnavailableMessage)

                RegistrationEvent.Registered ->
                    if (!state.busy) state
                    else clearSecrets(state).copy(busy = false, registered = true, notice = "")

                is RegistrationEvent.Failed ->
                    if (!state.busy) state else state.copy(busy = false, notice = event.message)

                RegistrationEvent.RegisteredHandled -> RegistrationState(
                    serviceAvailable = state.serviceAvailable,
                    membership = state.membership
                )

                RegistrationEvent.Cancelled -> RegistrationState(serviceAvailable = state.serviceAvailable)
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
