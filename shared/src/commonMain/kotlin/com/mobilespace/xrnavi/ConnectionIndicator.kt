package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

internal val ConnectionIndicatorDiameter = 5.dp
internal val ConnectionIndicatorEdgeOffset = 8.dp

internal val ConnectionStatus.indicatorColor: Color
    get() = when (this) {
        ConnectionStatus.Quic -> Color(0xFF00FF00)
        ConnectionStatus.Tls -> Color(0xFF0000FF)
        ConnectionStatus.Tcp -> Color(0xFF00FFFF)
        ConnectionStatus.Disconnected -> Color(0xFFFF0000)
    }

@Composable
internal fun ConnectionIndicator(status: ConnectionStatus, modifier: Modifier = Modifier) {
    val description = when (status) {
        ConnectionStatus.Quic -> "З'єднання: QUIC"
        ConnectionStatus.Tls -> "З'єднання: TLS"
        ConnectionStatus.Tcp -> "З'єднання: TCP"
        ConnectionStatus.Disconnected -> "Немає з'єднання або помилка з'єднання"
    }
    Box(
        modifier = modifier
            .size(ConnectionIndicatorDiameter)
            .background(status.indicatorColor, CircleShape)
            .testTag("connection_indicator")
            .semantics { contentDescription = description }
    )
}
