package com.mobilespace.xrnavi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class JsonRpcQuicConnectionTest {
    @Test
    fun connectsToMobileSpaceAndReceivesImmediatePingPong() = runTest {
        val client = connectMobileSpaceRpcClient(host = "95.216.88.141")
        try {
            withContext(Dispatchers.Default) {
                client.transportPing(Constants.MOBILESPACE_RPC_PING_INTERVAL_MS)
            }
            val livenessLog = client.takeLivenessLog()
            assertTrue(livenessLog.any { it.startsWith("PING") }, "Expected immediate Ping: $livenessLog")
            assertTrue(livenessLog.any { it.startsWith("PONG") }, "Expected immediate Pong: $livenessLog")
        } finally {
            client.disconnect()
        }
    }
}
