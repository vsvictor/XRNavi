package com.mobilespace.xrnavi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.mobilespace.xrnavi.di.AppContainer
import com.mobilespace.xrnavi.presentation.navigation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(val container: AppContainer) : ViewModel() {
    private val navigator = AppNavigator()
    private val stores = mutableMapOf<Long, ViewModelStoreOwner>()
    private val mutableState = MutableStateFlow(navigator.state)
    val state = mutableState.asStateFlow()
    fun owner(entry: NavigationEntry): ViewModelStoreOwner = stores.getOrPut(entry.id) {
        object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() }
    }
    fun open(destination: AppDestination, taskId: String? = null) {
        val shared = container.session.state.value
        navigator.open(destination, shared.context, shared.organizationMember, taskId)
        publish()
    }
    fun back() { navigator.back(); publish() }
    fun authenticated() { navigator.authenticated(); publish() }
    fun tab(destination: AppDestination) { navigator.selectTab(destination); publish() }
    fun finishPreview() { navigator.finishPreview(); publish() }
    private fun publish() {
        mutableState.value = navigator.state
        val retained = navigator.state.entries.map { it.id }.toSet()
        stores.keys.filterNot { it in retained }.forEach { stores.remove(it)?.viewModelStore?.clear() }
    }
    override fun onCleared() { stores.values.forEach { it.viewModelStore.clear() }; stores.clear() }
}
