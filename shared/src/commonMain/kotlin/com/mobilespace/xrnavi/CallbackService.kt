package com.mobilespace.xrnavi

/** Receives server-to-client notifications for the signed-in user. */
internal interface CallbackService {
    /** Subscribes and delivers callbacks until cancelled; re-subscribes after every reconnect. */
    suspend fun run(onResetToken: (String) -> Unit, onFailure: (Throwable) -> Unit)
}
