package com.mobilespace.xrnavi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Owns the server-callback subscription of the signed-in user. Tokens are never logged. */
internal class CallbackBloc(
    private val service: CallbackService,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(CallbackState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    fun dispatch(event: CallbackEvent) {
        mutableState.value = transition(mutableState.value, event)
        when (event) {
            CallbackEvent.Started -> if (job?.isActive != true) {
                job = scope.launch {
                    service.run(
                        onResetToken = { dispatch(CallbackEvent.ResetTokenReceived(it)) },
                        onFailure = { dispatch(CallbackEvent.Failed) },
                    )
                }
            }
            CallbackEvent.Stopped -> {
                job?.cancel()
                job = null
            }
            else -> Unit
        }
    }

    companion object {
        fun transition(state: CallbackState, event: CallbackEvent): CallbackState = when (event) {
            CallbackEvent.Started -> state.copy(active = true, failed = false)
            CallbackEvent.Stopped -> CallbackState()
            is CallbackEvent.ResetTokenReceived -> state.copy(resetToken = event.token)
            CallbackEvent.ResetTokenDismissed -> state.copy(resetToken = null)
            CallbackEvent.Failed -> state.copy(failed = true)
        }
    }
}
