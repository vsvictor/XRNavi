package com.mobilespace.xrnavi

import com.exkmptracker.bloc.TrackerState
import com.exkmptracker.model.TrackingIdentity
import com.exkmptracker.model.TrackingSession
import com.exkmptracker.model.TrackEstimate
import com.exkmptracker.model.LocationSource
import com.exkmptracker.ports.TrackerClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerIntegrationTest {
    private val session = TrackingSession("confirmed", TrackingIdentity("user", "personal:user"))

    private class Runtime : AppTrackingRuntime {
        val mutable = MutableStateFlow(TrackerState())
        override val state = mutable.asStateFlow()
        val starts = mutableListOf<TrackingSession>()
        var stops = 0
        var waitForPermission = false
        var failStop = false
        var stopGate: CompletableDeferred<Unit>? = null
        override suspend fun start(session: TrackingSession) {
            starts += session
            if (waitForPermission) {
                mutable.value = TrackerState(error = "Permission required")
                awaitCancellation()
            }
            mutable.value = TrackerState(running = true, identity = session.identity)
        }
        override suspend fun stop() {
            stops++
            stopGate?.await()
            check(!failStop) { "Unable to stop runtime" }
            mutable.value = TrackerState()
        }
    }

    @Test fun loginStartsWithoutProfileAndLogoutRetainsInstallationId() = runTest {
        val storage = MemorySecureSessionStorage()
        val ids = StoredDeviceIdProvider(storage)
        val id = ids.deviceId()
        val generation = MutableStateFlow<Long?>(null)
        val runtime = Runtime()
        var opens = 0
        val bloc = TrackerIntegrationBloc(backgroundScope, generation, ids, {
            assertEquals(id, it)
            opens++
            TrackerRegistration(it, session)
        }, runtime)
        runCurrent()
        assertTrue(runtime.starts.isEmpty())
        generation.value = 1
        runCurrent()
        assertEquals(1, opens)
        assertEquals(listOf(session), runtime.starts)
        assertEquals(id, bloc.state.value.trackerId)
        assertTrue(bloc.state.value.tracking.running)
        generation.value = null
        runCurrent()
        assertFalse(bloc.state.value.tracking.running)
        assertEquals(id, ids.deviceId())
        generation.value = 2
        runCurrent()
        assertEquals(2, opens)
        assertEquals(id, bloc.state.value.trackerId)
    }

    @Test fun logoutCancelsRegistrationBeforeAnySensorsStart() = runTest {
        val generation = MutableStateFlow<Long?>(1)
        val runtime = Runtime()
        var cancelled = false
        val bloc = TrackerIntegrationBloc(backgroundScope, generation, { "id" }, {
            try { awaitCancellation() } finally { cancelled = true }
        }, runtime)
        runCurrent()
        assertTrue(bloc.state.value.registering)
        generation.value = null
        runCurrent()
        assertTrue(cancelled)
        assertTrue(runtime.starts.isEmpty())
        assertFalse(bloc.state.value.authenticated)
    }

    @Test fun registrationErrorsAreVisibleAndRetryPreservesTheId() = runTest {
        val generation = MutableStateFlow<Long?>(1)
        val runtime = Runtime()
        var attempts = 0
        val bloc = TrackerIntegrationBloc(backgroundScope, generation, { "id" }, {
            attempts++
            if (attempts == 1) error("Server unavailable")
            TrackerRegistration(it, session)
        }, runtime)
        runCurrent()
        assertEquals(TrackerIntegrationProblem.Registration, bloc.state.value.problem)
        assertTrue(runtime.starts.isEmpty())
        bloc.retry()
        runCurrent()
        assertTrue(bloc.state.value.tracking.running)
        assertEquals("id", bloc.state.value.trackerId)
    }

    @Test fun pendingPermissionsAreVisibleAndLogoutCancelsThePendingStart() = runTest {
        val generation = MutableStateFlow<Long?>(1)
        val runtime = Runtime().also { it.waitForPermission = true }
        val bloc = TrackerIntegrationBloc(backgroundScope, generation, { "id" },
            { TrackerRegistration(it, session) }, runtime)
        runCurrent()
        assertEquals(TrackerIntegrationProblem.Runtime, bloc.state.value.problem)
        assertFalse(bloc.state.value.tracking.running)
        generation.value = null
        runCurrent()
        assertFalse(bloc.state.value.authenticated)
        assertTrue(runtime.stops >= 2)
    }

    @Test fun newOwnerStartsOnlyAfterOldRuntimeStopped() = runTest {
        val generation = MutableStateFlow<Long?>(1)
        val runtime = Runtime()
        var open = 0
        val bloc = TrackerIntegrationBloc(backgroundScope, generation, { "id" }, {
            open++
            if (open == 2) assertFalse(runtime.state.value.running)
            TrackerRegistration(it, session.copy(identity = TrackingIdentity("owner$open", "personal:owner$open")))
        }, runtime)
        runCurrent()
        generation.value = 2
        runCurrent()
        assertEquals("owner2", bloc.state.value.tracking.identity?.driverId)
        assertEquals(2, runtime.starts.size)
    }

    @Test fun tokenRefreshDoesNotCreateAnotherTrackingGeneration() = runTest {
        val manager = SessionManager(MemorySecureSessionStorage(), {
            AuthSession("renewed", "refresh2", "Bearer", 3600u, Long.MAX_VALUE)
        })
        assertNull(manager.trackingGeneration.value)
        manager.start(AuthSession("first", "refresh1", "Bearer", 3600u, Long.MAX_VALUE))
        val initial = manager.trackingGeneration.value
        manager.refreshNow()
        assertEquals(initial, manager.trackingGeneration.value)
        manager.clear()
        assertNull(manager.trackingGeneration.value)
    }

    @Test fun restoredSessionStartsTrackingWithoutAnotherLogin() = runTest {
        val storage = MemorySecureSessionStorage()
        SessionManager(storage, { error("Unexpected refresh") }).start(
            AuthSession("access", "refresh", "Bearer", 3600u, Long.MAX_VALUE))
        val restored = SessionManager(storage, { error("Unexpected refresh") })
        val runtime = Runtime()
        val bloc = TrackerIntegrationBloc(backgroundScope, restored.trackingGeneration, { "id" },
            { TrackerRegistration(it, session) }, runtime)
        runCurrent()
        assertTrue(runtime.starts.isEmpty())
        assertIs<SessionRestoreResult.Ready>(restored.restore())
        runCurrent()
        assertTrue(bloc.state.value.tracking.running)
        assertEquals(listOf(session), runtime.starts)
    }

    @Test fun rejectedRefreshStopsTheActiveRuntime() = runTest {
        val manager = SessionManager(MemorySecureSessionStorage(), { throw SessionExpiredException() })
        val runtime = Runtime()
        val bloc = TrackerIntegrationBloc(backgroundScope, manager.trackingGeneration, { "id" },
            { TrackerRegistration(it, session) }, runtime)
        manager.start(AuthSession("access", "refresh", "Bearer", 3600u, Long.MAX_VALUE))
        runCurrent()
        assertTrue(bloc.state.value.tracking.running)
        assertFailsWith<SessionExpiredException> { manager.refreshNow() }
        runCurrent()
        assertNull(manager.trackingGeneration.value)
        assertFalse(bloc.state.value.tracking.running)
        assertFalse(bloc.state.value.authenticated)
    }

    @Test fun failedIdPersistenceCannotCreateAnUnstableServerIdentity() {
        val storage = object : SecureSessionStorage {
            override fun read(): String? = null
            override fun write(value: String) = false
            override fun clear() = Unit
        }
        assertFailsWith<IllegalStateException> { StoredDeviceIdProvider(storage).deviceId() }
    }

    @Test fun newlyGrantedPermissionsRefreshTokensBeforeStartingRuntime() = runTest {
        val manager = SessionManager(MemorySecureSessionStorage(), {
            AuthSession("renewed", "refresh2", "Bearer", 3600u, Long.MAX_VALUE)
        })
        manager.start(AuthSession("original", "refresh1", "Bearer", 3600u, Long.MAX_VALUE))
        val generation = manager.trackingGeneration.value
        var refreshes = 0
        val registration = RpcTrackerRegistrationService(
            refreshSession = { manager.refreshNow(); refreshes++ },
        ) { _, _ ->
            """{"trackerId":"device","sessionId":"confirmed","driverId":"user","brandId":"personal:user","refreshRequired":true}"""
        }
        val runtime = Runtime()
        val bloc = TrackerIntegrationBloc(backgroundScope, manager.trackingGeneration, { "device" },
            { registration.open(it).also { assertEquals("renewed", manager.current()?.accessToken) } }, runtime)
        runCurrent()
        assertEquals(1, refreshes)
        assertEquals(generation, manager.trackingGeneration.value)
        assertEquals("refresh2", manager.current()?.refreshToken)
        assertEquals(listOf(session), runtime.starts)
        assertTrue(bloc.state.value.tracking.running)
    }

    @Test fun refreshFailureOrMalformedFlagPreventsRegistrationCompletion() = runTest {
        var refreshes = 0
        val registration = RpcTrackerRegistrationService(
            refreshSession = { refreshes++; error("Refresh unavailable") },
        ) { _, _ ->
            """{"trackerId":"device","sessionId":"confirmed","driverId":"user","brandId":"personal:user","refreshRequired":true}"""
        }
        assertFailsWith<IllegalStateException> { registration.open("device") }
        assertEquals(1, refreshes)
        for (flag in listOf("\"true\"", "null", "1", "{}")) {
            assertFailsWith<IllegalArgumentException> {
                RpcTrackerRegistrationService { _, _ ->
                    """{"trackerId":"device","sessionId":"confirmed","driverId":"user","brandId":"personal:user","refreshRequired":$flag}"""
                }.open("device")
            }
        }
    }

    @Test fun serverRegistrationMustConfirmTheSameDeviceIdAndRealIdentity() = runTest {
        val registration = RpcTrackerRegistrationService { method, _ ->
            assertEquals("tracking.open", method)
            """{"trackerId":"device","sessionId":"confirmed","driverId":"user","brandId":"personal:user"}"""
        }.open("device")
        assertEquals("device", registration.trackerId)
        assertEquals(session, registration.session)
        assertFailsWith<IllegalArgumentException> {
            RpcTrackerRegistrationService { _, _ ->
                """{"trackerId":"wrong","sessionId":"s","driverId":"u","brandId":"b"}"""
            }.open("device")
        }
    }

    @Test fun stopFailureBlocksAnotherOwnerUntilShutdownSucceeds() = runTest {
        val generation = MutableStateFlow<Long?>(1)
        val runtime = Runtime()
        val bloc = TrackerIntegrationBloc(backgroundScope, generation, { "id" },
            { TrackerRegistration(it, session) }, runtime)
        runCurrent()
        runtime.failStop = true
        generation.value = 2
        runCurrent()
        assertEquals(TrackerIntegrationProblem.Runtime, bloc.state.value.problem)
        assertEquals(1, runtime.starts.size)
        runtime.failStop = false
        bloc.retry()
        runCurrent()
        assertEquals(2, runtime.starts.size)
        assertTrue(bloc.state.value.tracking.running)
    }

    @Test fun logoutBeforeQueuedLoginCannotStartTrackingLater() = runTest {
        val manager = SessionManager(MemorySecureSessionStorage(), { error("Unexpected refresh") })
        val account = AuthenticatedAccountBloc({ error("Unexpected profile request") }, backgroundScope, manager)
        account.dispatch(AuthenticatedAccountEvent.LoginCompleted(
            AuthSession("access", "refresh", "Bearer", 3600u, Long.MAX_VALUE)))
        account.dispatch(AuthenticatedAccountEvent.Logout)
        runCurrent()
        assertNull(manager.current())
        assertNull(manager.trackingGeneration.value)
        assertEquals(AccountMode.SignedOut, account.state.value.mode)
    }

    @Test fun failedSessionPersistenceNeverAuthenticatesOrStartsTracking() = runTest {
        val storage = object : SecureSessionStorage {
            override fun read(): String? = null
            override fun write(value: String) = false
            override fun clear() = Unit
        }
        val manager = SessionManager(storage, { error("Unexpected refresh") })
        val account = AuthenticatedAccountBloc({ error("Unexpected profile request") }, backgroundScope, manager)
        account.dispatch(AuthenticatedAccountEvent.LoginCompleted(
            AuthSession("access", "refresh", "Bearer", 3600u, Long.MAX_VALUE)))
        runCurrent()
        assertNull(manager.current())
        assertNull(manager.trackingGeneration.value)
        assertEquals(AccountMode.SignedOut, account.state.value.mode)
        assertTrue(account.state.value.sessionPersistenceFailed)
    }

    @Test fun failedReplacementSessionCannotKeepOldOwnerTracking() = runTest {
        var writes = 0
        val storage = object : SecureSessionStorage {
            override fun read(): String? = null
            override fun write(value: String) = ++writes == 1
            override fun clear() = Unit
        }
        val manager = SessionManager(storage, { error("Unexpected refresh") })
        val first = AuthSession("access", "refresh", "Bearer", 3600u, Long.MAX_VALUE)
        manager.start(first)
        assertFailsWith<IllegalStateException> {
            manager.start(AuthSession("other", "refresh2", "Bearer", 3600u, Long.MAX_VALUE))
        }
        assertNull(manager.current())
        assertNull(manager.trackingGeneration.value)
    }

    @Test fun explicitDemoClearsTheAuthenticatedTrackingGeneration() = runTest {
        val manager = SessionManager(MemorySecureSessionStorage(), { error("Unexpected refresh") })
        manager.start(AuthSession("access", "refresh", "Bearer", 3600u, Long.MAX_VALUE))
        val account = AuthenticatedAccountBloc({ error("Unexpected profile request") }, backgroundScope, manager)
        account.dispatch(AuthenticatedAccountEvent.ContinueWithoutLogin)
        runCurrent()
        assertNull(manager.trackingGeneration.value)
        assertEquals(AccountMode.Demo, account.state.value.mode)
    }

    @Test fun mapKeepsObservationTimeAndDoesNotReemitOnUnrelatedTrackerChanges() = runTest {
        val estimate = TrackEstimate(50.0, 30.0, 4.0, 0.0, 0.0, 95.0, LocationSource.gps)
        val state = MutableStateFlow(TrackerIntegrationState(authenticated = true,
            tracking = TrackerState(running = true, estimate = estimate)))
        val clock = object : TrackerClock {
            override fun wallTimeMs() = 1_000_000L
            override fun monotonicSeconds() = 100.0
        }
        val native = FakeLocationProvider().also { it.permission.value = LocationPermission.Granted }
        val locations = TrackerLocationBloc(backgroundScope, state, native, clock)
        val provider = TrackingLocationProvider(native, locations)
        val fixes = mutableListOf<DeviceFix>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            provider.fixes().filterNotNull().toList(fixes)
        }
        runCurrent()
        assertEquals(995_000L, fixes.single().timestampMillis)
        state.value = state.value.copy(registering = true)
        runCurrent()
        assertEquals(1, fixes.size)
        state.value = state.value.copy(tracking = state.value.tracking.copy(
            estimate = estimate.copy(latitude = 50.1, monotonicSeconds = 99.0)))
        runCurrent()
        assertEquals(999_000L, fixes.last().timestampMillis)
        state.value = state.value.copy(authenticated = false)
        runCurrent()
        assertEquals(2, fixes.size)
        assertNull(locations.state.value)
    }

    @Test fun logoutInvalidatesMapPositionBeforeNativeStopCompletes() = runTest {
        val generation = MutableStateFlow<Long?>(1)
        val runtime = Runtime()
        val tracker = TrackerIntegrationBloc(backgroundScope, generation, { "id" },
            { TrackerRegistration(it, session) }, runtime)
        val native = FakeLocationProvider().also { it.permission.value = LocationPermission.Granted }
        val clock = object : TrackerClock {
            override fun wallTimeMs() = 1_000_000L
            override fun monotonicSeconds() = 100.0
        }
        val locations = TrackerLocationBloc(backgroundScope, tracker.state, native, clock)
        runCurrent()
        runtime.mutable.value = runtime.mutable.value.copy(
            estimate = TrackEstimate(50.0, 30.0, 4.0, 0.0, 0.0, 100.0, LocationSource.gps))
        runCurrent()
        assertNotNull(locations.state.value)
        val stopped = CompletableDeferred<Unit>()
        runtime.stopGate = stopped
        generation.value = null
        runCurrent()
        assertFalse(stopped.isCompleted)
        assertNull(locations.state.value)
        stopped.complete(Unit)
        runCurrent()
        assertFalse(tracker.state.value.authenticated)
    }
}
