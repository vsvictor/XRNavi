package com.mobilespace.xrnavi

import com.vsvictor.jsonrpckmp.JsonRpcClient
import com.vsvictor.jsonrpckmp.JsonRpcClientBuilder
import com.vsvictor.jsonrpckmp.RpcTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import xrnavi.shared.generated.resources.Res

internal interface RpcConnection {
    val transport: RpcTransport
    val client: JsonRpcClient
    suspend fun ping()
    suspend fun restore()
    suspend fun close()
}

internal class MobileSpaceRpcClient(
    private val connectionBloc: ConnectionBloc,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val connect: suspend (RpcTransport) -> RpcConnection = { transport ->
        NativeRpcConnection(transport, connectMobileSpaceRpcClient(transport = transport))
    },
    private val pingIntervalMillis: Long = Constants.MOBILESPACE_RPC_PING_INTERVAL_MS.toLong(),
    private val maxMissedPongs: UInt = Constants.MOBILESPACE_RPC_MAX_MISSED_PONGS,
    private val upgradeIntervalMillis: Long = Constants.RPC_CONNECTION_TRANSPORT_UP_MS,
) {
    private val startMutex = Mutex()
    private val active = MutableStateFlow<RpcConnection?>(null)
    private var connectionJob: Job? = null
    private val transports = listOf(RpcTransport.QUIC, RpcTransport.TLS, RpcTransport.TCP)

    init {
        require(pingIntervalMillis > 0)
        require(maxMissedPongs > 0u)
        require(upgradeIntervalMillis > 0)
    }

    suspend fun start() = startMutex.withLock {
        if (connectionJob?.isActive != true) {
            connectionJob = scope.launch { runConnections() }
        }
    }

    /** Emits every newly active connection's client, so per-connection state can be re-established. */
    val clients: Flow<JsonRpcClient> = active.filterNotNull().map { it.client }.distinctUntilChanged()

    suspend fun awaitClient(): JsonRpcClient {
        start()
        return active.filterNotNull().first().client
    }

    private suspend fun runConnections() {
        var nextTransport = 0
        var owned: RpcConnection? = null
        try {
            while (true) {
                val connection = active.value ?: connectCandidate(transports[nextTransport])
                if (connection == null) {
                    nextTransport = (nextTransport + 1) % transports.size
                    if (nextTransport == 0) {
                        connectionBloc.dispatch(ConnectionEvent.Exhausted)
                        delay(upgradeIntervalMillis)
                    }
                    continue
                }
                owned = connection
                active.value = connection
                connectionBloc.dispatch(ConnectionEvent.Connected(connection.transport))
                val replacement = runSession(connection)
                if (replacement != null) {
                    active.value = replacement
                    if (replacement !== connection) closeConnection(connection)
                    owned = replacement
                } else {
                    active.value = null
                    closeConnection(connection)
                    owned = null
                    // A lost TCP connection gets one fresh TCP connect before restarting at QUIC.
                    nextTransport = (transports.indexOf(connection.transport) + 1)
                        .coerceAtMost(transports.lastIndex)
                }
            }
        } finally {
            val connection = active.value
            active.value = null
            connectionBloc.dispatch(ConnectionEvent.Disconnected)
            connection?.let { closeConnection(it) }
            owned?.takeIf { it !== connection }?.let { closeConnection(it) }
        }
    }

    private sealed interface SessionEvent {
        data object Pong : SessionEvent
        data object MissedPong : SessionEvent
        data class Upgrade(val connection: RpcConnection) : SessionEvent
    }

    private suspend fun runSession(connection: RpcConnection): RpcConnection? = coroutineScope {
        val events = Channel<SessionEvent>(Channel.UNLIMITED)
        val monitor = launch {
            while (true) {
                delay(pingIntervalMillis)
                val pong = attempt("ping ${connection.transport}") { connection.ping() }
                events.send(if (pong) SessionEvent.Pong else SessionEvent.MissedPong)
            }
        }
        val upgrade = launch {
            val preferred = transports.take(transports.indexOf(connection.transport))
            if (preferred.isEmpty()) return@launch
            val ticks = Channel<Unit>(Channel.CONFLATED)
            val timer = launch {
                while (true) {
                    delay(upgradeIntervalMillis)
                    ticks.send(Unit)
                }
            }
            var candidate: RpcConnection? = null
            try {
                for (tick in ticks) {
                    for (transport in preferred) {
                        candidate = connectCandidate(transport)
                        if (candidate != null) {
                            events.send(SessionEvent.Upgrade(candidate))
                            awaitCancellation()
                        }
                    }
                }
            } finally {
                withContext(NonCancellable) {
                    timer.cancelAndJoin()
                    ticks.close()
                    candidate?.takeIf { it !== active.value }?.let { closeConnection(it) }
                }
            }
        }
        var missed = 0u
        try {
            while (true) {
                when (val event = events.receive()) {
                    SessionEvent.Pong -> {
                        missed = 0u
                        connectionBloc.dispatch(ConnectionEvent.Connected(connection.transport))
                    }
                    SessionEvent.MissedPong -> {
                        missed++
                        connectionBloc.dispatch(ConnectionEvent.Failed)
                        if (missed >= maxMissedPongs) {
                            active.value = null
                            monitor.cancelAndJoin()
                            upgrade.cancelAndJoin()
                            val restored = attempt("restore ${connection.transport}") {
                                connection.restore()
                                connection.ping()
                            }
                            return@coroutineScope if (restored) connection else null
                        }
                    }
                    is SessionEvent.Upgrade -> {
                        // Transfer ownership before cancelling the probe's cleanup.
                        active.value = event.connection
                        connectionBloc.dispatch(ConnectionEvent.Connected(event.connection.transport))
                        return@coroutineScope event.connection
                    }
                }
            }
            @Suppress("UNREACHABLE_CODE")
            null
        } finally {
            withContext(NonCancellable) {
                monitor.cancelAndJoin()
                upgrade.cancelAndJoin()
                events.close()
            }
        }
    }

    private suspend fun connectCandidate(transport: RpcTransport): RpcConnection? {
        var candidate: RpcConnection? = null
        var accepted = false
        try {
            candidate = connect(transport)
            var missed = 0u
            while (missed < maxMissedPongs) {
                if (attempt("initial ping $transport") { candidate.ping() }) {
                    accepted = true
                    return candidate
                }
                if (active.value == null) connectionBloc.dispatch(ConnectionEvent.Failed)
                missed++
                if (missed < maxMissedPongs) delay(pingIntervalMillis)
            }
            if (attempt("initial restore $transport") {
                    candidate.restore()
                    candidate.ping()
                }
            ) {
                accepted = true
                return candidate
            }
            return null
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            println("MobileSpace JSON-RPC connect $transport failed: ${error.message}")
            if (active.value == null) connectionBloc.dispatch(ConnectionEvent.Failed)
            return null
        } finally {
            if (!accepted) candidate?.let { closeConnection(it) }
        }
    }

    private suspend fun attempt(operation: String, block: suspend () -> Unit): Boolean =
        try {
            block()
            true
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            println("MobileSpace JSON-RPC $operation failed: ${error.message}")
            false
        }

    private suspend fun closeConnection(connection: RpcConnection) = withContext(NonCancellable) {
        attempt("close ${connection.transport}") { connection.close() }
        Unit
    }
}

