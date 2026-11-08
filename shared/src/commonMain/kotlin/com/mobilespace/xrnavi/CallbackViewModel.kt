package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class CallbackViewModel(service: CallbackService) : ViewModel() {
    val bloc = CallbackBloc(service, viewModelScope)

    override fun onCleared() {
        bloc.dispatch(CallbackEvent.Stopped)
    }
}

internal fun callbackViewModelFactory(service: CallbackService) = viewModelFactory {
    initializer { CallbackViewModel(service) }
}
