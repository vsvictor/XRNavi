package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.time.Clock

internal class GeoViewModel(provider: LocationProvider) : ViewModel() {
    val bloc = GeoBloc(provider, viewModelScope, { Clock.System.now().toEpochMilliseconds() })
}

internal fun geoViewModelFactory(provider: LocationProvider): ViewModelProvider.Factory =
    viewModelFactory { initializer { GeoViewModel(provider) } }
