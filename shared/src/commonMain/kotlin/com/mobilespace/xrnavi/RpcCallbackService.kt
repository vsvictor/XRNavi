package com.mobilespace.xrnavi

import com.vsvictor.jsonrpckmp.JsonRpcClient
import com.vsvictor.jsonrpckmp.RpcCallbackListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal const val resetTokenCallbackMethod = "auth.passwordResetToken"
internal const val callbackSubscribeMethod = "navi.callback.subscribe"

/** Extracts the reset token from a callback; returns null for other methods or malformed params. */
internal fun parseResetToken(method: String, paramsJson: String?): String? {
    if (method != resetTokenCallbackMethod || paramsJson == null) return null
    val params = try {
        Json.parseToJsonElement(paramsJson) as? JsonObject
    } catch (_: Exception) {
        null
    }
    return (params?.get("reset_token") as? JsonPrimitive)?.takeIf { it.isString }
        ?.content?.takeIf { it.isNotBlank() }
}

internal class RpcCallbackService(
    private val clients: Flow<JsonRpcClient>,
    private val call: suspend (method: String, paramsJson: String) -> String,
) : CallbackService {
    override suspend fun run(onResetToken: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        clients.collectLatest { client ->
            try {
                call(callbackSubscribeMethod, "{}")
                client.startCallbacks(object : RpcCallbackListener {
                    override fun onCallback(method: String, paramsJson: String?) {
                        parseResetToken(method, paramsJson)?.let(onResetToken)
                    }
                })
                awaitCancellation()
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) { runCatching { client.stopCallbacks() } }
                throw cancelled
            } catch (failure: Exception) {
                onFailure(failure)
            }
        }
    }
}
