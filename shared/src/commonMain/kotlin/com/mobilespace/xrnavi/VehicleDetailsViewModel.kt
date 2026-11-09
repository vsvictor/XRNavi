package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class VehicleDetailsViewModel : ViewModel() {
    val bloc = VehicleDetailsBloc()
}

internal val VehicleDetailsViewModelFactory: ViewModelProvider.Factory =
    viewModelFactory { initializer { VehicleDetailsViewModel() } }
