package com.mobilespace.xrnavi

internal actual class EgoLock actual constructor() {
    actual fun <T> withLock(action: () -> T): T = synchronized(this) { action() }
}
