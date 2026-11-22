package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class MapViewModel : ViewModel() {
    val bloc = MapBloc()
}

internal val MapViewModelFactory = viewModelFactory { initializer { MapViewModel() } }
