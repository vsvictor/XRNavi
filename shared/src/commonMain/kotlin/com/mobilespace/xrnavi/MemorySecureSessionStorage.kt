package com.mobilespace.xrnavi

internal class MemorySecureSessionStorage(private var value: String? = null) : SecureSessionStorage {
    override fun read(): String? = value
    override fun write(value: String): Boolean {
        this.value = value
        return true
    }

    override fun clear() {
        value = null
    }
}
