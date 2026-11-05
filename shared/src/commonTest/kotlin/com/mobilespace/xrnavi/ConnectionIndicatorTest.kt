package com.mobilespace.xrnavi

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vsvictor.jsonrpckmp.RpcTransport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ConnectionIndicatorTest {
    @Test
    fun startsDisconnectedAndMapsAllSupportedTransports() {
        val bloc = ConnectionBloc()
        assertEquals(ConnectionStatus.Disconnected, bloc.state.value)
        listOf(
            RpcTransport.QUIC to ConnectionStatus.Quic,
            RpcTransport.TLS to ConnectionStatus.Tls,
            RpcTransport.TCP to ConnectionStatus.Tcp
        ).forEach { (transport, expected) ->
            bloc.dispatch(ConnectionEvent.Connected(transport))
            assertEquals(expected, bloc.state.value)
            bloc.dispatch(ConnectionEvent.Failed)
            assertEquals(ConnectionStatus.Disconnected, bloc.state.value)
            bloc.dispatch(ConnectionEvent.Connected(transport))
            bloc.dispatch(ConnectionEvent.Disconnected)
            assertEquals(ConnectionStatus.Disconnected, bloc.state.value)
        }
    }

    @Test
    fun unsupportedTransportIsNotReportedAsConnected() {
        val bloc = ConnectionBloc()
        assertFailsWith<IllegalStateException> {
            bloc.dispatch(ConnectionEvent.Connected(RpcTransport.UDP))
        }
        assertEquals(ConnectionStatus.Disconnected, bloc.state.value)
    }

    @Test
    fun exhaustedTransportsExposeAnErrorUntilConnectionIsVerified() {
        val bloc = ConnectionBloc()
        bloc.dispatch(ConnectionEvent.Exhausted)
        assertEquals(ConnectionStatus.Disconnected, bloc.state.value)
        assertEquals(
            "Помилка з'єднання: QUIC, TLS і TCP недоступні. Повторюємо підключення.",
            bloc.error.value
        )
        bloc.dispatch(ConnectionEvent.Failed)
        kotlin.test.assertNotNull(bloc.error.value)
        bloc.dispatch(ConnectionEvent.Connected(RpcTransport.TLS))
        assertNull(bloc.error.value)
    }

    @Test
    fun diameterOffsetsAndColorsMatchTheScreenContract() {
        assertEquals(5.dp, ConnectionIndicatorDiameter)
        assertEquals(8.dp, ConnectionIndicatorEdgeOffset)
        assertEquals(Color(0xFF00FF00), ConnectionStatus.Quic.indicatorColor)
        assertEquals(Color(0xFF0000FF), ConnectionStatus.Tls.indicatorColor)
        assertEquals(Color(0xFF00FFFF), ConnectionStatus.Tcp.indicatorColor)
        assertEquals(Color(0xFFFF0000), ConnectionStatus.Disconnected.indicatorColor)
    }
}
