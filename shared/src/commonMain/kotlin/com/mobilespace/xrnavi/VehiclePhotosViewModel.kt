package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class VehiclePhotosViewModel(service: VehiclePhotoService, selections: VehiclePhotoSelections) : ViewModel() {
    val bloc = VehiclePhotosBloc(service, viewModelScope, selections)
}

internal fun vehiclePhotosViewModelFactory(
    service: VehiclePhotoService,
    selections: VehiclePhotoSelections,
): ViewModelProvider.Factory =
    viewModelFactory { initializer { VehiclePhotosViewModel(service, selections) } }
