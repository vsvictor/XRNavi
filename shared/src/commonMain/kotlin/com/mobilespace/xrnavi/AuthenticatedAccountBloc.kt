package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

internal enum class AccountMode { SignedOut, Demo, Authenticated }

internal data class AuthenticatedAccountState(
    val mode: AccountMode = AccountMode.SignedOut,
    val section: DemoSection = DemoSection.Map,
    val profile: AuthenticatedProfile? = null,
    val loading: Boolean = false,
    val error: String = "",
    val expired: Boolean = false,
) {
    override fun toString(): String = "AuthenticatedAccountState(mode=$mode, loading=$loading)"
}

internal sealed interface AuthenticatedAccountEvent {
    class LoginCompleted(val session: AuthSession) : AuthenticatedAccountEvent
    class OpenSection(val section: DemoSection) : AuthenticatedAccountEvent
    data object RetryProfile : AuthenticatedAccountEvent
    data object ContinueWithoutLogin : AuthenticatedAccountEvent
    data object Logout : AuthenticatedAccountEvent
}

internal class AuthenticatedAccountBloc(
    private val profiles: AuthenticatedProfileService,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(AuthenticatedAccountState())
    val state = mutableState.asStateFlow()
    private var session: AuthSession? = null
    fun currentSession(): AuthSession? = session
    private var request: Job? = null
    private var expiry: Job? = null

    fun dispatch(event: AuthenticatedAccountEvent) {
        when (event) {
            is AuthenticatedAccountEvent.LoginCompleted -> {
                clearSession()
                session = event.session
                mutableState.value = AuthenticatedAccountState(mode = AccountMode.Authenticated)
                val expiryDelayMillis = event.session.expiresInSeconds.coerceAtMost(
                    (Long.MAX_VALUE / 1000).toULong()
                ).toLong() * 1000
                expiry = scope.launch {
                    delay(expiryDelayMillis)
                    request?.cancel()
                    request = null
                    expiry = null
                    session = null
                    mutableState.value = mutableState.value.copy(
                        profile = null, loading = false, expired = true,
                        error = "Термін сесії завершився. Вийдіть та увійдіть знову.",
                    )
                }
                loadProfile()
            }
            is AuthenticatedAccountEvent.OpenSection -> if (
                mutableState.value.mode == AccountMode.Authenticated &&
                event.section in listOf(
                    DemoSection.Map, DemoSection.Garage, DemoSection.Settings, DemoSection.Profile,
                )
            ) {
                mutableState.value = mutableState.value.copy(section = event.section)
            }
            AuthenticatedAccountEvent.RetryProfile -> loadProfile()
            AuthenticatedAccountEvent.ContinueWithoutLogin -> {
                clearSession()
                mutableState.value = AuthenticatedAccountState(mode = AccountMode.Demo)
            }
            AuthenticatedAccountEvent.Logout -> {
                clearSession()
                mutableState.value = AuthenticatedAccountState()
            }
        }
    }

    private fun clearSession() {
        request?.cancel()
        expiry?.cancel()
        request = null
        expiry = null
        session = null
    }

    private fun loadProfile() {
        val authenticated = session ?: return
        if (mutableState.value.loading) return
        mutableState.value = mutableState.value.copy(loading = true, error = "", profile = null)
        request = scope.launch {
            try {
                val profile = withTimeout(signInTimeoutMillis) { profiles.getProfile(authenticated) }
                currentCoroutineContext().ensureActive()
                mutableState.value = mutableState.value.copy(profile = profile, loading = false)
            } catch (_: TimeoutCancellationException) {
                currentCoroutineContext().ensureActive()
                fail("Сервер не відповів вчасно. Повторіть завантаження профілю.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: InvalidProfileResponseException) {
                currentCoroutineContext().ensureActive()
                fail("Сервер повернув некоректний профіль. Повторіть завантаження.")
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                fail("Не вдалося завантажити профіль. Перевірте з'єднання та повторіть. Якщо сесія недійсна, увійдіть знову.")
            }
        }
    }

    private fun fail(message: String) {
        mutableState.value = mutableState.value.copy(profile = null, loading = false, error = message)
    }
}
