package com.mobilespace.xrnavi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch

/** Compose-facing host of [RecoveryBloc]: mirrors its state and forwards intents as events. */
internal class RecoveryViewModel(
    previewEnabled: Boolean = false,
    service: RecoveryService? = null,
    private val progress: RecoveryProgressStore? = null,
) : ViewModel() {
    private val bloc = RecoveryBloc(previewEnabled, service, if (service != null) viewModelScope else null)
    private var current by mutableStateOf(bloc.state.value)

    init {
        progress?.read()?.let { bloc.dispatch(RecoveryEvent.Restored(it)); current = bloc.state.value }
        if (service != null) viewModelScope.launch {
            bloc.state.collect {
                current = it
                saveProgress(it)
            }
        }
    }

    private fun dispatch(event: RecoveryEvent) {
        bloc.dispatch(event)
        current = bloc.state.value
        saveProgress(current)
    }

    private fun saveProgress(state: RecoveryState) {
        if (progress == null || notLive(state)) return
        progress.write(if (state.stage == RecoveryStage.CheckEmail) state.email.trim() else null)
    }

    private fun notLive(state: RecoveryState) = !state.serviceAvailable

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
    val busy get() = current.busy
    val live get() = current.serviceAvailable
    val tokenInput get() = current.tokenInput
    val canSubmitToken get() = current.canSubmitToken

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
    fun updateTokenInput(value: String) = dispatch(RecoveryEvent.TokenInputChanged(value))
    fun submitToken() = dispatch(RecoveryEvent.TokenSubmitted)
    fun cancel() = dispatch(RecoveryEvent.Cancelled)
    fun receiveResetToken(token: String) = dispatch(RecoveryEvent.TokenReceived(token))

    override fun onCleared() {
        dispatch(RecoveryEvent.Cleared)
        super.onCleared()
    }
}

internal fun recoveryViewModelFactory(
    previewEnabled: Boolean,
    service: RecoveryService? = null,
    progress: RecoveryProgressStore? = null,
) = viewModelFactory {
    initializer { RecoveryViewModel(previewEnabled, service, progress) }
}
