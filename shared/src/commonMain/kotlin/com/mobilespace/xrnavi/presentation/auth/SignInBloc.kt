package com.mobilespace.xrnavi.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class SignInState(
    val email: String = "",
    val password: String = ""
)

internal sealed interface SignInEvent {
    data class EmailChanged(val value: String) : SignInEvent
    data class PasswordChanged(val value: String) : SignInEvent
    data object ClearPassword : SignInEvent
}

internal class SignInBloc : ViewModel() {
    private val mutableState = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = mutableState.asStateFlow()

    fun dispatch(event: SignInEvent) {
        mutableState.value = when (event) {
            is SignInEvent.EmailChanged -> mutableState.value.copy(email = event.value)
            is SignInEvent.PasswordChanged -> mutableState.value.copy(password = event.value)
            SignInEvent.ClearPassword -> mutableState.value.copy(password = "")
        }
    }
}

internal val SignInBlocFactory = viewModelFactory {
    initializer { SignInBloc() }
}
