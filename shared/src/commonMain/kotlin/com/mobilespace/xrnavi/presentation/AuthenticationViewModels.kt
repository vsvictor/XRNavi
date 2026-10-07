package com.mobilespace.xrnavi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilespace.xrnavi.domain.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import xrnavi.shared.generated.resources.*

enum class AuthenticationEffect { Authenticated, Register, SignIn, Invitation }
data class AuthenticationUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val personalAccount: Boolean = true,
    val consentGiven: Boolean = false,
    val errors: AuthenticationErrors = AuthenticationErrors(),
    val statusMessage: StringResource? = null,
    val isSubmitting: Boolean = false,
) {
    val passwordStrong get() = password.isEmpty() || password.length >= 8
}

abstract class AuthenticationViewModel : ViewModel() {
    protected val mutableState = MutableStateFlow(AuthenticationUiState())
    val state = mutableState.asStateFlow()
    private val channel = Channel<AuthenticationEffect>(Channel.BUFFERED)
    val effects = channel.receiveAsFlow()
    fun navigate(effect: AuthenticationEffect) { channel.trySend(effect) }
    fun emailChanged(value: String) = mutableState.update {
        it.copy(email = value, errors = it.errors.copy(email = null), statusMessage = null)
    }
    fun passwordChanged(value: String) = mutableState.update {
        it.copy(password = value, errors = it.errors.copy(password = null), statusMessage = null)
    }
    fun notice(message: StringResource) { mutableState.update { it.copy(statusMessage = message) } }
    protected fun submit(unavailable: StringResource, action: suspend () -> AuthenticationResult) {
        if (state.value.isSubmitting) return
        mutableState.update { it.copy(isSubmitting = true, statusMessage = null) }
        viewModelScope.launch {
            try {
                when (val result = action()) {
                    AuthenticationResult.Authenticated -> navigate(AuthenticationEffect.Authenticated)
                    AuthenticationResult.NotConfigured -> notice(unavailable)
                    is AuthenticationResult.Invalid -> mutableState.update { it.copy(errors = result.errors) }
                }
            } finally {
                mutableState.update { it.copy(isSubmitting = false) }
            }
        }
    }
    override fun onCleared() { channel.close() }
}
class SignInViewModel(private val signIn: SignIn) : AuthenticationViewModel() {
    fun submit() {
        val credentials = state.value.let { Credentials(it.email, it.password) }
        submit(Res.string.ui_avtoryzatsiyu_shche_ne_pidklyucheno_vashi_dani_ne_nadsylalysya) { signIn(credentials) }
    }
    fun recoverPassword() = notice(Res.string.ui_vidnovlennya_parolya_bude_dostupne_pislya_pidklyuchennya_servisu)
    fun apple() = notice(Res.string.ui_vkhid_cherez_apple_shche_ne_pidklyucheno)
    fun google() = notice(Res.string.ui_vkhid_cherez_google_shche_ne_pidklyucheno)
    fun corporate() = notice(Res.string.ui_korporatyvnyy_vkhid_shche_ne_pidklyucheno)
}
class RegistrationViewModel(private val register: Register) : AuthenticationViewModel() {
    fun nameChanged(value: String) = mutableState.update {
        it.copy(fullName = value, errors = it.errors.copy(fullName = null), statusMessage = null)
    }
    fun selectAccount(personal: Boolean) = mutableState.update { it.copy(personalAccount = personal, statusMessage = null) }
    fun toggleConsent() = mutableState.update { it.copy(consentGiven = !it.consentGiven, errors = it.errors.copy(consent = null)) }
    fun help() = notice(Res.string.ui_stvorit_osobystyy_profil_abo_skorystaytesya_zaproshennyam_orhani)
    fun submit() {
        val request = state.value.let {
            Registration(it.fullName, Credentials(it.email, it.password),
                if (it.personalAccount) AccountType.Personal else AccountType.Corporate, it.consentGiven)
        }
        submit(Res.string.ui_reyestratsiyu_shche_ne_pidklyucheno_vashi_dani_ne_nadsylalysya) { register(request) }
    }
}
