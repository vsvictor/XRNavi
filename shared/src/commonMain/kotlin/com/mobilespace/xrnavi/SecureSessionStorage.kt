package com.mobilespace.xrnavi

/** Platform-protected storage for the serialized session (Android Keystore, iOS Keychain). */
interface SecureSessionStorage {
    fun read(): String?
    fun write(value: String): Boolean
    fun clear()
}
