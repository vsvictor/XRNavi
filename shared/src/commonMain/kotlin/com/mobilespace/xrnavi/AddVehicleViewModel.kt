package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class AddVehicleViewModel(
    service: GarageService,
    sessionProvider: () -> AuthSession?,
) : ViewModel() {
    val bloc = AddVehicleBloc(service, sessionProvider, viewModelScope)
}

internal fun addVehicleViewModelFactory(
    service: GarageService,
    sessionProvider: () -> AuthSession?,
): ViewModelProvider.Factory = viewModelFactory { initializer { AddVehicleViewModel(service, sessionProvider) } }
