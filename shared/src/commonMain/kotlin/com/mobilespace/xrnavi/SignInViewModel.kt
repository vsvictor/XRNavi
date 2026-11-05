package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class SignInViewModel(auth: SignInAuthService) : ViewModel() {
    val bloc = SignInBloc(auth, viewModelScope)

    fun clearPassword() = bloc.dispatch(SignInEvent.Cancel)

    override fun onCleared() {
        bloc.dispatch(SignInEvent.Cancel)
        super.onCleared()
    }
}

internal fun signInViewModelFactory(auth: SignInAuthService) = viewModelFactory {
    initializer { SignInViewModel(auth) }
}