private class NativeRpcConnection(
    override val transport: RpcTransport,
    override val client: JsonRpcClient
) : RpcConnection {
    override suspend fun ping() {
        client.transportPing(Constants.MOBILESPACE_RPC_PING_INTERVAL_MS)
        client.takeLivenessLog()
    }

    override suspend fun restore() {
        client.restoreConnection(Constants.MOBILESPACE_RPC_PING_INTERVAL_MS)
    }

    override suspend fun close() = client.disconnect()
}

internal suspend fun connectMobileSpaceRpcClient(
    host: String = Constants.MOBILESPACE_RPC_HOST,
    transport: RpcTransport = RpcTransport.QUIC,
): JsonRpcClient {
    val caPem = Res.readBytes("files/mobilespace_app_ca.pem").decodeToString()
    return JsonRpcClientBuilder.builder(
        transport = transport,
        host = host,
        port = Constants.MOBILESPACE_RPC_PORT,
    )
        .caCertificatePem(caPem)
        .serverName(Constants.MOBILESPACE_RPC_HOST)
        .connectTimeout(Constants.RPC_CONNECTION_TRANSPORT_UP_MS.toULong())
        .pingFrequency(0uL)
        .maxMissedPongs(Constants.MOBILESPACE_RPC_MAX_MISSED_PONGS)
        .connect()
}
