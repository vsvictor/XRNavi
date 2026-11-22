package com.mobilespace.xrnavi

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthorizedRpcTest {
    private val now = 1_000_000L

    private fun session(access: String, expiresAt: Long = now + 600_000) =
        AuthSession(access, "refresh", "Bearer", 600uL, expiresAt)

    @Test
    fun publicMethodsAreSentWithoutToken() = runTest {
        val seen = mutableListOf<String>()
        val rpc = AuthorizedRpc(SessionManager(MemorySecureSessionStorage(), { throw SessionExpiredException() }, { now })) { m, p ->
            seen += "$m $p"; "{}"
        }
        publicRpcMethods.forEach { rpc.call(it, "{}") }
        assertEquals(publicRpcMethods.size, seen.size)
        assertEquals(false, seen.any { "access_token" in it })
    }

    @Test
    fun protectedMethodWithoutSessionFailsWithoutNetwork() = runTest {
        val rpc = AuthorizedRpc(SessionManager(MemorySecureSessionStorage(), { throw SessionExpiredException() }, { now })) { _, _ ->
            error("must not call server")
        }
        assertFailsWith<SessionExpiredException> { rpc.call("garage.vehicles.list", "{}") }
    }

    @Test
    fun protectedMethodGetsCurrentTokenAndExpiredOneIsRefreshedFirst() = runTest {
        val sessions = SessionManager(MemorySecureSessionStorage(), { session("fresh") }, { now })
        sessions.start(session("stale", now - 1))
        val tokens = mutableListOf<String>()
        val rpc = AuthorizedRpc(sessions) { _, p ->
            tokens += Json.parseToJsonElement(p).jsonObject["access_token"]!!.jsonPrimitive.content
            "{}"
        }
        rpc.call("auth.getProfile", """{"access_token":"caller-supplied"}""")
        assertEquals(listOf("fresh"), tokens)
    }

    @Test
    fun serverRejectionRefreshesAndRetriesOnce() = runTest {
        val sessions = SessionManager(MemorySecureSessionStorage(), { session("second") }, { now })
        sessions.start(session("first"))
        val tokens = mutableListOf<String>()
        val rpc = AuthorizedRpc(sessions) { _, p ->
            val token = Json.parseToJsonElement(p).jsonObject["access_token"]!!.jsonPrimitive.content
            tokens += token
            if (token == "first") throw IllegalStateException("RpcError -32001 invalid token") else "{}"
        }
        rpc.call("garage.vehicles.list", "{}")
        assertEquals(listOf("first", "second"), tokens)
    }

    @Test
    fun rejectedRefreshEndsSession() = runTest {
        val sessions = SessionManager(MemorySecureSessionStorage(), { throw IllegalStateException("-32001") }, { now })
        sessions.start(session("first"))
        val rpc = AuthorizedRpc(sessions) { _, _ -> throw IllegalStateException("-32001") }
        assertFailsWith<SessionExpiredException> { rpc.call("garage.vehicles.list", "{}") }
        assertEquals(null, sessions.current())
    }
}
