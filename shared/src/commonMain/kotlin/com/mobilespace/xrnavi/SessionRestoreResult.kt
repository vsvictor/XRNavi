package com.mobilespace.xrnavi

internal sealed interface SessionRestoreResult {
    /** No stored session: show the sign-in page. */
    data object None : SessionRestoreResult

    /** A valid (possibly just refreshed) session: enter the app automatically. */
    class Ready(val session: AuthSession) : SessionRestoreResult

    /** The server rejected the stored tokens; they were removed. */
    data object Rejected : SessionRestoreResult

    /** The refresh could not be attempted (offline, timeout); stored tokens were kept. */
    data object Unavailable : SessionRestoreResult
}
