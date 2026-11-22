package com.mobilespace.xrnavi

/**
 * JSON-RPC-2-OAuth2 answers invalid, expired or refresh-type tokens with error `-32001`.
 * The native client surfaces server errors as exceptions carrying that code in the message.
 */
internal fun Throwable.isAuthRejection(): Boolean =
    this is SessionExpiredException || message.orEmpty().contains("-32001")
