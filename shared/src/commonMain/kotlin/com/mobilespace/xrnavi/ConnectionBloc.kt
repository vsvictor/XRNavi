package com.mobilespace.xrnavi

import com.vsvictor.jsonrpckmp.RpcTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class ConnectionStatus {
    Disconnected,
    Quic,
    Tls,
    Tcp
}

internal sealed interface ConnectionEvent {
    data class Connected(val transport: RpcTransport) : ConnectionEvent
    data object Disconnected : ConnectionEvent
    data object Failed : ConnectionEvent
    data object Exhausted : ConnectionEvent
}

internal class ConnectionBloc {
    private val mutableState = MutableStateFlow(ConnectionStatus.Disconnected)
    val state = mutableState.asStateFlow()
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()

    fun dispatch(event: ConnectionEvent) {
        if (event is ConnectionEvent.Connected) mutableError.value = null
        if (event == ConnectionEvent.Exhausted) {
            mutableError.value = "Помилка з'єднання: QUIC, TLS і TCP недоступні. Повторюємо підключення."
        }
        mutableState.value = when (event) {
            is ConnectionEvent.Connected -> when (event.transport) {
                RpcTransport.QUIC -> ConnectionStatus.Quic
                RpcTransport.TLS -> ConnectionStatus.Tls
                RpcTransport.TCP -> ConnectionStatus.Tcp
                else -> error("Unsupported connection indicator transport: ${event.transport}")
            }
            ConnectionEvent.Disconnected, ConnectionEvent.Failed, ConnectionEvent.Exhausted ->
                ConnectionStatus.Disconnected
        }
    }
}
