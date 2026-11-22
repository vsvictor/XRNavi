package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class GarageViewModel(
    service: GarageService,
    sessionProvider: () -> AuthSession?,
) : ViewModel() {
    val bloc = GarageBloc(service, sessionProvider, viewModelScope)
}

internal fun garageViewModelFactory(
    service: GarageService,
    sessionProvider: () -> AuthSession?,
): ViewModelProvider.Factory = viewModelFactory { initializer { GarageViewModel(service, sessionProvider) } }
