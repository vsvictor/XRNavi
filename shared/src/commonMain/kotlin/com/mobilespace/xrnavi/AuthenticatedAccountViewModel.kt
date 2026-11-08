package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class AuthenticatedAccountViewModel(
    profiles: AuthenticatedProfileService,
    sessions: SessionManager,
) : ViewModel() {
    val bloc = AuthenticatedAccountBloc(profiles, viewModelScope, sessions)

    override fun onCleared() {
        bloc.dispatch(AuthenticatedAccountEvent.Release)
        super.onCleared()
    }
}

internal fun authenticatedAccountViewModelFactory(
    profiles: AuthenticatedProfileService,
    sessions: SessionManager,
) = viewModelFactory { initializer { AuthenticatedAccountViewModel(profiles, sessions) } }
