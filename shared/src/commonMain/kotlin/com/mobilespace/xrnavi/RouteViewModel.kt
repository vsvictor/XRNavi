package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class RouteViewModel(service: RoutePlanningService) : ViewModel() {
    val bloc = RouteBloc(service, viewModelScope)
}

internal fun routeViewModelFactory(service: RoutePlanningService): ViewModelProvider.Factory =
    viewModelFactory { initializer { RouteViewModel(service) } }
