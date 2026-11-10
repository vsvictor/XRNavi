package com.mobilespace.xrnavi

internal const val SplashDurationMillis = 2_400L

internal data class SplashState(
    val elapsedMillis: Long = 0,
    val finished: Boolean = false,
)

internal sealed interface SplashEvent {
    data class Frame(val timeNanos: Long) : SplashEvent
}
