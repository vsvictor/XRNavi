package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

internal class SplashViewModel : ViewModel() {
    val bloc = SplashBloc()
}

internal fun splashViewModelFactory() = viewModelFactory {
    initializer { SplashViewModel() }
}
