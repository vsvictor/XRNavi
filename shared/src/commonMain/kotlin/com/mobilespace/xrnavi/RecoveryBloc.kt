package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RecoveryBloc(previewEnabled: Boolean = false) {
    private val mutableState = MutableStateFlow(RecoveryState(previewEnabled = previewEnabled))
    val state = mutableState.asStateFlow()

    fun dispatch(event: RecoveryEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(state: RecoveryState, event: RecoveryEvent): RecoveryState {
            val preview = state.previewEnabled
            val newPassword = preview && state.stage == RecoveryStage.NewPassword
            return when (event) {
                is RecoveryEvent.EmailChanged ->
                    state.copy(email = event.value, emailTouched = true, notice = "")

                RecoveryEvent.EmailTouched -> state.copy(emailTouched = true)
                RecoveryEvent.PasswordTouched -> state.copy(passwordTouched = true)
                RecoveryEvent.ConfirmationTouched -> state.copy(confirmationTouched = true)
                RecoveryEvent.ResetRequested -> {
                    if (state.stage != RecoveryStage.Request) state
                    else {
                        val touched = state.copy(emailTouched = true)
                        if (touched.canRequest) touched.copy(notice = recoveryUnavailableMessage)
                        else touched
                    }
                }

                is RecoveryEvent.PreviewOpened ->
                    if (!preview) state else cleared(state).copy(notice = "", stage = event.stage)

                RecoveryEvent.ReturnedToRequest ->
                    cleared(state).copy(notice = "", stage = RecoveryStage.Request)

                RecoveryEvent.Resent ->
                    if (preview && state.stage == RecoveryStage.CheckEmail)
                        state.copy(notice = recoveryUnavailableMessage) else state

                RecoveryEvent.MailOpened ->
                    if (preview && state.stage == RecoveryStage.CheckEmail)
                        state.copy(notice = recoveryMailUnavailableMessage) else state

                is RecoveryEvent.PasswordChanged ->
                    if (newPassword) state.copy(
                        password = event.value, passwordTouched = true, notice = ""
                    ) else state

                is RecoveryEvent.ConfirmationChanged ->
                    if (newPassword) state.copy(
                        confirmation = event.value, confirmationTouched = true, notice = ""
                    ) else state

                RecoveryEvent.PasswordVisibilityToggled ->
                    if (newPassword) state.copy(passwordVisible = !state.passwordVisible) else state

                RecoveryEvent.ConfirmationVisibilityToggled ->
                    if (newPassword) state.copy(confirmationVisible = !state.confirmationVisible)
                    else state

                RecoveryEvent.NewPasswordSubmitted -> {
                    if (!newPassword) state
                    else {
                        val touched = state.copy(passwordTouched = true, confirmationTouched = true)
                        if (touched.canReset) cleared(touched)
                            .copy(notice = passwordResetUnavailableMessage)
                        else touched
                    }
                }

                RecoveryEvent.Cancelled ->
                    cleared(state).copy(
                        notice = "", stage = RecoveryStage.Request, email = "", emailTouched = false
                    )

                RecoveryEvent.Cleared -> cleared(state)
            }
        }

        private fun cleared(state: RecoveryState) = state.copy(
            password = "",
            confirmation = "",
            passwordVisible = false,
            confirmationVisible = false,
            passwordTouched = false,
            confirmationTouched = false
        )
    }
}
