package com.mobilespace.xrnavi

import com.vsvictor.jsonrpckmp.JsonRpcClient
import com.vsvictor.jsonrpckmp.RpcTransport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MobileSpaceRpcClientTest {
    @Test
    fun immediatePongIsRequiredBeforeShowingConnectedAndPingsRepeatAtInterval() = runTest {
        val fixture = Fixture(this)
        val quic = fixture.offer(RpcTransport.QUIC)
        val pong = CompletableDeferred<Unit>()
        quic.pings.addLast { pong.await() }
        fixture.start()
        assertEquals(1, quic.pingCount)
        assertEquals(ConnectionStatus.Disconnected, fixture.bloc.state.value)
        pong.complete(Unit)
        runCurrent()
        assertEquals(ConnectionStatus.Quic, fixture.bloc.state.value)
        advanceTimeBy(99)
        runCurrent()
        assertEquals(1, quic.pingCount)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, quic.pingCount)
    }

    @Test
    fun missingPongsMustBeConsecutiveAndSuccessfulRestoreKeepsConnection() = runTest {
        val fixture = Fixture(this)
        val quic = fixture.offer(RpcTransport.QUIC)
        fixture.start()
        quic.pings.addAll(listOf(
            { error("miss 1") }, { error("miss 2") }, {},
            { error("miss 1 again") }, { error("miss 2 again") }, { error("miss 3") }
        ))
        fixture.tick(500)
        assertEquals(0, quic.restoreCount)
        fixture.tick(100)
        assertEquals(1, quic.restoreCount)
        assertEquals(ConnectionStatus.Quic, fixture.bloc.state.value)
        assertEquals(0, quic.closeCount)
        assertEquals(listOf(RpcTransport.QUIC), fixture.attempts)
    }

    @Test
    fun failedRestoreClosesQuicBeforeFallingBackToTls() = runTest {
        val fixture = Fixture(this)
        val quic = fixture.offer(RpcTransport.QUIC)
        fixture.offer(RpcTransport.TLS)
        fixture.start()
        quic.failNextPings(3)
        quic.restoreFails = true
        fixture.tick(300)
        assertEquals(ConnectionStatus.Tls, fixture.bloc.state.value)
        assertEquals(1, quic.closeCount)
        assertTrue(fixture.events.indexOf("close QUIC") < fixture.events.indexOf("connect TLS"))
    }

    @Test
    fun restoredConnectionWithoutPongIsRejectedAndTlsFallsBackToTcp() = runTest {
        val fixture = Fixture(this)
        val tls = fixture.offer(RpcTransport.TLS)
        fixture.offer(RpcTransport.TCP)
        fixture.start()
        tls.failNextPings(4)
        fixture.tick(300)
        assertEquals(1, tls.restoreCount)
        assertEquals(1, tls.closeCount)
        assertEquals(ConnectionStatus.Tcp, fixture.bloc.state.value)
    }

    @Test
    fun exhaustionShowsErrorThenRestartsAtQuicAfterUpgradeInterval() = runTest {
        val fixture = Fixture(this)
        fixture.start()
        assertEquals(listOf(RpcTransport.QUIC, RpcTransport.TLS, RpcTransport.TCP), fixture.attempts)
        assertNotNull(fixture.bloc.error.value)
        fixture.offer(RpcTransport.QUIC)
        fixture.tick(999)
        assertEquals(3, fixture.attempts.size)
        fixture.tick(1)
        assertEquals(ConnectionStatus.Quic, fixture.bloc.state.value)
        assertNull(fixture.bloc.error.value)
        assertEquals(RpcTransport.QUIC, fixture.attempts.last())
    }

    @Test
    fun tlsUpgradeKeepsOldConnectionAndHeartbeatsUntilQuicPong() = runTest {
        val fixture = Fixture(this)
        val tls = fixture.offer(RpcTransport.TLS)
        fixture.start()
        val quic = fixture.offer(RpcTransport.QUIC)
        val pong = CompletableDeferred<Unit>()
        quic.pings.addLast { pong.await() }
        fixture.tick(1000)
        assertEquals(ConnectionStatus.Tls, fixture.bloc.state.value)
        assertEquals(0, tls.closeCount)
        val previousPings = tls.pingCount
        fixture.tick(200)
        assertTrue(tls.pingCount > previousPings)
        pong.complete(Unit)
        runCurrent()
        assertEquals(ConnectionStatus.Quic, fixture.bloc.state.value)
        assertEquals(1, tls.closeCount)
        assertEquals(0, quic.closeCount)
    }

    @Test
    fun failedUpgradePingClosesCandidateButNotHealthyTls() = runTest {
        val fixture = Fixture(this)
        val tls = fixture.offer(RpcTransport.TLS)
        fixture.start()
        val quic = fixture.offer(RpcTransport.QUIC)
        quic.failNextPings(3)
        quic.restoreFails = true
        fixture.tick(1200)
        assertEquals(ConnectionStatus.Tls, fixture.bloc.state.value)
        assertEquals(1, quic.closeCount)
        assertEquals(0, tls.closeCount)
        assertNull(fixture.bloc.error.value)
        fixture.tick(1000)
        assertEquals(3, fixture.attempts.count { it == RpcTransport.QUIC })
        assertEquals(ConnectionStatus.Tls, fixture.bloc.state.value)
    }

    @Test
    fun initialMissingPongsReachThresholdAndRestoreBeforeTransportFallback() = runTest {
        val fixture = Fixture(this)
        val quic = fixture.offer(RpcTransport.QUIC)
        fixture.offer(RpcTransport.TLS)
        quic.failNextPings(3)
        quic.restoreFails = true
        fixture.start()
        assertEquals(1, quic.pingCount)
        assertEquals(listOf(RpcTransport.QUIC), fixture.attempts)
        fixture.tick(199)
        assertEquals(0, quic.restoreCount)
        fixture.tick(1)
        assertEquals(3, quic.pingCount)
        assertEquals(1, quic.restoreCount)
        assertEquals(1, quic.closeCount)
        assertEquals(ConnectionStatus.Tls, fixture.bloc.state.value)
    }

    @Test
    fun upgradeScheduleDoesNotDriftWithFailedProbeDuration() = runTest {
        val fixture = Fixture(this)
        fixture.offer(RpcTransport.TLS)
        fixture.start()
        val quic = fixture.offer(RpcTransport.QUIC)
        quic.failNextPings(3)
        quic.restoreFails = true
        fixture.tick(1200)
        assertEquals(2, fixture.attempts.count { it == RpcTransport.QUIC })
        fixture.tick(799)
        assertEquals(2, fixture.attempts.count { it == RpcTransport.QUIC })
        fixture.tick(1)
        assertEquals(3, fixture.attempts.count { it == RpcTransport.QUIC })
    }

    @Test
    fun initialRestoreStillRequiresPongAndCanRetainQuic() = runTest {
        val fixture = Fixture(this)
        val quic = fixture.offer(RpcTransport.QUIC)
        quic.failNextPings(3)
        fixture.start()
        fixture.tick(200)
        assertEquals(1, quic.restoreCount)
        assertEquals(4, quic.pingCount)
        assertEquals(ConnectionStatus.Quic, fixture.bloc.state.value)
        assertEquals(0, quic.closeCount)
        assertEquals(listOf(RpcTransport.QUIC), fixture.attempts)
    }

    @Test
    fun tcpCanUpgradeDirectlyToQuicWithoutTryingTls() = runTest {
        val fixture = Fixture(this)
        val tcp = fixture.offer(RpcTransport.TCP)
        fixture.start()
        fixture.offer(RpcTransport.QUIC)
        fixture.tick(1000)
        assertEquals(ConnectionStatus.Quic, fixture.bloc.state.value)
        assertEquals(1, tcp.closeCount)
        assertEquals(1, fixture.attempts.count { it == RpcTransport.TLS })
    }

    @Test
    fun failingActiveConnectionCancelsProbeBeforeRestoringIt() = runTest {
        val fixture = Fixture(this)
        val tcp = fixture.offer(RpcTransport.TCP)
        fixture.start()
        val quic = fixture.offer(RpcTransport.QUIC)
        quic.pings.addLast { CompletableDeferred<Unit>().await() }
        fixture.tick(1000)
        tcp.failNextPings(3)
        fixture.tick(300)
        assertEquals(1, quic.closeCount)
        assertEquals(1, tcp.restoreCount)
        assertEquals(0, tcp.closeCount)
        assertEquals(ConnectionStatus.Tcp, fixture.bloc.state.value)
        assertTrue(fixture.events.indexOf("close QUIC") < fixture.events.indexOf("restore TCP"))
    }

    @Test
    fun tcpUpgradeTriesQuicThenTlsAndClosesTcpOnlyAfterVerifiedPong() = runTest {
        val fixture = Fixture(this)
        val tcp = fixture.offer(RpcTransport.TCP)
        fixture.start()
        val tls = fixture.offer(RpcTransport.TLS)
        fixture.tick(1000)
        assertEquals(
            listOf(RpcTransport.QUIC, RpcTransport.TLS, RpcTransport.TCP, RpcTransport.QUIC, RpcTransport.TLS),
            fixture.attempts
        )
        assertEquals(ConnectionStatus.Tls, fixture.bloc.state.value)
        assertEquals(1, tls.pingCount)
        assertEquals(1, tcp.closeCount)
    }

    @Test
    fun failedBothUpgradeProbesKeepHealthyTcpAndRetry() = runTest {
        val fixture = Fixture(this)
        val tcp = fixture.offer(RpcTransport.TCP)
        fixture.start()
        fixture.tick(2000)
        assertEquals(ConnectionStatus.Tcp, fixture.bloc.state.value)
        assertEquals(0, tcp.closeCount)
        assertEquals(3, fixture.attempts.count { it == RpcTransport.QUIC })
        assertTrue(tcp.pingCount >= 21)
    }

    @Test
    fun deadTcpIsRestoredThenClosedAndCycleRestartsAtQuic() = runTest {
        val fixture = Fixture(this)
        val tcp = fixture.offer(RpcTransport.TCP)
        fixture.start()
        tcp.failNextPings(3)
        tcp.restoreFails = true
        fixture.tick(300)
        assertEquals(1, tcp.restoreCount)
        assertEquals(1, tcp.closeCount)
        assertNotNull(fixture.bloc.error.value)
        fixture.offer(RpcTransport.QUIC)
        fixture.tick(1000)
        assertEquals(ConnectionStatus.Quic, fixture.bloc.state.value)
    }

    @Test
    fun lostTcpReconnectsBeforeResumingUpgradeProbes() = runTest {
        val fixture = Fixture(this)
        val tcp = fixture.offer(RpcTransport.TCP)
        fixture.start()
        val reconnected = fixture.offer(RpcTransport.TCP)
        tcp.failNextPings(3)
        tcp.restoreFails = true
        fixture.tick(300)
        assertEquals(1, tcp.closeCount)
        assertEquals(1, reconnected.pingCount)
        assertEquals(ConnectionStatus.Tcp, fixture.bloc.state.value)
        assertTrue(fixture.events.indexOf("close TCP") < fixture.events.lastIndexOf("connect TCP"))
        fixture.offer(RpcTransport.QUIC)
        fixture.tick(999)
        assertEquals(ConnectionStatus.Tcp, fixture.bloc.state.value)
        fixture.tick(1)
        assertEquals(ConnectionStatus.Quic, fixture.bloc.state.value)
        assertEquals(1, reconnected.closeCount)
    }

    @Test
    fun cancellingDuringRestoreClosesOldConnectionAndDoesNotFallback() = runTest {
        val fixture = Fixture(this)
        val quic = fixture.offer(RpcTransport.QUIC)
        fixture.start()
        quic.failNextPings(3)
        quic.restoreAction = { CompletableDeferred<Unit>().await() }
        fixture.tick(300)
        backgroundScope.cancel()
        runCurrent()
        assertEquals(1, quic.closeCount)
        assertEquals(listOf(RpcTransport.QUIC), fixture.attempts)
        assertEquals(ConnectionStatus.Disconnected, fixture.bloc.state.value)
    }

    @Test
    fun cancellingUpgradeClosesCandidateAndActiveConnection() = runTest {
        val fixture = Fixture(this)
        val tls = fixture.offer(RpcTransport.TLS)
        fixture.start()
        val quic = fixture.offer(RpcTransport.QUIC)
        quic.pings.addLast { CompletableDeferred<Unit>().await() }
        fixture.tick(1000)
        backgroundScope.cancel()
        runCurrent()
        assertEquals(1, tls.closeCount)
        assertEquals(1, quic.closeCount)
        assertEquals(ConnectionStatus.Disconnected, fixture.bloc.state.value)
    }

    @Test
    fun repeatedStartDoesNotLaunchDuplicateLoops() = runTest {
        val fixture = Fixture(this)
        val quic = fixture.offer(RpcTransport.QUIC)
        fixture.start()
        fixture.start()
        fixture.tick(100)
        assertEquals(listOf(RpcTransport.QUIC), fixture.attempts)
        assertEquals(2, quic.pingCount)
        assertFalse(quic.closed)
    }

    private class Fixture(private val scope: TestScope) {
        val bloc = ConnectionBloc()
        val attempts = mutableListOf<RpcTransport>()
        val events = mutableListOf<String>()
        private val available = mutableMapOf<RpcTransport, ArrayDeque<FakeConnection>>()
        private val manager = MobileSpaceRpcClient(
            connectionBloc = bloc,
            scope = scope.backgroundScope,
            connect = { transport ->
                attempts.add(transport)
                events.add("connect $transport")
                available[transport]?.removeFirstOrNull() ?: error("$transport unavailable")
            },
            pingIntervalMillis = 100,
            maxMissedPongs = 3u,
            upgradeIntervalMillis = 1000
        )

        fun offer(transport: RpcTransport): FakeConnection =
            FakeConnection(transport, events).also {
                available.getOrPut(transport) { ArrayDeque() }.addLast(it)
            }

        suspend fun start() {
            manager.start()
            scope.runCurrent()
        }

        fun tick(millis: Long) {
            scope.advanceTimeBy(millis)
            scope.runCurrent()
        }
    }

    private class FakeConnection(
        override val transport: RpcTransport,
        private val events: MutableList<String>
    ) : RpcConnection {
        override val client: JsonRpcClient get() = error("Native handle is not used by lifecycle tests")
        val pings = ArrayDeque<suspend () -> Unit>()
        var pingCount = 0
        var restoreCount = 0
        var closeCount = 0
        var restoreFails = false
        var restoreAction: suspend () -> Unit = {}
        val closed get() = closeCount > 0

        override suspend fun ping() {
            check(!closed)
            pingCount++
            events.add("ping $transport")
            pings.removeFirstOrNull()?.invoke()
        }

        override suspend fun restore() {
            check(!closed)
            restoreCount++
            events.add("restore $transport")
            if (restoreFails) error("restore failed")
            restoreAction()
        }

        override suspend fun close() {
            closeCount++
            events.add("close $transport")
        }

        fun failNextPings(count: Int) {
            repeat(count) { pings.addLast { error("pong timed out") } }
        }
    }
}
