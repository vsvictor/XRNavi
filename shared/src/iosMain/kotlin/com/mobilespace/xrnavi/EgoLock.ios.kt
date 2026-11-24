package com.mobilespace.xrnavi

import platform.Foundation.NSLock

internal actual class EgoLock actual constructor() {
    private val lock = NSLock()

    actual fun <T> withLock(action: () -> T): T {
        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
        }
    }
}
