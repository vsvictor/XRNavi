package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class AuthenticatedAccountViewModel(profiles: AuthenticatedProfileService) : ViewModel() {
    val bloc = AuthenticatedAccountBloc(profiles, viewModelScope)

    override fun onCleared() {
        bloc.dispatch(AuthenticatedAccountEvent.Logout)
        super.onCleared()
    }
}

internal fun authenticatedAccountViewModelFactory(profiles: AuthenticatedProfileService) =
    viewModelFactory { initializer { AuthenticatedAccountViewModel(profiles) } }
