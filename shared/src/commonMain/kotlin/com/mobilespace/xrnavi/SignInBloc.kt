package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

internal const val signInTimeoutMillis = 15_000L

internal data class SignInState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val submitting: Boolean = false,
    val notice: String = "",
    val accountPickerRequested: Boolean = false,
    val signedIn: Boolean = false,
    val openMap: Boolean = false,
) {
    override fun toString(): String = "SignInState(submitting=$submitting, signedIn=$signedIn)"
}

internal sealed interface SignInEvent {
    class EmailChanged(val value: String) : SignInEvent
    class PasswordChanged(val value: String) : SignInEvent
    data object TogglePasswordVisibility : SignInEvent
    data object SelectAccount : SignInEvent
    data object AccountPickerLaunched : SignInEvent
    class AccountSelected(val email: String) : SignInEvent
    class AccountSelectionFailed(val message: String) : SignInEvent
    data object Submit : SignInEvent
    data object MapOpened : SignInEvent
    data object Cancel : SignInEvent
}

internal class SignInBloc(
    private val auth: SignInAuthService,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(SignInState())
    val state = mutableState.asStateFlow()
    private var request: Job? = null
    private var session: AuthSession? = null

    fun dispatch(event: SignInEvent) {
        val current = mutableState.value
        when (event) {
            is SignInEvent.EmailChanged -> if (!current.submitting) {
                mutableState.value = current.copy(email = event.value, notice = "")
            }
            is SignInEvent.PasswordChanged -> if (!current.submitting) {
                mutableState.value = current.copy(password = event.value, notice = "")
            }
            SignInEvent.TogglePasswordVisibility ->
                mutableState.value = current.copy(passwordVisible = !current.passwordVisible)
            SignInEvent.SelectAccount -> if (!current.submitting) {
                mutableState.value = current.copy(accountPickerRequested = true, notice = "")
            }
            SignInEvent.AccountPickerLaunched ->
                mutableState.value = current.copy(accountPickerRequested = false)
            is SignInEvent.AccountSelected -> if (!current.submitting) {
                val error = emailValidationError(event.email)
                mutableState.value = if (error == null) {
                    current.copy(email = event.email, notice = "", accountPickerRequested = false)
                } else {
                    current.copy(
                        notice = "Обраний акаунт не містить коректної електронної адреси.",
                        accountPickerRequested = false,
                    )
                }
            }
            is SignInEvent.AccountSelectionFailed ->
                mutableState.value = current.copy(
                    accountPickerRequested = false,
                    notice = event.message,
                )
            SignInEvent.Submit -> submit()
            SignInEvent.MapOpened -> mutableState.value = current.copy(openMap = false)
            SignInEvent.Cancel -> {
                request?.cancel()
                request = null
                session = null
                mutableState.value = SignInState(email = current.email)
            }
        }
    }

    private fun submit() {
        val current = mutableState.value
        if (current.submitting || current.signedIn) return
        val validation = emailValidationError(current.email)
            ?: if (current.password.isEmpty()) "Введіть пароль" else null
        if (validation != null) {
            mutableState.value = current.copy(notice = validation)
            return
        }
        mutableState.value = current.copy(submitting = true, notice = "")
        request = scope.launch {
            try {
                val authenticated = withTimeout(signInTimeoutMillis) {
                    auth.login(current.email.trim(), current.password)
                }
                currentCoroutineContext().ensureActive()
                session = authenticated
                mutableState.value = mutableState.value.copy(
                    password = "",
                    passwordVisible = false,
                    submitting = false,
                    signedIn = true,
                    openMap = true,
                )
            } catch (_: TimeoutCancellationException) {
                currentCoroutineContext().ensureActive()
                fail("Сервер не відповів вчасно. Спробуйте увійти ще раз.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: InvalidAuthResponseException) {
                currentCoroutineContext().ensureActive()
                fail("Сервер повернув некоректну відповідь на вхід.")
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                // Native errors may contain sensitive server data; never render them verbatim.
                fail("Не вдалося увійти. Перевірте пошту, пароль і з'єднання із сервером.")
            }
        }
    }

    private fun fail(message: String) {
        session = null
        mutableState.value = mutableState.value.copy(submitting = false, notice = message)
    }
}
