package com.mobilespace.xrnavi

import com.exkmptracker.bloc.TrackerState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

internal class TrackerIntegrationBloc(
    scope: CoroutineScope,
    private val sessions: StateFlow<Long?>,
    private val ids: DeviceIdProvider,
    private val registration: TrackerRegistrationService,
    private val runtime: AppTrackingRuntime,
    private val retryMillis: Long = 10_000,
) {
    private val mutableState = MutableStateFlow(TrackerIntegrationState())
    val state = mutableState.asStateFlow()
    private val retries = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch {
            sessions.collectLatest { generation ->
                // Account boundaries stop collection before another owner can start it.
                while (!stopRuntime()) {
                    withTimeoutOrNull(retryMillis) { retries.receive() }
                }
                mutableState.value = TrackerIntegrationState(authenticated = generation != null)
                if (generation == null) return@collectLatest
                while (currentCoroutineContext().isActive) {
                    while (!stopRuntime()) {
                        withTimeoutOrNull(retryMillis) { retries.receive() }
                    }
                    var registrationSucceeded = false
                    try {
                        mutableState.value = state.value.copy(registering = true, problem = null)
                        val id = ids.deviceId()
                        val registered = withTimeout(signInTimeoutMillis) { registration.open(id) }
                        currentCoroutineContext().ensureActive()
                        if (sessions.value != generation) return@collectLatest
                        registrationSucceeded = true
                        mutableState.value = state.value.copy(registering = false, trackerId = registered.trackerId)
                        coroutineScope {
                            launch {
                                runtime.state.collect { tracking ->
                                    mutableState.value = state.value.copy(tracking = tracking,
                                        problem = if (tracking.error != null) TrackerIntegrationProblem.Runtime else null)
                                }
                            }
                            runtime.start(registered.session)
                            awaitCancellation()
                        }
                    } catch (cancelled: CancellationException) {
                        if (cancelled !is TimeoutCancellationException) throw cancelled
                        mutableState.value = state.value.copy(registering = false,
                            problem = if (registrationSucceeded) TrackerIntegrationProblem.Runtime
                                else TrackerIntegrationProblem.Registration)
                    } catch (_: Exception) {
                        mutableState.value = state.value.copy(registering = false,
                            problem = if (registrationSucceeded) TrackerIntegrationProblem.Runtime
                                else TrackerIntegrationProblem.Registration)
                    } finally {
                        mutableState.value = state.value.copy(
                            authenticated = sessions.value != null,
                            tracking = TrackerState(),
                        )
                        withContext(NonCancellable) { stopRuntime() }
                    }
                    withTimeoutOrNull(retryMillis) { retries.receive() }
                }
            }
        }
    }

    fun retry() { retries.trySend(Unit) }

    private suspend fun stopRuntime(): Boolean = try {
        withTimeout(10_000) { runtime.stop() }
        true
    } catch (cancelled: CancellationException) {
        if (cancelled !is TimeoutCancellationException) throw cancelled
        mutableState.value = state.value.copy(problem = TrackerIntegrationProblem.Runtime)
        false
    } catch (_: Exception) {
        mutableState.value = state.value.copy(problem = TrackerIntegrationProblem.Runtime)
        false
    }
}
