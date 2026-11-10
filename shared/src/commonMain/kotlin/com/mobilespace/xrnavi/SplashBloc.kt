package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class SplashBloc {
    private val mutableState = MutableStateFlow(SplashState())
    val state = mutableState.asStateFlow()
    private var startedAtNanos: Long? = null

    fun dispatch(event: SplashEvent) {
        when (event) {
            is SplashEvent.Frame -> {
                if (mutableState.value.finished) return
                val start = startedAtNanos ?: event.timeNanos.also { startedAtNanos = it }
                val elapsed = ((event.timeNanos - start) / 1_000_000L)
                    .coerceIn(mutableState.value.elapsedMillis, SplashDurationMillis)
                mutableState.value = SplashState(
                    elapsedMillis = elapsed,
                    finished = elapsed == SplashDurationMillis,
                )
            }
        }
    }
}
