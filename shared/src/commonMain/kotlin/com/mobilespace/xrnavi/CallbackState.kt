package com.mobilespace.xrnavi

internal data class CallbackState(
    val active: Boolean = false,
    val resetToken: String? = null,
    val failed: Boolean = false,
)

internal sealed interface CallbackEvent {
    data object Started : CallbackEvent
    data object Stopped : CallbackEvent
    data class ResetTokenReceived(val token: String) : CallbackEvent
    data object ResetTokenDismissed : CallbackEvent
    data object Failed : CallbackEvent
}
