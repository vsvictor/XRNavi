package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobilespace.xrnavi.di.AppContainer
import com.mobilespace.xrnavi.presentation.AppViewModel
import com.mobilespace.xrnavi.presentation.ui.ApexTheme
import com.mobilespace.xrnavi.presentation.ui.ScreenHost

@Composable
fun App() {
    val appViewModel = viewModel { AppViewModel(AppContainer()) }
    val navigation by appViewModel.state.collectAsStateWithLifecycle()
    PlatformBackHandler(navigation.canGoBack, appViewModel::back)
    ApexTheme { ScreenHost(appViewModel, navigation.current) }
}
