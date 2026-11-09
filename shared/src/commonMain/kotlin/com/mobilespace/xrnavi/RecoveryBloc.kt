package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class RecoveryBloc(
    previewEnabled: Boolean = false,
    private val service: RecoveryService? = null,
    private val scope: CoroutineScope? = null,
) {
    private val mutableState = MutableStateFlow(
        RecoveryState(previewEnabled = previewEnabled, serviceAvailable = service != null && scope != null)
    )
    val state = mutableState.asStateFlow()

    fun dispatch(event: RecoveryEvent) {
        val before = mutableState.value
        val after = transition(before, event)
        mutableState.value = after
        if (after.busy && !before.busy) when (event) {
            RecoveryEvent.ResetRequested, RecoveryEvent.Resent -> launchRequest(after.email)
            RecoveryEvent.NewPasswordSubmitted -> launchConfirm(after.email, after.resetToken, after.password)
            else -> Unit
        }
    }

    private fun launchRequest(email: String) {
        val service = service ?: return
        scope?.launch {
            val result = try {
                service.requestReset(email.trim())
                RecoveryEvent.RequestAccepted
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                RecoveryEvent.RequestFailed(error.recoveryFailureMessage())
            }
            dispatch(result)
        }
    }

    private fun launchConfirm(email: String, token: String, password: String) {
        val service = service ?: return
        scope?.launch {
            val result = try {
                service.confirmReset(email, token, password)
                RecoveryEvent.ResetCompleted
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                RecoveryEvent.ResetFailed(error.isAuthRejection(), error.recoveryFailureMessage())
            }
            dispatch(result)
        }
    }

    companion object {
        fun transition(state: RecoveryState, event: RecoveryEvent): RecoveryState {
            val preview = state.previewEnabled
            val newPassword = (preview || state.resetToken.isNotEmpty()) &&
                state.stage == RecoveryStage.NewPassword && !state.busy
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
                        when {
                            !touched.canRequest -> touched
                            touched.serviceAvailable -> touched.copy(busy = true, notice = "")
                            else -> touched.copy(notice = recoveryUnavailableMessage)
                        }
                    }
                }

                is RecoveryEvent.PreviewOpened ->
                    if (!preview) state else cleared(state).copy(notice = "", stage = event.stage)

                RecoveryEvent.ReturnedToRequest ->
                    cleared(state).copy(notice = "", stage = RecoveryStage.Request)

                RecoveryEvent.Resent ->
                    if (state.serviceAvailable && state.stage == RecoveryStage.CheckEmail && !state.busy)
                        state.copy(busy = true, notice = "")
                    else if (preview && state.stage == RecoveryStage.CheckEmail)
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
                        when {
                            !touched.canReset -> touched
                            touched.serviceAvailable && touched.resetToken.isNotEmpty() ->
                                touched.copy(busy = true, notice = "")
                            else -> cleared(touched).copy(notice = passwordResetUnavailableMessage)
                        }
                    }
                }

                RecoveryEvent.Cancelled ->
                    cleared(state).copy(
                        resetToken = "", busy = false, notice = "", stage = RecoveryStage.Request, email = "", emailTouched = false
                    )

                RecoveryEvent.Cleared -> cleared(state).copy(resetToken = "")

                is RecoveryEvent.TokenInputChanged ->
                    if (state.serviceAvailable && state.stage == RecoveryStage.CheckEmail)
                        state.copy(tokenInput = event.value, notice = "") else state

                RecoveryEvent.TokenSubmitted ->
                    if (!state.canSubmitToken) state
                    else cleared(state).copy(
                        resetToken = state.tokenInput.trim(), stage = RecoveryStage.NewPassword,
                        notice = ""
                    )

                is RecoveryEvent.TokenReceived ->
                    if (!state.serviceAvailable || event.token.isBlank()) state
                    else cleared(state).copy(
                        resetToken = event.token, stage = RecoveryStage.NewPassword,
                        notice = "", busy = false
                    )

                is RecoveryEvent.Restored ->
                    if (state.serviceAvailable && state.stage == RecoveryStage.Request &&
                        emailValidationError(event.email) == null
                    ) state.copy(email = event.email, stage = RecoveryStage.CheckEmail) else state

                RecoveryEvent.RequestAccepted ->
                    if (!state.busy) state
                    else state.copy(busy = false, notice = "", stage = RecoveryStage.CheckEmail)

                is RecoveryEvent.RequestFailed ->
                    if (!state.busy) state else state.copy(busy = false, notice = event.message)

                RecoveryEvent.ResetCompleted ->
                    if (!state.busy) state
                    else cleared(state).copy(busy = false, resetToken = "", stage = RecoveryStage.Changed)

                is RecoveryEvent.ResetFailed ->
                    if (!state.busy) state
                    else if (event.invalidToken) cleared(state).copy(
                        busy = false, resetToken = "", stage = RecoveryStage.InvalidLink
                    ) else state.copy(busy = false, notice = event.message)
            }
        }

        private fun cleared(state: RecoveryState) = state.copy(
            password = "",
            confirmation = "",
            tokenInput = "",
            passwordVisible = false,
            confirmationVisible = false,
            passwordTouched = false,
            confirmationTouched = false
        )
    }
}
