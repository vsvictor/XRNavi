package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/** Compose-facing host of [RecoveryBloc]: mirrors its state and forwards intents as events. */
internal class RecoveryViewModel(previewEnabled: Boolean = false) : ViewModel() {
    private val bloc = RecoveryBloc(previewEnabled)
    private var current by mutableStateOf(bloc.state.value)

    private fun dispatch(event: RecoveryEvent) {
        bloc.dispatch(event)
        current = bloc.state.value
    }

    val stage get() = current.stage
    val email get() = current.email
    val password get() = current.password
    val confirmation get() = current.confirmation
    val passwordVisible get() = current.passwordVisible
    val confirmationVisible get() = current.confirmationVisible
    val notice get() = current.notice
    val emailError get() = current.emailError
    val passwordError get() = current.passwordError
    val confirmationError get() = current.confirmationError
    val canRequest get() = current.canRequest
    val canReset get() = current.canReset

    fun updateEmail(value: String) = dispatch(RecoveryEvent.EmailChanged(value))
    fun touchEmail() = dispatch(RecoveryEvent.EmailTouched)
    fun touchPassword() = dispatch(RecoveryEvent.PasswordTouched)
    fun touchConfirmation() = dispatch(RecoveryEvent.ConfirmationTouched)
    fun requestReset() = dispatch(RecoveryEvent.ResetRequested)

    fun openPreview(next: RecoveryStage): Boolean {
        dispatch(RecoveryEvent.PreviewOpened(next))
        return current.previewEnabled
    }

    fun returnToRequest() = dispatch(RecoveryEvent.ReturnedToRequest)
    fun resend() = dispatch(RecoveryEvent.Resent)
    fun openMailPreview() = dispatch(RecoveryEvent.MailOpened)
    fun updatePassword(value: String) = dispatch(RecoveryEvent.PasswordChanged(value))
    fun updateConfirmation(value: String) = dispatch(RecoveryEvent.ConfirmationChanged(value))
    fun togglePasswordVisibility() = dispatch(RecoveryEvent.PasswordVisibilityToggled)
    fun toggleConfirmationVisibility() = dispatch(RecoveryEvent.ConfirmationVisibilityToggled)
    fun submitNewPassword() = dispatch(RecoveryEvent.NewPasswordSubmitted)
    fun cancel() = dispatch(RecoveryEvent.Cancelled)

    override fun onCleared() {
        dispatch(RecoveryEvent.Cleared)
        super.onCleared()
    }
}

internal fun recoveryViewModelFactory(previewEnabled: Boolean) = viewModelFactory {
    initializer { RecoveryViewModel(previewEnabled) }
}
