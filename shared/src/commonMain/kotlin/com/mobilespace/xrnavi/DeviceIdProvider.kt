package com.mobilespace.xrnavi

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Stable per-installation identifier sent with `auth.login`; it survives sign-out. */
internal fun interface DeviceIdProvider {
    fun deviceId(): String
}

/** Generates a random id on first use and keeps it in the given storage. */
internal class StoredDeviceIdProvider(private val storage: SecureSessionStorage) : DeviceIdProvider {
    @OptIn(ExperimentalUuidApi::class)
    override fun deviceId(): String {
        storage.read()?.takeIf { it.isNotBlank() }?.let { return it }
        val created = Uuid.random().toString()
        storage.write(created)
        return created
    }
}
