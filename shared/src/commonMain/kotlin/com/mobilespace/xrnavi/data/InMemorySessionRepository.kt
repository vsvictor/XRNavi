package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.SessionRepository
import com.mobilespace.xrnavi.domain.SessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemorySessionRepository(initial: SessionState = SessionState()) : SessionRepository {
    private val mutableState = MutableStateFlow(initial)
    override val state = mutableState.asStateFlow()
    override fun update(transform: (SessionState) -> SessionState) = mutableState.update(transform)
}
