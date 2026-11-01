package com.mobilespace.xrnavi.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobilespace.xrnavi.domain.auth.AuthValidationError
import com.mobilespace.xrnavi.domain.auth.emailValidationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class RecoveryState(
    val email: String = "",
    val requestUnavailable: Boolean = false,
    val emailTouched: Boolean = false
) {
    val emailError: AuthValidationError?
        get() = emailValidationError(email).takeIf { emailTouched }

    val canRequest: Boolean
        get() = emailValidationError(email) == null
}

internal sealed interface RecoveryEvent {
    data class EmailChanged(val value: String) : RecoveryEvent
    data object EmailTouched : RecoveryEvent
    data object RequestReset : RecoveryEvent
    data object Cancel : RecoveryEvent
}

internal class RecoveryBloc : ViewModel() {
    private val mutableState = MutableStateFlow(RecoveryState())
    val state: StateFlow<RecoveryState> = mutableState.asStateFlow()

    fun dispatch(event: RecoveryEvent) {
        val current = mutableState.value
        mutableState.value = when (event) {
            is RecoveryEvent.EmailChanged -> current.copy(
                email = event.value,
                emailTouched = true,
                requestUnavailable = false
            )
            RecoveryEvent.EmailTouched -> current.copy(emailTouched = true)
            RecoveryEvent.RequestReset -> current.copy(
                emailTouched = true,
                // TODO: Replace this unavailable state with the domain password-recovery use case and configured auth API.
                requestUnavailable = current.canRequest
            )
            RecoveryEvent.Cancel -> RecoveryState()
        }
    }

    override fun onCleared() {
        mutableState.value = RecoveryState()
        super.onCleared()
    }
}

internal val RecoveryBlocFactory = viewModelFactory {
    initializer { RecoveryBloc() }
}
