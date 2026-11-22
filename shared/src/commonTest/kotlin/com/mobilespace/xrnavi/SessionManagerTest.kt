package com.mobilespace.xrnavi

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class SessionManagerTest {
    private var now = 1_000_000L

    private fun session(access: String, expiresAt: Long) =
        AuthSession(access, "refresh-$access", "Bearer", 3600uL, expiresAt)

    private fun manager(
        storage: SecureSessionStorage = MemorySecureSessionStorage(),
        refresh: suspend (String) -> AuthSession = { throw SessionExpiredException() },
    ) = SessionManager(storage, { refresh(it) }, { now })

    @Test
    fun noStoredTokenGoesToSignIn() = runTest {
        assertIs<SessionRestoreResult.None>(manager().restore())
    }

    @Test
    fun corruptedStorageIsTreatedAsNoToken() = runTest {
        val storage = MemorySecureSessionStorage("garbage")
        assertIs<SessionRestoreResult.None>(manager(storage).restore())
        assertNull(storage.read())
    }

    @Test
    fun validStoredTokenSignsInWithoutRefresh() = runTest {
        val storage = MemorySecureSessionStorage()
        manager(storage).start(session("a", now + 600_000))
        val result = manager(storage) { error("must not refresh") }.restore()
        assertEquals("a", assertIs<SessionRestoreResult.Ready>(result).session.accessToken)
    }

    @Test
    fun expiredTokenIsRefreshedAndPersisted() = runTest {
        val storage = MemorySecureSessionStorage()
        manager(storage).start(session("old", now - 1))
        val restored = manager(storage) { assertEquals("refresh-old", it); session("new", now + 600_000) }
        assertEquals("new", assertIs<SessionRestoreResult.Ready>(restored.restore()).session.accessToken)
        assertEquals("new", assertIs<SessionRestoreResult.Ready>(manager(storage).restore()).session.accessToken)
    }

    @Test
    fun rejectedRefreshClearsStorage() = runTest {
        val storage = MemorySecureSessionStorage()
        manager(storage).start(session("old", now - 1))
        assertIs<SessionRestoreResult.Rejected>(manager(storage).restore())
        assertNull(storage.read())
    }

    @Test
    fun networkFailureKeepsStoredTokens() = runTest {
        val storage = MemorySecureSessionStorage()
        manager(storage).start(session("old", now - 1))
        assertIs<SessionRestoreResult.Unavailable>(manager(storage) { error("offline") }.restore())
        assertEquals(true, storage.read() != null)
    }

    @Test
    fun validSessionRefreshesOnlyWhenExpiring() = runTest {
        var refreshes = 0
        val sessions = manager { refreshes++; session("new", now + 600_000) }
        sessions.start(session("a", now + 600_000))
        assertEquals("a", sessions.validSession().accessToken)
        now += 580_000
        assertEquals("new", sessions.validSession().accessToken)
        assertEquals(1, refreshes)
    }

    @Test
    fun validSessionWithoutLoginIsExpired() = runTest {
        assertFailsWith<SessionExpiredException> { manager().validSession() }
    }

    @Test
    fun clearRemovesStoredSession() = runTest {
        val storage = MemorySecureSessionStorage()
        val sessions = manager(storage)
        sessions.start(session("a", now + 600_000))
        sessions.clear()
        assertNull(storage.read())
        assertNull(sessions.current())
    }
}
